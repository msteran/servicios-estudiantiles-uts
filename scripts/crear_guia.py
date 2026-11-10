"""Genera la guía de sustentación a partir del proyecto y sus evidencias reales."""
from pathlib import Path
from xml.etree import ElementTree as ET
from html import escape
import json
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak, Table, TableStyle, Image, Preformatted
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.enums import TA_LEFT

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'output/pdf/Guia_Sustentacion_Mi_UTS.pdf'
OUT.parent.mkdir(parents=True, exist_ok=True)
for name, file in [('Arial', 'arial.ttf'), ('ArialBold', 'arialbd.ttf'), ('ArialItalic', 'ariali.ttf')]:
    pdfmetrics.registerFont(TTFont(name, str(Path('C:/Windows/Fonts') / file)))
pdfmetrics.registerFontFamily('Arial', normal='Arial', bold='ArialBold', italic='ArialItalic', boldItalic='ArialBold')
GREEN = colors.HexColor('#006B50')
INK = colors.HexColor('#17392D')
GRAY = colors.HexColor('#52665C')
PALE = colors.HexColor('#EDF6EF')
styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name='BodyU', fontName='Arial', fontSize=10.4, leading=15.2, textColor=INK, spaceAfter=10))
styles.add(ParagraphStyle(name='SmallU', parent=styles['BodyU'], fontSize=8.4, leading=12, spaceAfter=7))
styles.add(ParagraphStyle(name='TitleU', fontName='ArialBold', fontSize=28, leading=33, textColor=INK, spaceAfter=18))
styles.add(ParagraphStyle(name='HeadU', fontName='ArialBold', fontSize=17, leading=22, textColor=GREEN, spaceAfter=12))
styles.add(ParagraphStyle(name='SubU', fontName='ArialBold', fontSize=11.5, leading=16, textColor=INK, spaceBefore=8, spaceAfter=7))
styles.add(ParagraphStyle(name='KickerU', fontName='ArialBold', fontSize=9, leading=14, textColor=GREEN, spaceAfter=15))
styles.add(ParagraphStyle(name='CodeU', fontName='Courier', fontSize=8, leading=12, backColor=PALE, borderPadding=12, spaceAfter=16))
story=[]

def p(text, style='BodyU'):
    return Paragraph(text, styles[style])
def add(text, style='BodyU'):
    story.append(p(text, style))
def head(num, title):
    if story: story.append(PageBreak())
    add(f'MI UTS / GUÍA DE SUSTENTACIÓN / {num}', 'KickerU')
    add(title, 'TitleU')
def step(n, title, detail, why):
    add(f'{n}. {title}', 'SubU'); add(detail); add(f'<b>Por qué:</b> {why}')
def box(text):
    t=Table([[p(text)]],colWidths=[491])
    t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,-1),PALE),('LEFTPADDING',(0,0),(-1,-1),14),('RIGHTPADDING',(0,0),(-1,-1),14),('TOPPADDING',(0,0),(-1,-1),12),('BOTTOMPADDING',(0,0),(-1,-1),5)]))
    story.append(t); story.append(Spacer(1,14))
def grid(rows, widths):
    table=Table([[p(str(cell),'SmallU') for cell in row] for row in rows], colWidths=widths, repeatRows=1, hAlign='LEFT')
    table.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),PALE),('VALIGN',(0,0),(-1,-1),'TOP'),('LINEBELOW',(0,0),(-1,0),1,GREEN),('LINEBELOW',(0,1),(-1,-1),.3,colors.HexColor('#D5E2DA')),('LEFTPADDING',(0,0),(-1,-1),8),('RIGHTPADDING',(0,0),(-1,-1),8),('TOPPADDING',(0,0),(-1,-1),8),('BOTTOMPADDING',(0,0),(-1,-1),5)]))
    story.append(table); story.append(Spacer(1,12))
