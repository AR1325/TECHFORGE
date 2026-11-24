package com.checo.techforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.checo.techforge.data.*
import com.checo.techforge.domain.readDeviceSnapshot
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    projects: List<ProjectEntity>, records: List<LabRecordEntity>,
    onProjects: () -> Unit, onLab: () -> Unit, onTools: () -> Unit,
    onDevice: () -> Unit, onCommands: () -> Unit, onHash: () -> Unit, onLogs: () -> Unit
) {
    val context = LocalContext.current
    val device = remember { readDeviceSnapshot(context) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("TECHFORGE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text("Tu taller técnico portátil", style = MaterialTheme.typography.headlineSmall)
                    Text("Proyectos, registros y herramientas locales, sin depender de la nube.")
                    Spacer(Modifier.height(8.dp))
                    Text("${projects.count { it.status != "Completado" }} proyectos activos · ${records.count { it.status == "Abierto" }} registros abiertos")
                }
            }
        }
        item { Card(onClick = onDevice, Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Este dispositivo", style = MaterialTheme.typography.titleMedium); Text("${device.manufacturer} ${device.model}"); Text("Android ${device.androidVersion} · Batería ${device.battery}") } } }
        item { Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium) }
        item { Button(onClick = onProjects, Modifier.fillMaxWidth()) { Text("Proyectos") } }
        item { Button(onClick = onLab, Modifier.fillMaxWidth()) { Text("Bitácora técnica") } }
        item { Button(onClick = onTools, Modifier.fillMaxWidth()) { Text("Herramientas") } }
        item { OutlinedButton(onClick = onCommands, Modifier.fillMaxWidth()) { Text("Biblioteca de comandos") } }
        item { OutlinedButton(onClick = onHash, Modifier.fillMaxWidth()) { Text("Verificador SHA-256") } }
        item { OutlinedButton(onClick = onLogs, Modifier.fillMaxWidth()) { Text("Analizador de registros") } }
        item { Text("Actividad reciente", style = MaterialTheme.typography.titleMedium) }
        items(records.take(4), key = { it.id }) { Text("${it.title} · ${it.status}") }
        items(projects.take(4), key = { it.id }) { Text("${it.name} · ${it.status}") }
    }
}

@Composable
fun ProjectsScreen(projects: List<ProjectEntity>, repository: TechForgeRepository) {
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tus proyectos", style = MaterialTheme.typography.headlineSmall)
                Button(onClick = { showDialog = true }) { Text("Nuevo") }
            }
        }
        if (projects.isEmpty()) item { Text("Aún no hay proyectos. Crea uno para empezar.") }
        items(projects, key = { it.id }) { p ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(p.name, style = MaterialTheme.typography.titleMedium)
                    Text("${p.category} · ${p.status}")
                    if (p.description.isNotBlank()) Text(p.description)
                    Row {
                        TextButton(onClick = { scope.launch { repository.updateProjectStatus(p, if (p.status == "Completado") "Activo" else "Completado") } }) { Text(if (p.status == "Completado") "Reabrir" else "Completar") }
                        TextButton(onClick = { scope.launch { repository.deleteProject(p.id) } }) { Text("Eliminar") }
                    }
                }
            }
        }
    }
    if (showDialog) AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Nuevo proyecto") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Nombre") })
            OutlinedTextField(category, { category = it }, label = { Text("Categoría") })
            OutlinedTextField(description, { description = it }, label = { Text("Descripción") })
        } },
        confirmButton = { Button(onClick = { scope.launch { repository.addProject(name, description, category) }; name = ""; description = ""; showDialog = false }, enabled = name.isNotBlank()) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } }
    )
}

@Composable
fun LabScreen(records: List<LabRecordEntity>, repository: TechForgeRepository) {
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Diagnóstico") }
    var description by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bitácora técnica", style = MaterialTheme.typography.headlineSmall)
                Button(onClick = { showDialog = true }) { Text("Nuevo") }
            }
        }
        if (records.isEmpty()) item { Text("Todavía no hay entradas.") }
        items(records, key = { it.id }) { r ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(r.title, style = MaterialTheme.typography.titleMedium)
                    Text("${r.category} · ${r.status}")
                    Text(r.description)
                    Row {
                        TextButton(onClick = { scope.launch { repository.toggleLabRecord(r) } }) { Text(if (r.status == "Resuelto") "Reabrir" else "Resolver") }
                        TextButton(onClick = { scope.launch { repository.deleteLabRecord(r.id) } }) { Text("Eliminar") }
                    }
                }
            }
        }
    }
    if (showDialog) AlertDialog(
        onDismissRequest = { showDialog = false }, title = { Text("Nuevo registro") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("Título") })
            OutlinedTextField(category, { category = it }, label = { Text("Categoría") })
            OutlinedTextField(description, { description = it }, label = { Text("Detalle") }, minLines = 3)
        } },
        confirmButton = { Button(onClick = { scope.launch { repository.addLabRecord(title, category, description) }; title = ""; description = ""; showDialog = false }, enabled = title.isNotBlank()) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } }
    )
}

