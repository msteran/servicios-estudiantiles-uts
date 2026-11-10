# Mi UTS - Servicios Estudiantiles

Aplicación Android nativa en Kotlin y Jetpack Compose para el proyecto final de Aplicaciones Móviles. Simula un portal de autoservicio de las Unidades Tecnológicas de Santander. No usa servicios institucionales reales ni realiza pagos.

## Abrir y ejecutar

1. Abrir esta carpeta desde **File > Open** en Android Studio.
2. Elegir **JDK 17 o 21** como Gradle JDK. El Java 25 incluido en este Android Studio no es compatible con Gradle 8.9.
3. Instalar Android SDK 35 si no está disponible y sincronizar Gradle.
4. Seleccionar la configuración `app` y un emulador o dispositivo Android 8.0 / API 26 o superior.
5. Presionar Run. El nombre de la aplicación instalada es **Mi UTS**.

En este equipo el SDK está configurado en `local.properties`, archivo excluido de Git. El proyecto usa AGP 8.7.3, Gradle 8.9, Kotlin 2.0.21, Compose BOM 2024.12.01 y Lifecycle 2.8.7.

Desde PowerShell, dentro del proyecto:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

La prueba de recorrido completo asume una instalación sin perfil ni historial. Ejecutarla en un emulador de pruebas nuevo o con los datos de **Mi UTS** vacíos. El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Arquitectura MVVM

```text
app/src/main/java/co/edu/uts/servicios/
  MainActivity.kt                 Punto de entrada e inyección del repositorio
  data/
    Models.kt                     Estudiante, curso, cotización, liquidación
    StudentRepository.kt          Contrato de almacenamiento
    LocalStudentRepository.kt     SharedPreferences + JSON local
  viewmodel/
    PortalViewModel.kt            Estado, validaciones, correo y cálculos
  ui/
    PortalApp.kt                  Navegación entre tres pestañas
    ProfileScreen.kt              Formulario académico
    TuitionScreen.kt              Curso, beca, Icetex y liquidación
    HistoryScreen.kt              LazyColumn y total acumulado
    Components.kt                 Componentes compartidos y formato monetario
    Theme.kt                      Material 3 claro/oscuro
    Accessibility.kt              Etiquetas accesibles de controles
```

Se comparte un `PortalViewModel` con alcance de Activity. `_uiState` es un `MutableStateFlow` privado; `uiState` se expone como `StateFlow` mediante `asStateFlow()`. Cada pantalla usa `collectAsStateWithLifecycle()` y envía eventos al ViewModel. Las vistas únicamente muestran datos, formatean cantidades/fechas y manejan navegación o apertura de menús.

Flujo: **usuario -> evento de UI -> ViewModel -> repositorio/estado -> StateFlow -> Compose**.

## Reglas implementadas

- Nombre: 2 a 80 caracteres; letras, espacios, apóstrofos o guiones. Se compactan los espacios.
- Código: 5 a 12 dígitos. Se conserva como texto para no perder ceros iniciales. Estos límites son una decisión del proyecto, no una norma oficial de UTS.
- Facultad obligatoria: Ingenierías o Ciencias Socioeconómicas.
- Correo simulado: nombre normalizado sin tildes, separado con puntos, código y `@uts.edu.co`. Ejemplo: `jose.perez.0012345@uts.edu.co`.
- El botón Liquidar solo se habilita con un perfil **guardado**. El método `liquidate()` también verifica el perfil.
- El borrador de un perfil editado no reemplaza el perfil guardado hasta pulsar Guardar perfil.
- Cada pulsación de Liquidar registra una nueva liquidación, incluso del mismo curso; el historial muestra las más recientes primero.
- Cada registro conserva una copia del perfil y los importes del momento; cambios posteriores no reescriben el historial.
- El ViewModel recalcula el total acumulado al agregar una matrícula y al cargar datos guardados.

### Tarifas simuladas y fórmula

