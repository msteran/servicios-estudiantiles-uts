package co.edu.uts.servicios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uts.servicios.viewmodel.PortalViewModel

@Composable fun TuitionScreen(viewModel: PortalViewModel, onProfile: () -> Unit, onHistory: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().testTag("tuition_list"), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { PageTitle("02 / SIGUE APRENDIENDO", "Matrícula libre", "Elige tu próximo curso y conoce el valor antes de liquidar.") }
        state.storageError?.let { item { Notice(it) } }
        if (!state.canLiquidate) item {
            Panel {
                Text("Primero, completa tu perfil", fontWeight = FontWeight.Bold)
                Text("Guarda tus datos académicos para habilitar la liquidación.")
                TextButton(onClick = onProfile) { Text("Ir a mi perfil") }
            }
        }
        item {
            Panel {
                Text("Tu próximo curso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Selector("Curso libre", state.selectedCourse.title, state.courses, { it.title }, onSelect = viewModel::selectCourse)
                Text(state.selectedCourse.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AmountRow("Valor base", money(state.quote.base), strong = true)
            }
        }
        item {
            Panel {
                Text("Beneficios y financiación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.scholarship, onCheckedChange = viewModel::setScholarship,
                        modifier = Modifier.semanticsLabel("Beca de Excelencia UTS"))
                    Column(Modifier.weight(1f)) {
                        Text("Beca de Excelencia UTS", fontWeight = FontWeight.Medium)
                        Text("50 % de descuento", style = MaterialTheme.typography.bodySmall)
                    }
                }
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("Financiar con Icetex", fontWeight = FontWeight.Medium)
                        Text("5 % sobre el valor después de la beca", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = state.icetex, onCheckedChange = viewModel::setIcetex,
                        modifier = Modifier.semanticsLabel("Financiar con Icetex"))
                }
            }
        }
        item {
            Panel {
                Text("Resumen de liquidación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AmountRow("Costo del curso", money(state.quote.base))
                AmountRow("Beca de excelencia", "− ${money(state.quote.discount)}")
                AmountRow("Recargo Icetex", "+ ${money(state.quote.surcharge)}")
                HorizontalDivider()
                Text("TOTAL A PAGAR", style = MaterialTheme.typography.labelMedium)
                Text(money(state.quote.total), style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Button(onClick = viewModel::liquidate, enabled = state.canLiquidate && state.storageError == null,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Liquidar") }
                Text("Cada liquidación agrega un registro al historial. Valores de ejemplo en COP; no se realizan pagos reales.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        state.message?.let { item { Notice(it) } }
        item { OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text("Ver historial") } }
    }
}
