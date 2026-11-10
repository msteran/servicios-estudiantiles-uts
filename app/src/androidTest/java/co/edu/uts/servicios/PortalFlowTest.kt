package co.edu.uts.servicios

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uts.servicios.data.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PortalFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun listText(list: String, text: String): SemanticsNodeInteraction {
        compose.onNodeWithTag(list).performScrollToNode(hasText(text))
        return compose.onNodeWithText(text).performScrollTo()
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        val file = java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test fun completeStudentJourney() {
        compose.onNodeWithText("Historial", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Tu siguiente paso comienza aquí").assertIsDisplayed()
        compose.onNodeWithText("Matrícula", useUnmergedTree = true).performClick()
        listText("tuition_list", "Liquidar").assertIsNotEnabled()
        capture("01-bloqueo")
        compose.onNodeWithText("Perfil", useUnmergedTree = true).performClick()
        listText("profile_list", "Guardar perfil").performClick()
        compose.onNodeWithText("Ingresa entre 5 y 12 dígitos.").assertExists()
        listText("profile_list", "Nombre completo").performTextInput("Ana Torres")
        listText("profile_list", "Código estudiantil").performTextInput("2026001")
        listText("profile_list", "Facultad").performClick()
        compose.onNodeWithText("Ingenierías").performClick()
        listText("profile_list", "Guardar perfil").performClick()
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        listText("profile_list", "ana.torres.2026001@uts.edu.co").assertIsDisplayed()
        capture("02-perfil")
        compose.onNodeWithText("Matrícula", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("tuition_list").performScrollToNode(hasContentDescription("Beca de Excelencia UTS"))
        compose.onNodeWithContentDescription("Beca de Excelencia UTS").performClick()
        compose.onNodeWithTag("tuition_list").performScrollToNode(hasContentDescription("Financiar con Icetex"))
        compose.onNodeWithContentDescription("Financiar con Icetex").performClick()
        listText("tuition_list", "Liquidar").assertIsEnabled().performClick()
        capture("03-liquidacion")
        compose.onNodeWithText("Historial", useUnmergedTree = true).performClick()
        compose.onNodeWithText("1 liquidaciones registradas · COP").assertIsDisplayed()
        listText("history_list", "Inglés Técnico").assertIsDisplayed()
        capture("04-historial")
        // La Activity se recrea, como durante un cambio de configuración.
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Historial", useUnmergedTree = true).performClick()
        compose.onNodeWithText("1 liquidaciones registradas · COP").assertIsDisplayed()
        val repo = LocalStudentRepository(InstrumentationRegistry.getInstrumentation().targetContext)
        assertEquals(105_000L, repo.load().enrollments.single().quote.total)
        assertEquals("ana.torres.2026001@uts.edu.co", repo.load().student?.email)
    }

    @Test fun localRepositoryRoundTrip() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Archivo aislado mediante ContextWrapper para no alterar la sesión de la app.
        val isolated = object : android.content.ContextWrapper(context) {
            override fun getSharedPreferences(name: String, mode: Int) = super.getSharedPreferences("test_round_trip", Context.MODE_PRIVATE)
        }
        val repo = LocalStudentRepository(isolated)
        val student = Student("José Pérez", "0012345", Faculty.SOCIOECONOMIC, "jose.perez.0012345@uts.edu.co")
        val data = PortalData(student, listOf(Enrollment("test-id", "Programación Web", student,
            true, true, Quote(300_000, 150_000, 7_500, 157_500), 1234)))
        repo.save(data)
        assertEquals(data, LocalStudentRepository(isolated).load())
    }
}