| Curso | Base COP | Solo beca | Solo Icetex | Beca + Icetex |
|---|---:|---:|---:|---:|
| Inglés Técnico | 200.000 | 100.000 | 210.000 | 105.000 |
| Programación Web | 300.000 | 150.000 | 315.000 | 157.500 |
| Ciberseguridad | 400.000 | 200.000 | 420.000 | 210.000 |

```text
descuento = base * 50 % si tiene beca; si no, 0
subtotal = base - descuento
recargo = subtotal * 5 % si usa Icetex; si no, 0
total = subtotal + recargo
acumulado = suma de los totales registrados
```

El enunciado no define explícitamente el orden de los porcentajes; se aplica el recargo al monto efectivamente financiado después de la beca. Los precios elegidos producen pesos enteros; `Long` evita errores de punto flotante. Si se incorporan tarifas con centavos, debe definirse una política de redondeo y usar unidades menores o `BigDecimal`.

## Persistencia y alcance

`LocalStudentRepository` guarda un pequeño documento JSON en SharedPreferences privadas. `apply()` actualiza la memoria inmediatamente y escribe a disco de forma asíncrona. El ViewModel sobrevive a cambios de configuración y el repositorio permite recuperar el perfil/historial tras un inicio nuevo. Los borradores de formularios no se persisten después de cerrar el proceso.

Si la lectura falla, se informa y se bloquea la escritura para no sobrescribir los datos. Es una app de un solo perfil local; editarlo no equivale a iniciar sesión con otra cuenta. No hay autenticación, nube, pagos, integración real con Icetex ni validación oficial de matrícula. Para producción se necesitarían backend, autenticación, almacenamiento transaccional y revisión de protección de datos. Para un historial grande se recomienda Room; SharedPreferences basta para este ejercicio pequeño.

## Pruebas

`PortalViewModelTest` cubre 11 casos: bloqueo sin perfil, campos vacíos, normalización del correo, entradas inválidas, las 12 combinaciones de tarifas, acumulado e historial inmutable, recarga de datos, error de escritura, edición de perfil, rechazo de cursos ajenos y fallo de lectura.

`PortalFlowTest` contiene dos pruebas instrumentadas: recorrido de las tres pantallas con recreación de Activity y prueba de ida/vuelta del repositorio real en un archivo aislado. El recorrido guarda capturas en el almacenamiento externo privado de la app.

## Entrega y fuentes

El PDF de sustentación explica decisiones, estructura, demostración y resultados comprobados. Repositorio privado: https://github.com/msteran/servicios-estudiantiles-uts. Para que el profesor pueda abrirlo, el propietario debe darle acceso o decidir hacerlo público desde GitHub.

La guía se encuentra en [docs/Guia_Sustentacion_Mi_UTS.pdf](docs/Guia_Sustentacion_Mi_UTS.pdf). Se verificaron **11 pruebas unitarias y 2 instrumentadas**, además de lint (0 errores, 6 advertencias). Las capturas de las tres pantallas están en `docs/evidencias/`. Se comprobó también que el perfil y la matrícula permanecen después de detener y volver a abrir el proceso.

La ejecución de pruebas mediante Gradle puede desinstalar la aplicación al terminar. Para conservar capturas y datos de demostración, se reinstalaron los APK de app y pruebas y se ejecutó directamente `adb shell am instrument -w co.edu.uts.servicios.test/androidx.test.runner.AndroidJUnitRunner`. Ese recorrido también pasó. El emulador entregado contiene únicamente el ejemplo ficticio Ana Torres y una matrícula de $105.000.

- Enunciado suministrado: *Miniproyecto Final: Sistema Integrado de Servicios Estudiantiles UTS*, dos páginas.
- [Recomendaciones oficiales de arquitectura Android](https://developer.android.com/topic/architecture/recommendations)
- [Estado en Jetpack Compose](https://developer.android.com/develop/ui/compose/state)
- [Corrutinas y ciclo de vida](https://developer.android.com/topic/libraries/architecture/coroutines)
- [AndroidX Test: Espresso 3.7.0 corrige el acceso a InputManager](https://developer.android.com/jetpack/androidx/releases/test)
