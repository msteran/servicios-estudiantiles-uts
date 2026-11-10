package co.edu.uts.servicios.data

enum class Faculty(val label: String) {
    ENGINEERING("Ingenierías"), SOCIOECONOMIC("Ciencias Socioeconómicas")
}

data class Student(val name: String, val code: String, val faculty: Faculty, val email: String)
data class Course(val id: String, val title: String, val description: String, val base: Long)

// Valores de demostración en pesos colombianos, no tarifas oficiales de UTS.
object CourseCatalog {
    val courses = listOf(
        Course("english", "Inglés Técnico", "Comunicación para tu futuro profesional", 200_000),
        Course("web", "Programación Web", "Construye experiencias para la web", 300_000),
        Course("security", "Ciberseguridad", "Protege la información y los sistemas", 400_000)
    )
}

data class Quote(val base: Long, val discount: Long, val surcharge: Long, val total: Long)
data class Enrollment(
    val id: String, val courseTitle: String, val student: Student,
    val scholarship: Boolean, val icetex: Boolean, val quote: Quote, val createdAt: Long
)
data class PortalData(val student: Student? = null, val enrollments: List<Enrollment> = emptyList())
