package co.edu.uts.servicios

import co.edu.uts.servicios.data.*
import co.edu.uts.servicios.viewmodel.PortalViewModel
import org.junit.Assert.*
import org.junit.Test

private class MemoryRepository(var data: PortalData = PortalData()) : StudentRepository {
    var failSave = false
    override fun load() = data
    override fun save(data: PortalData) {
        if (failSave) error("Disco no disponible")
        this.data = data
    }
}

class PortalViewModelTest {
    private fun register(vm: PortalViewModel) {
        vm.changeName("  José   Pérez  ")
        vm.changeCode("0012345")
        vm.changeFaculty(Faculty.ENGINEERING)
        vm.saveProfile()
    }

    @Test fun profileIsRequiredEvenWhenCallingViewModelDirectly() {
        val vm = PortalViewModel(MemoryRepository())
        assertFalse(vm.uiState.value.canLiquidate)
        vm.liquidate()
        assertTrue(vm.uiState.value.enrollments.isEmpty())
        assertEquals(0L, vm.uiState.value.accumulatedTotal)
    }
    @Test fun rejectsEmptyProfile() {
        val vm = PortalViewModel(MemoryRepository()); vm.saveProfile()
        val s = vm.uiState.value
        assertNotNull(s.nameError); assertNotNull(s.codeError); assertNotNull(s.facultyError)
        assertNull(s.student)
    }
    @Test fun generatesNormalizedInstitutionalEmailAndKeepsLeadingZeroes() {
        val vm = PortalViewModel(MemoryRepository()); register(vm)
        assertEquals("jose.perez.0012345@uts.edu.co", vm.uiState.value.student?.email)
        assertEquals("José Pérez", vm.uiState.value.student?.name)
        assertTrue(vm.uiState.value.canLiquidate)
    }
    @Test fun rejectsInvalidCodeAndNumericName() {
        val vm = PortalViewModel(MemoryRepository())
        vm.changeName("1234"); vm.changeCode("12x"); vm.changeFaculty(Faculty.SOCIOECONOMIC); vm.saveProfile()
        assertNotNull(vm.uiState.value.nameError); assertNotNull(vm.uiState.value.codeError)
    }
    @Test fun computesAllBenefitCombinationsForEveryCourse() {
        val vm = PortalViewModel(MemoryRepository())
        val expected = listOf(
            listOf(200_000L, 100_000L, 210_000L, 105_000L),
            listOf(300_000L, 150_000L, 315_000L, 157_500L),
            listOf(400_000L, 200_000L, 420_000L, 210_000L))
        CourseCatalog.courses.forEachIndexed { index, course ->
            vm.selectCourse(course)
            listOf(false to false, true to false, false to true, true to true).forEachIndexed { combination, flags ->
                vm.setScholarship(flags.first); vm.setIcetex(flags.second)
                assertEquals(expected[index][combination], vm.uiState.value.quote.total)
            }
        }
    }
    @Test fun accumulatesAndPreservesHistoricalAmounts() {
        val vm = PortalViewModel(MemoryRepository()); register(vm)
        vm.setScholarship(true); vm.setIcetex(true); vm.liquidate()
        vm.selectCourse(CourseCatalog.courses[1]); vm.setScholarship(false); vm.setIcetex(false); vm.liquidate()
        assertEquals(405_000L, vm.uiState.value.accumulatedTotal)
        assertEquals(105_000L, vm.uiState.value.enrollments.last().quote.total)
        assertEquals(2, vm.uiState.value.enrollments.map { it.id }.distinct().size)
    }
    @Test fun reloadsSavedProfileAndHistory() {
        val repo = MemoryRepository(); val first = PortalViewModel(repo)
        register(first); first.liquidate()
        val restored = PortalViewModel(repo).uiState.value
        assertTrue(restored.canLiquidate); assertEquals(200_000L, restored.accumulatedTotal)
        assertEquals(1, restored.enrollments.size)
    }
    @Test fun failedSaveDoesNotAddHistory() {
        val repo = MemoryRepository(); val vm = PortalViewModel(repo); register(vm)
        repo.failSave = true; vm.liquidate()
        assertTrue(vm.uiState.value.enrollments.isEmpty())
        assertEquals("No se pudo guardar. Inténtalo de nuevo.", vm.uiState.value.message)
    }
    @Test fun editingProfileDoesNotRewriteOldEnrollmentOwner() {
        val vm = PortalViewModel(MemoryRepository()); register(vm); vm.liquidate()
        vm.changeName("Ana Gómez"); vm.saveProfile()
        assertEquals("José Pérez", vm.uiState.value.enrollments.single().student.name)
        assertEquals("Ana Gómez", vm.uiState.value.student?.name)
    }
    @Test fun rejectsForeignCoursePrices() {
        val vm = PortalViewModel(MemoryRepository())
        vm.selectCourse(Course("fake", "Falso", "", -100))
        assertEquals(200_000L, vm.uiState.value.quote.total)
    }
    @Test fun readFailureBlocksWrites() {
        var writes = 0
        val vm = PortalViewModel(object : StudentRepository {
            override fun load(): PortalData = error("JSON corrupto")
            override fun save(data: PortalData) { writes++ }
        })
        register(vm); vm.liquidate()
        assertNotNull(vm.uiState.value.storageError); assertEquals(0, writes)
    }
}
