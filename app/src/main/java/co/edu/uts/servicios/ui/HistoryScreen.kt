package co.edu.uts.servicios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uts.servicios.viewmodel.PortalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable fun HistoryScreen(viewModel: PortalViewModel, onTuition: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().testTag("history_list"), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { PageTitle("03 / TODO EN UN LUGAR", "Tu historial", "Consulta tus liquidaciones y el consolidado financiero.") }
        state.storageError?.let { item { Notice(it) } }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("TOTAL ACUMULADO A PAGAR", style = MaterialTheme.typography.labelLarge)
                    Text(money(state.accumulatedTotal), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("${state.enrollments.size} liquidaciones registradas · COP", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (state.enrollments.isEmpty()) item {
            Panel {
                Icon(Icons.Outlined.List, contentDescription = null, modifier = Modifier.size(40.dp))
                Text("Tu siguiente paso comienza aquí", style = MaterialTheme.typography.titleLarge)
                Text("Aún no tienes liquidaciones. Cuando registres un curso, aparecerá en este espacio.")
                Button(onClick = onTuition) { Text("Explorar cursos") }
            }
        } else {
            item { Text("Movimientos recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(state.enrollments, key = { it.id }) { row ->
                Panel {
                    Text(row.courseTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(SimpleDateFormat("dd MMM yyyy · HH:mm", Locale("es", "CO")).format(Date(row.createdAt)),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${row.student.name} · ${row.student.code}", style = MaterialTheme.typography.bodySmall)
                    AmountRow("Base", money(row.quote.base))
                    AmountRow("Beca${if (row.scholarship) " (50 %)" else ""}", "− ${money(row.quote.discount)}")
                    AmountRow("Icetex${if (row.icetex) " (5 %)" else ""}", "+ ${money(row.quote.surcharge)}")
                    HorizontalDivider()
                    AmountRow("Total liquidado", money(row.quote.total), strong = true)
                    Text("Referencia ${row.id.take(8).uppercase()}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
