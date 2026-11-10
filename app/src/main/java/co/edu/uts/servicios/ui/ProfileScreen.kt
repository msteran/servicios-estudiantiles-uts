package co.edu.uts.servicios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uts.servicios.data.Faculty
import co.edu.uts.servicios.viewmodel.PortalViewModel

@Composable fun ProfileScreen(viewModel: PortalViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().testTag("profile_list"), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { PageTitle("01 / TU INFORMACIÓN", "Perfil académico", "Tu punto de partida para acceder a los servicios estudiantiles.") }
        state.storageError?.let { item { Notice(it) } }
        item {
            Panel {
                Text("Datos del estudiante", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = state.name, onValueChange = viewModel::changeName,
                    label = { Text("Nombre completo") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    isError = state.nameError != null, supportingText = { Text(state.nameError ?: "Tal como aparece en tu registro académico.") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.code, onValueChange = viewModel::changeCode,
                    label = { Text("Código estudiantil") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.codeError != null, supportingText = { Text(state.codeError ?: "Entre 5 y 12 dígitos.") },
                    modifier = Modifier.fillMaxWidth())
                Selector("Facultad", state.faculty?.label.orEmpty(), Faculty.entries,
                    { it.label }, state.facultyError, viewModel::changeFaculty)
                Button(onClick = viewModel::saveProfile, enabled = state.storageError == null,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Guardar perfil") }
            }
        }
        state.message?.let { item { Notice(it) } }
        state.student?.let { student ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                        Text("Tu perfil está activo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(student.name)
                        Text("${student.code} · ${student.faculty.label}", style = MaterialTheme.typography.bodyMedium)
                        HorizontalDivider()
                        Text("CORREO INSTITUCIONAL SIMULADO", style = MaterialTheme.typography.labelSmall)
                        Text(student.email, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        item { Text("Proyecto académico · Portal de simulación UTS", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
