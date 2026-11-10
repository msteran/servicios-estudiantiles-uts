package co.edu.uts.servicios.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.uts.servicios.data.*
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PortalUiState(
    val name: String = "", val code: String = "", val faculty: Faculty? = null,
    val nameError: String? = null, val codeError: String? = null, val facultyError: String? = null,
    val student: Student? = null,
    val courses: List<Course> = CourseCatalog.courses,
    val selectedCourse: Course = CourseCatalog.courses.first(),
    val scholarship: Boolean = false, val icetex: Boolean = false,
    val quote: Quote = Quote(200_000, 0, 0, 200_000),
    val enrollments: List<Enrollment> = emptyList(),
    val accumulatedTotal: Long = 0,
    val canLiquidate: Boolean = false,
    val message: String? = null,
    val storageError: String? = null
)

class PortalViewModel(private val repository: StudentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PortalUiState())
    val uiState: StateFlow<PortalUiState> = _uiState.asStateFlow()

    init {
        try {
            val data = repository.load()
            _uiState.update { it.copy(name = data.student?.name.orEmpty(), code = data.student?.code.orEmpty(),
                faculty = data.student?.faculty, student = data.student, enrollments = data.enrollments,
                accumulatedTotal = data.enrollments.sumOf { row -> row.quote.total }, canLiquidate = data.student != null) }
        } catch (_: Exception) {
            // No sobreescribir datos que no se pudieron leer.
            _uiState.update { it.copy(storageError = "No se pudieron leer los datos locales. Reinicia la aplicación.") }
        }
    }

    fun changeName(value: String) = _uiState.update { it.copy(name = value, nameError = null, message = null) }
    fun changeCode(value: String) = _uiState.update { it.copy(code = value, codeError = null, message = null) }
    fun changeFaculty(value: Faculty) = _uiState.update { it.copy(faculty = value, facultyError = null, message = null) }

    fun saveProfile() {
        val s = _uiState.value
        if (s.storageError != null) return
        val name = s.name.trim().replace(Regex("\\s+"), " ")
        val code = s.code.trim()
        val nameError = if (name.length !in 2..80 || !name.any { it.isLetter() } ||
            !name.all { it.isLetter() || it == ' ' || it == '\'' || it == '-' })
            "Usa entre 2 y 80 caracteres: letras, espacios, apóstrofo o guion." else null
        val codeError = if (!code.matches(Regex("[0-9]{5,12}"))) "Ingresa entre 5 y 12 dígitos." else null
        val facultyError = if (s.faculty == null) "Selecciona tu facultad." else null
        if (nameError != null || codeError != null || facultyError != null) {
            _uiState.update { it.copy(nameError = nameError, codeError = codeError, facultyError = facultyError, message = null) }
            return
        }
        val slug = Normalizer.normalize(name, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT).replace(' ', '.').replace(Regex("[^a-z0-9.]"), "").trim('.')
            .ifBlank { "estudiante" }
        val student = Student(name, code, s.faculty!!, "$slug.$code@uts.edu.co")
        if (persist(PortalData(student, s.enrollments))) {
            _uiState.update { it.copy(student = student, name = name, code = code,
                nameError = null, codeError = null, facultyError = null,
                canLiquidate = true, message = "Perfil guardado. Ya puedes liquidar tus cursos.") }
        }
    }

    fun selectCourse(course: Course) {
        if (course !in CourseCatalog.courses) return
        _uiState.update { val next = it.copy(selectedCourse = course, message = null); next.copy(quote = calculate(next)) }
    }
    fun setScholarship(value: Boolean) = _uiState.update {
        val next = it.copy(scholarship = value, message = null); next.copy(quote = calculate(next))
    }
    fun setIcetex(value: Boolean) = _uiState.update {
        val next = it.copy(icetex = value, message = null); next.copy(quote = calculate(next))
    }

    /** Primero la beca; el recargo se aplica sobre el valor restante a financiar. */
    private fun calculate(s: PortalUiState): Quote {
        val base = s.selectedCourse.base
        val discount = if (s.scholarship) base / 2 else 0
        val surcharge = if (s.icetex) (base - discount) * 5 / 100 else 0
        return Quote(base, discount, surcharge, base - discount + surcharge)
    }

    fun liquidate() {
        val s = _uiState.value
        // La protección existe también aquí, no solamente en el botón.
        val student = s.student ?: run {
            _uiState.update { it.copy(message = "Primero guarda tu perfil académico.") }; return
        }
        if (s.storageError != null) return
        val quote = calculate(s)
        val enrollment = Enrollment(UUID.randomUUID().toString(), s.selectedCourse.title, student,
            s.scholarship, s.icetex, quote, System.currentTimeMillis())
        val rows = listOf(enrollment) + s.enrollments
        if (persist(PortalData(student, rows))) {
            _uiState.update { it.copy(enrollments = rows, accumulatedTotal = rows.sumOf { row -> row.quote.total },
                message = "Liquidación registrada. Consúltala en Historial.") }
        }
    }

    private fun persist(data: PortalData): Boolean = try {
        repository.save(data); true
    } catch (_: Exception) {
        _uiState.update { it.copy(message = "No se pudo guardar. Inténtalo de nuevo.") }; false
    }

    class Factory(private val repository: StudentRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PortalViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return PortalViewModel(repository) as T
        }
    }
}
