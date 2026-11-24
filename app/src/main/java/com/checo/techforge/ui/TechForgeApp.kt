package com.checo.techforge.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checo.techforge.data.*
import com.checo.techforge.domain.readUtf8Limited
import com.checo.techforge.domain.sha256
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class Page(val title: String) {
    HOME("Centro de operaciones"), PROJECTS("Proyectos"), LAB("Bitácora técnica"), TOOLS("Herramientas"),
    DEVICE("Diagnóstico del dispositivo"), COMMANDS("Biblioteca de comandos"), HASH("Verificador SHA-256"),
    LOGS("Analizador de registros"), BACKUP("Respaldos"), ADD_DEVICE("Inventario de equipos")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechForgeApp(repository: TechForgeRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var page by rememberSaveable { mutableStateOf(Page.HOME) }
    var hashResult by remember { mutableStateOf<HashResultView?>(null) }
    var hashStatus by remember { mutableStateOf<String?>(null) }
    var logDocument by remember { mutableStateOf<LogDocumentView?>(null) }
    var logStatus by remember { mutableStateOf<String?>(null) }
    var backupStatus by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    val projects by repository.projects.collectAsStateWithLifecycle(emptyList())
    val records by repository.labRecords.collectAsStateWithLifecycle(emptyList())
    val devices by repository.savedDevices.collectAsStateWithLifecycle(emptyList())
    val commands by repository.commands.collectAsStateWithLifecycle(emptyList())
    LaunchedEffect(repository) { repository.seedCommandLibraryIfEmpty() }

    val hashPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            hashStatus = "Calculando SHA-256…"
            runCatching { computeHash(context, uri) }
                .onSuccess { hashResult = it; hashStatus = "Hash calculado correctamente." }
                .onFailure { hashStatus = "No se pudo leer el archivo: ${it.message ?: "error desconocido"}" }
            busy = false
        }
    }
    val logPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            runCatching { readLogDocument(context, uri) }
                .onSuccess { logDocument = it; logStatus = "Archivo abierto correctamente."; page = Page.LOGS }
                .onFailure { logDocument = null; logStatus = "No se pudo abrir el registro: ${it.message ?: "error desconocido"}"; page = Page.LOGS }
            busy = false
        }
    }
    val backupWriter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            busy = true
            backupStatus = "Generando respaldo…"
            runCatching {
                val json = withContext(Dispatchers.IO) { repository.exportBackup() }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(json) }
                        ?: error("No se pudo abrir el destino de escritura.")
                }
            }.onSuccess { backupStatus = "Respaldo exportado correctamente." }
                .onFailure { backupStatus = "Error al exportar: ${it.message}" }
            busy = false
        }
    }
    val backupReader = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            backupStatus = "Validando respaldo…"
            runCatching {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        readUtf8Limited(input, 15 * 1024 * 1024).also {
                            require(!it.truncated) { "El archivo supera el límite de 15 MB." }
                        }.text
                    } ?: error("No se pudo abrir el archivo.")
                }
                withContext(Dispatchers.IO) { repository.restoreBackup(json) }
            }.onSuccess { backupStatus = "Respaldo restaurado. Los datos locales fueron reemplazados." }
                .onFailure { backupStatus = "Respaldo rechazado: ${it.message ?: "archivo inválido"}" }
            busy = false
        }
    }

    val mainPages = setOf(Page.HOME, Page.PROJECTS, Page.LAB, Page.TOOLS)
    BackHandler(enabled = page !in mainPages) { page = Page.TOOLS }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text("TECHFORGE", style = MaterialTheme.typography.titleMedium)
                        Text(page.title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    if (page !in mainPages) IconButton(onClick = { page = Page.TOOLS }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (page in mainPages) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = page == Page.HOME, onClick = { page = Page.HOME }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Inicio") })
                NavigationBarItem(selected = page == Page.PROJECTS, onClick = { page = Page.PROJECTS }, icon = { Icon(Icons.Default.Folder, null) }, label = { Text("Proyectos") })
                NavigationBarItem(selected = page == Page.LAB, onClick = { page = Page.LAB }, icon = { Icon(Icons.Default.Science, null) }, label = { Text("Bitácora") })
                NavigationBarItem(selected = page == Page.TOOLS, onClick = { page = Page.TOOLS }, icon = { Icon(Icons.Default.Build, null) }, label = { Text("Herramientas") })
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (page) {
                Page.HOME -> DashboardScreen(projects, records, { page = Page.PROJECTS }, { page = Page.LAB }, { page = Page.TOOLS }, { page = Page.DEVICE }, { page = Page.COMMANDS }, { page = Page.HASH }, { logPicker.launch(arrayOf("text/*", "application/json", "application/xml", "*/*")) })
                Page.PROJECTS -> ProjectsScreen(projects, repository)
                Page.LAB -> LabScreen(records, repository)
                Page.TOOLS -> ToolsScreen({ page = Page.DEVICE }, { page = Page.COMMANDS }, { page = Page.HASH }, { logPicker.launch(arrayOf("text/*", "application/json", "application/xml", "*/*")) }, { page = Page.BACKUP }, { page = Page.ADD_DEVICE })
                Page.DEVICE -> DeviceInfoScreen()
                Page.COMMANDS -> CommandsScreen(commands)
                Page.HASH -> HashScreen(hashResult, hashStatus, busy) { hashPicker.launch(arrayOf("*/*")) }
                Page.LOGS -> LogAnalyzerScreen(logDocument, logStatus, busy) { logPicker.launch(arrayOf("text/*", "application/json", "application/xml", "*/*")) }
                Page.BACKUP -> BackupScreen(backupStatus, busy, { backupWriter.launch("TECHFORGE-respaldo.json") }, { backupReader.launch(arrayOf("application/json", "text/*", "*/*")) })
                Page.ADD_DEVICE -> DeviceInventoryScreen(devices, repository)
            }
        }
    }
}

private suspend fun computeHash(context: Context, uri: Uri): HashResultView = withContext(Dispatchers.IO) {
    val result = context.contentResolver.openInputStream(uri)?.use(::sha256) ?: error("El proveedor no permitió leer el archivo.")
    HashResultView(queryDisplayName(context, uri), result.bytesRead, result.sha256)
}
private suspend fun readLogDocument(context: Context, uri: Uri): LogDocumentView = withContext(Dispatchers.IO) {
    val result = context.contentResolver.openInputStream(uri)?.use { readUtf8Limited(it, 2 * 1024 * 1024) } ?: error("El proveedor no permitió leer el archivo.")
    LogDocumentView(queryDisplayName(context, uri), result.text, result.truncated)
}
private fun queryDisplayName(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) return cursor.getString(index)
        }
    }
    return uri.lastPathSegment ?: "archivo"
}