@Composable
fun ToolsScreen(onDevice: () -> Unit, onCommands: () -> Unit, onHash: () -> Unit, onLogs: () -> Unit, onBackup: () -> Unit, onInventory: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Caja de herramientas", style = MaterialTheme.typography.headlineSmall) }
        item { Button(onClick = onDevice, Modifier.fillMaxWidth()) { Text("Diagnóstico del dispositivo") } }
        item { Button(onClick = onCommands, Modifier.fillMaxWidth()) { Text("Biblioteca de comandos") } }
        item { Button(onClick = onHash, Modifier.fillMaxWidth()) { Text("Verificador SHA-256") } }
        item { Button(onClick = onLogs, Modifier.fillMaxWidth()) { Text("Analizador de registros") } }
        item { Button(onClick = onInventory, Modifier.fillMaxWidth()) { Text("Inventario de equipos") } }
        item { Button(onClick = onBackup, Modifier.fillMaxWidth()) { Text("Respaldos y restauración") } }
        item { Text("Las herramientas procesan los archivos localmente. Los comandos solo se copian, nunca se ejecutan.") }
    }
}

@Composable
fun DeviceInventoryScreen(devices: List<SavedDeviceEntity>, repository: TechForgeRepository) {
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var maker by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("Android") }
    var notes by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Inventario", style = MaterialTheme.typography.headlineSmall); Button(onClick = { showDialog = true }) { Text("Añadir") } } }
        items(devices, key = { it.id }) { d ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) {
                Text(d.name, style = MaterialTheme.typography.titleMedium)
                Text("${d.manufacturer} ${d.model} · ${d.platform}")
                if (d.notes.isNotBlank()) Text(d.notes)
                TextButton(onClick = { scope.launch { repository.deleteDevice(d.id) } }) { Text("Eliminar") }
            } }
        }
    }
    if (showDialog) AlertDialog(
        onDismissRequest = { showDialog = false }, title = { Text("Añadir equipo") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Nombre") })
            OutlinedTextField(maker, { maker = it }, label = { Text("Fabricante") })
            OutlinedTextField(model, { model = it }, label = { Text("Modelo") })
            OutlinedTextField(platform, { platform = it }, label = { Text("Plataforma") })
            OutlinedTextField(notes, { notes = it }, label = { Text("Notas") })
        } },
        confirmButton = { Button(onClick = { scope.launch { repository.addDevice(name, maker, model, platform, notes) }; showDialog = false }, enabled = name.isNotBlank()) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } }
    )
}

@Composable
fun CommandsScreen(commands: List<CommandEntity>) {
    var query by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Biblioteca de comandos", style = MaterialTheme.typography.headlineSmall); OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Buscar") }) }
        items(commands.filter { it.title.contains(query, true) || it.command.contains(query, true) }, key = { it.id }) { c ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text(c.title, style = MaterialTheme.typography.titleMedium); Text(c.platform + " · Riesgo " + c.risk); Text(c.command, color = MaterialTheme.colorScheme.primary); Text(c.explanation) } }
        }
    }
}

@Composable
fun DeviceInfoScreen() {
    val context = LocalContext.current
    val d = remember { readDeviceSnapshot(context) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("${d.manufacturer} ${d.model}", style = MaterialTheme.typography.headlineSmall) }
        item { InfoCard("Sistema", "Android ${d.androidVersion} · API ${d.apiLevel}\nParche: ${d.securityPatch}\nABI: ${d.supportedAbis}") }
        item { InfoCard("Memoria", "RAM total: ${d.totalRam}\nRAM disponible: ${d.availableRam}") }
        item { InfoCard("Almacenamiento de la app", "Total: ${d.totalStorage}\nDisponible: ${d.availableStorage}") }
        item { InfoCard("Batería", d.battery) }
        item { Text("El almacenamiento mostrado corresponde al volumen privado de la aplicación, no necesariamente a toda la capacidad física.") }
    }
}

@Composable
private fun InfoCard(title: String, value: String) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(value) } }
}
