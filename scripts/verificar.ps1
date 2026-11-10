param([string]$JavaHome = 'C:\Program Files\Java\jdk-17', [switch]$DeviceTests)
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = $JavaHome
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    & .\gradlew.bat assembleDebug testDebugUnitTest lintDebug --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación o la verificación local.' }
    if ($DeviceTests) {
        & .\gradlew.bat connectedDebugAndroidTest --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas del dispositivo.' }
    }
} finally { Pop-Location }
