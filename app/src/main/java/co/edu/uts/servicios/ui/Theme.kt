package co.edu.uts.servicios.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006B50), onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F5E5), onPrimaryContainer = Color(0xFF003D2C),
    secondary = Color(0xFF55634A), secondaryContainer = Color(0xFFE5EBCF),
    background = Color(0xFFF6F8F4), surface = Color(0xFFF6F8F4),
    surfaceContainer = Color(0xFFEDF1EB), surfaceContainerLow = Color.White,
    onSurface = Color(0xFF172B23), onSurfaceVariant = Color(0xFF52635B)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF80D9B4), primaryContainer = Color(0xFF00513C),
    background = Color(0xFF101B16), surface = Color(0xFF101B16)
)
@Composable fun UtsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, content = content)
}