def screenshot(name, texts):
    path=ROOT/'docs/evidencias'/name
    if not path.exists(): raise FileNotFoundError(path)
    from PIL import Image as PILImage
    with PILImage.open(path) as im: w,h=im.size
    image=Image(str(path), width=182, height=182*h/w)
    table=Table([[image,[p(t) for t in texts]]], colWidths=[202,289])
    table.setStyle(TableStyle([('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),0),('RIGHTPADDING',(0,0),(-1,-1),8)]))
    story.append(table);story.append(Spacer(1,12))
def code(text): story.append(Preformatted(text,styles['CodeU']))
def line(file, token):
    path=ROOT/'app/src/main/java/co/edu/uts/servicios'/file
    lines=path.read_text(encoding='utf-8').splitlines()
    return next(i for i,s in enumerate(lines,1) if token in s)

head('01', 'Sistema Integrado de\nServicios Estudiantiles'.replace('\n','<br/>'))
add('Proyecto final · Android nativo · Jetpack Compose · MVVM', 'HeadU')
story.append(Spacer(1,18))
box('<b>Idea central para explicar al profesor</b><br/><br/>Construimos un portal con tres módulos conectados por un mismo estado: el estudiante guarda su perfil, liquida cursos libres y consulta el total acumulado. El ViewModel decide las reglas; Compose representa sus resultados.')
grid([['Módulo','Qué demuestra'],['01 · Perfil académico','Validación de entradas y generación de correo institucional simulado.'],['02 · Matrícula libre','Selección de curso, beca del 50 %, recargo Icetex del 5 % y bloqueo sin perfil.'],['03 · Historial financiero','Lista de liquidaciones y total acumulado reactivo.']],[165,326])
add('Qué contiene esta guía', 'SubU')
add('Preparación del proyecto, explicación del código, decisiones de diseño, capturas del emulador, guion de demostración, pruebas ejecutadas y preguntas de sustentación.')
add('Alcance de la entrega', 'SubU')
add('Aplicación académica de un solo perfil, con almacenamiento local. Los precios son de ejemplo y no se conecta con UTS ni con Icetex. No realiza pagos reales.')
add('Preparado para la entrega del 9 de noviembre de 2026 (America/Bogota). Datos de prueba ficticios: Ana Torres, código 2026001.', 'SmallU')

head('02', 'Paso a paso: preparación')
step('1','Crear un proyecto independiente','El código se creó en la carpeta PARCIAL 3 CORTE APLICACIONES MOVILES, sin modificar el proyecto GimnasioUTS que ya estaba abierto. Se agregó el módulo Android <b>app</b>, su manifiesto y el Gradle Wrapper.','Mantener un proyecto reproducible y conservar los trabajos anteriores.')
step('2','Configurar herramientas compatibles','Se fijaron Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21, SDK 35 y Compose BOM 2024.12.01. La compilación usa JDK 17; Android Studio también puede usar JDK 21. Se redujo Gradle a dos trabajadores y 1 GB de heap.','El JDK 25 de Android Studio provocó incompatibilidad. Limitar los procesos ayudó en un equipo de 8 GB de RAM.')
step('3','Separar responsabilidades','Se crearon los tres paquetes obligatorios. MainActivity construye el repositorio y entrega el ViewModel a la aplicación.','Evitar cálculos y acceso al almacenamiento dentro de las pantallas.')
code('data/       Models.kt, StudentRepository.kt\n            LocalStudentRepository.kt\nviewmodel/  PortalViewModel.kt\nui/         ProfileScreen.kt, TuitionScreen.kt\n            HistoryScreen.kt, PortalApp.kt\n            Components.kt, Theme.kt, Accessibility.kt')
box('<b>Recorrido de una acción</b><br/>Usuario pulsa Guardar perfil → la UI envía un evento → el ViewModel valida → el repositorio guarda → cambia StateFlow → Compose muestra el nuevo estado.')
add('Para ejecutar otra vez: abrir esta carpeta en Android Studio, sincronizar Gradle, elegir la configuración app y el emulador Pixel_10a, y presionar Run.', 'SmallU')

head('03', 'Paso a paso: perfil')
screenshot('02-perfil.png',[
    '<b>4. Definir el modelo Student</b><br/>Contiene nombre, código, facultad y correo. El código es texto para conservar ceros iniciales.',
    '<b>5. Capturar y validar</b><br/>ProfileScreen muestra OutlinedTextField y un menú de facultad. saveProfile() exige nombre de 2 a 80 caracteres, código de 5 a 12 dígitos y facultad seleccionada.',
    '<b>6. Generar el correo</b><br/>El ViewModel recorta espacios, elimina tildes, usa minúsculas y une nombre, código y dominio. Ejemplo: José Pérez / 0012345 produce jose.perez.0012345@uts.edu.co.',
    '<b>Por qué se hace en el ViewModel</b><br/>La misma regla se puede probar sin dibujar la pantalla. La UI no decide si el perfil es válido.',
    '<b>Qué mostrar</b><br/>Guardar primero con campos vacíos. Después completar datos y mostrar el correo y la tarjeta de perfil activo.'
])
box('<b>Decisión explícita:</b> los límites del nombre y código son reglas del proyecto, no normas oficiales verificadas de UTS. Un borrador editado no cambia el perfil guardado hasta pulsar Guardar perfil.')
add(f'Código para abrir: viewmodel/PortalViewModel.kt, línea {line("viewmodel/PortalViewModel.kt","fun saveProfile")}; ui/ProfileScreen.kt.', 'SmallU')

head('04', 'Paso a paso: liquidación')
screenshot('03-liquidacion.png',[
    '<b>7. Preparar el catálogo</b><br/>Inglés Técnico: $200.000. Programación Web: $300.000. Ciberseguridad: $400.000. Son valores simulados en COP.',
    '<b>8. Conectar los controles</b><br/>El menú llama selectCourse(). El Checkbox llama setScholarship(). El Switch llama setIcetex(). Cada evento recalcula la cotización en el ViewModel.',
    '<b>9. Aplicar los porcentajes</b><br/>Primero se descuenta la beca. Después se calcula el recargo sobre el valor restante a financiar. Esta interpretación queda visible en la pantalla.',
    '<b>Ejemplo con ambos controles</b><br/>Base: $200.000.<br/>Beca: -$100.000.<br/>Recargo: $5.000.<br/><b>Total: $105.000.</b>',
    '<b>Por qué Long</b><br/>Con estas tarifas todos los resultados son pesos enteros. Se evitan imprecisiones de Float/Double. Para tarifas con centavos se necesitaría una regla de redondeo.'
])
code('descuento = si hay beca: base / 2; si no: 0\nrecargo = si hay Icetex: (base - descuento) * 5 / 100\ntotal = base - descuento + recargo')
add(f'Código para abrir: viewmodel/PortalViewModel.kt, línea {line("viewmodel/PortalViewModel.kt","private fun calculate")}; ui/TuitionScreen.kt.', 'SmallU')

head('05', 'Paso a paso: historial')
screenshot('04-historial.png',[
    '<b>10. Registrar una liquidación</b><br/>liquidate() verifica el perfil y crea Enrollment con identificador UUID, fecha, curso, condiciones, copia del estudiante e importes.',
    '<b>11. Consolidar el total</b><br/>El ViewModel suma quote.total de cada registro después de agregar uno nuevo. HistoryScreen solo muestra accumulatedTotal.',
    '<b>12. Dibujar una lista eficiente</b><br/>LazyColumn presenta los registros y usa el ID como clave estable. Los más recientes aparecen primero.',
    '<b>13. Conservar la información</b><br/>LocalStudentRepository guarda perfil e historial en SharedPreferences como un documento JSON. No requiere Internet.',
    '<b>Por qué guardar una copia</b><br/>Editar después el nombre o las condiciones del formulario no modifica lo que ya se había liquidado.'
])
box('<b>Persistencia y límites:</b> apply() escribe a disco de forma asíncrona. El perfil y las liquidaciones se recuperan al iniciar de nuevo; los borradores no se guardan tras cerrar el proceso. Para un historial grande o requisitos transaccionales, el siguiente paso sería Room.')
add(f'Código para abrir: viewmodel/PortalViewModel.kt, línea {line("viewmodel/PortalViewModel.kt","fun liquidate")}; data/LocalStudentRepository.kt; ui/HistoryScreen.kt.', 'SmallU')

head('06', 'Cómo defender la arquitectura')
add('MVVM: quién hace qué', 'HeadU')
grid([['Capa','Responsabilidad','No debe hacer'],['Model / data','Definir entidades, catálogo y almacenamiento local.','Dibujar pantallas.'],['ViewModel','Validar, generar correo, calcular importes y producir estado.','Referenciar vistas o controles Android.'],['View / ui','Mostrar el estado y enviar eventos. Formatear textos y fechas.','Calcular descuentos o leer SharedPreferences.']],[100,215,176])
add('StateFlow encapsulado y flujo unidireccional', 'SubU')
code('private val _uiState = MutableStateFlow(PortalUiState())\nval uiState: StateFlow<PortalUiState> = _uiState.asStateFlow()\n\n// Dentro de cada una de las tres pantallas:\nval state by viewModel.uiState.collectAsStateWithLifecycle()')
add('El estado mutable solo se cambia dentro del ViewModel. La pantalla observa una versión de solo lectura y llama funciones para solicitar cambios. collectAsStateWithLifecycle() adapta la colección al ciclo de vida de la pantalla, evitando mantenerla activa innecesariamente en segundo plano.')
add('Dos niveles de protección', 'SubU')
add('El botón Liquidar usa canLiquidate para estar deshabilitado sin perfil guardado. Además, liquidate() retorna si student es nulo. Así la regla también se cumple cuando el método se invoca directamente, como ocurre en las pruebas.')
add('Un ViewModel compartido para los tres módulos', 'SubU')
add('Las pantallas pertenecen al mismo portal y necesitan el mismo perfil e historial. Un ViewModel con alcance de Activity evita estados duplicados. rememberSaveable conserva la pestaña; el ViewModel conserva los datos durante una recreación de Activity.')
add('Material Design 3 y accesibilidad', 'SubU')
add('Se usaron Card, Column, Row, LazyColumn, OutlinedTextField, ExposedDropdownMenuBox, Checkbox y Switch. Hay tema claro/oscuro, desplazamiento vertical y etiquetas accesibles en los controles de beca e Icetex.')

head('07', 'Guion de demostración práctica')
add('Duración sugerida: 6 a 8 minutos. Usa datos ficticios.', 'HeadU')
grid([['Paso','Acción en el emulador','Qué explicar'],['1','Abrir Mi UTS en una instalación sin datos. Entrar a Historial.','El total empieza en cero y la lista tiene un estado vacío.'],['2','Ir a Matrícula y buscar Liquidar.','El botón está bloqueado porque todavía no existe un perfil guardado.'],['3','Ir a Perfil. Pulsar Guardar perfil sin completar campos.','Los errores provienen del ViewModel.'],['4','Escribir Ana Torres, 2026001 e Ingenierías. Guardar.','El correo se genera automáticamente y se habilita la liquidación.'],['5','Elegir Inglés Técnico. Activar beca e Icetex.','Base $200.000, descuento $100.000, recargo $5.000 y total $105.000.'],['6','Pulsar Liquidar y abrir Historial.','La nueva matrícula aparece y el acumulado se actualiza.'],['7','Liquidar Programación Web sin beca ni Icetex.','Se suman $300.000; el acumulado es $405.000 si solo hay estas dos matrículas.'],['8','Cerrar y abrir la aplicación. Mostrar los paquetes en Android Studio.','La persistencia conserva registros; las reglas están separadas de la UI.']],[45,220,226])
box('<b>Atención durante la sustentación:</b> el emulador de esta entrega ya contiene el registro ficticio creado por la prueba. Para demostrar el estado vacío usa otro emulador o borra únicamente los datos de Mi UTS, sabiendo que se perderán ese perfil e historial locales. No pulses varias veces Liquidar salvo que quieras generar varias matrículas.')
add('Frase de cierre sugerida', 'SubU')
add('“La interfaz reacciona a un estado que controla el ViewModel. Por eso puedo cambiar de pantalla sin perder los datos, probar las reglas sin una interfaz y reutilizar el almacenamiento mediante una interfaz de repositorio”.')

head('08', 'Verificación y resultados')
unit_files=list((ROOT/'app/build/test-results/testDebugUnitTest').glob('TEST-*.xml'))
device_files=list((ROOT/'app/build/outputs/androidTest-results/connected').rglob('TEST-*.xml'))
def totals(files):
    tests=failures=errors=0
    for f in files:
        root=ET.parse(f).getroot()
        tests+=int(root.get('tests',0)); failures+=int(root.get('failures',0)); errors+=int(root.get('errors',0))
    return tests,failures+errors
u,uf=totals(unit_files); d,df=totals(device_files)
if u < 11 or d < 2 or uf or df: raise RuntimeError(f'Pruebas incompletas o fallidas: unit={u}/{uf}, device={d}/{df}')
validation=json.loads((ROOT/'docs/validacion.json').read_text(encoding='utf-8'))
box(f'<b>Resultado comprobado</b><br/>APK debug compilado. {u} pruebas unitarias y {d} pruebas instrumentadas aprobadas, sin fallos. Análisis lint: {escape(validation["lint"])}. Ejecución en Pixel_10a con {escape(validation["android"])}.')
grid([['Evidencia','Qué verifica'],['PortalViewModelTest','Perfil obligatorio, campos vacíos/incorrectos, normalización del correo y ceros iniciales.'],['12 combinaciones financieras','Los tres cursos con ninguna condición, solo beca, solo Icetex y ambas.'],['Integridad de estado','Acumulado correcto, historial anterior inmutable, recarga desde repositorio y errores de lectura/escritura.'],['PortalFlowTest','Navegación real, bloqueo, validaciones, registro, controles, historial y recreación de Activity.'],['Repositorio local real','Serialización y lectura del perfil y liquidaciones en un archivo de prueba aislado.']],[151,340])
add('Cómo reproducir las verificaciones', 'SubU')
code('.\\gradlew.bat assembleDebug testDebugUnitTest lintDebug\n.\\gradlew.bat connectedDebugAndroidTest')
add('Configurar antes JAVA_HOME con JDK 17 o 21. Las pruebas instrumentadas requieren un emulador encendido y el recorrido completo espera datos iniciales vacíos. scripts/verificar.ps1 automatiza los comandos; usar -DeviceTests para incluir el dispositivo.')
add('Ubicación de reportes', 'SubU')
add('Pruebas unitarias: app/build/reports/tests/testDebugUnitTest/index.html.<br/>Pruebas Android: app/build/reports/androidTests/connected/debug/index.html.<br/>Lint: app/build/reports/lint-results-debug.html.<br/>Capturas: docs/evidencias/.', 'SmallU')
add('Límite de la verificación: se probó el emulador disponible, no una matriz de teléfonos físicos. No se evalúan servidores ni transacciones bancarias porque la aplicación es una simulación.', 'SmallU')

head('09', 'Preguntas para la sustentación')
for q,a in [
    ('¿Por qué MVVM?','Separa presentación, reglas y datos. Permite cambiar una pantalla sin reescribir el cálculo, y probar el ViewModel con un repositorio en memoria.'),
    ('¿Por qué el MutableStateFlow es privado?','Para impedir que la UI cambie el estado sin pasar por las reglas. StateFlow público permite observarlo.'),
    ('¿Por qué no calcular el total dentro de un Composable?','La UI puede recomponerse muchas veces. El cálculo es una regla de negocio centralizada y comprobable en el ViewModel.'),
    ('¿Por qué no se perdió el perfil al recrear la pantalla?','El ViewModel tiene alcance de Activity y sobrevive a cambios de configuración. Al iniciar un proceso nuevo, el repositorio recupera los datos guardados.'),
    ('¿Por qué el recargo es $5.000 y no $10.000 en el ejemplo?','Se aplica el 5 % a los $100.000 que quedan después de la beca. El enunciado no fijó el orden; la interpretación está documentada y visible.'),
    ('¿Qué mejorarías en una versión real?','Autenticación, backend institucional, base de datos Room, protección de datos y validación oficial de tarifas. Este ejercicio conserva un único perfil local y no procesa pagos.')]:
    add(q,'SubU');add(a)
add('Entrega del código', 'SubU')
add('El repositorio local incluye código, Gradle Wrapper, README, pruebas y evidencias. El APK de demostración se entrega aparte. '+escape(validation['repository']))
add('Fuentes y referencia técnica', 'SubU')
add('Enunciado entregado por el usuario: “Miniproyecto Final: Sistema Integrado de Servicios Estudiantiles UTS”. Las decisiones adicionales (precios, límites de campos, persistencia y orden de porcentajes) están identificadas en esta guía y en el README.', 'SmallU')
add('<link href="https://developer.android.com/topic/architecture/recommendations" color="#006B50">Android Developers: recomendaciones de arquitectura</link><br/><link href="https://developer.android.com/develop/ui/compose/state" color="#006B50">Android Developers: estado en Jetpack Compose</link><br/><link href="https://developer.android.com/topic/libraries/architecture/coroutines" color="#006B50">Android Developers: corrutinas y ciclo de vida</link>', 'SmallU')

def footer(canvas,doc):
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor('#D6E4DA'));canvas.line(52,44,543,44)
    canvas.setFont('Arial',8);canvas.setFillColor(GRAY)
    canvas.drawString(52,29,'MI UTS  /  Proyecto final de Aplicaciones Móviles')
    canvas.drawRightString(543,29,f'{doc.page}')
    canvas.restoreState()
doc=SimpleDocTemplate(str(OUT),pagesize=(595.28,841.89),leftMargin=52,rightMargin=52,topMargin=45,bottomMargin=60,
    title='Guía de sustentación - Sistema Integrado de Servicios Estudiantiles UTS',author='Proyecto Mi UTS')
doc.build(story,onFirstPage=footer,onLaterPages=footer)
print(OUT)
