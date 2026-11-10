package co.edu.uts.servicios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.edu.uts.servicios.viewmodel.PortalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PortalApp(viewModel: PortalViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val labels = listOf("Perfil", "Matrícula", "Historial")
    val icons = listOf(Icons.Outlined.Person, Icons.Outlined.AccountBox, Icons.Outlined.List)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mi UTS", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text("/ Estudiantes", color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium)
                }
            })
        },
        bottomBar = {
            NavigationBar {
                labels.forEachIndexed { index, label ->
                    NavigationBarItem(selected = tab == index, onClick = { tab = index },
                        icon = { Icon(icons[index], contentDescription = null) }, label = { Text(label) })
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding()) {
            when (tab) {
                0 -> ProfileScreen(viewModel)
                1 -> TuitionScreen(viewModel, onProfile = { tab = 0 }, onHistory = { tab = 2 })
                2 -> HistoryScreen(viewModel, onTuition = { tab = 1 })
            }
        }
    }
}
