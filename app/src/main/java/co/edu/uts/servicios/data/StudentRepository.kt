package co.edu.uts.servicios.data

/** La interfaz permite probar el ViewModel sin depender de Android. */
interface StudentRepository {
    fun load(): PortalData
    fun save(data: PortalData)
}
