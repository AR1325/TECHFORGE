package com.checo.techforge.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.checo.techforge.domain.humanBytes

@Composable
fun HashScreen(result: HashResultView?, status: String?, busy: Boolean, onPickFile: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(17.dp)) {
                    Icon(Icons.Default.Fingerprint, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Huella SHA-256", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text("Selecciona un archivo. El cálculo se realiza localmente, por bloques, sin cargarlo a ningún servidor.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Button(onClick = onPickFile, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.FileOpen, null); Spacer(Modifier.width(8.dp)); Text(if (busy) "Procesando…" else "Seleccionar archivo") } }
        if (status != null) item { Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        if (result != null) item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(result.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Tamaño: ${humanBytes(result.bytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text("SHA-256", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        SelectionContainer { Text(result.digest, modifier = Modifier.padding(12.dp), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall) }
                    }
                    OutlinedButton(onClick = { clipboard.setText(AnnotatedString(result.digest)) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(7.dp)); Text("Copiar hash") }
                }
            }
        }
        item { Text("Un hash coincidente indica que el contenido produce la misma huella SHA-256; no demuestra por sí solo que el archivo sea seguro o auténtico.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
    }
}
data class HashResultView(val name: String, val bytes: Long, val digest: String)

@Composable
fun LogAnalyzerScreen(log: LogDocumentView?, status: String?, busy: Boolean, onPickFile: () -> Unit) {
    var query by remember(log?.name) { mutableStateOf("") }
    val allLines = remember(log?.content) { log?.content?.lines().orEmpty() }
    val matches = remember(allLines, query) { if (query.isBlank()) allLines else allLines.filter { it.contains(query, ignoreCase = true) } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item {
            Text("Analizador de registros", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Abre un archivo de texto o log. El contenido se procesa en el teléfono y no se guarda automáticamente.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        item { Button(onClick = onPickFile, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.FileOpen, null); Spacer(Modifier.width(7.dp)); Text(if (busy) "Abriendo…" else "Seleccionar registro") } }
        if (status != null) item { Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        if (log != null) {
            item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(13.dp)) { Text(log.name, fontWeight = FontWeight.SemiBold); Text("${allLines.size} líneas · ${matches.size} coincidencias${if (log.truncated) " · vista limitada a 2 MB" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
            item { OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Filtrar líneas") }, singleLine = true) }
            item {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SelectionContainer { Text(matches.take(1500).joinToString("\n").ifBlank { "No hay líneas que coincidan con la búsqueda." }, modifier = Modifier.padding(12.dp).horizontalScroll(rememberScrollState()), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface) }
                }
                if (matches.size > 1500) Text("Se muestran las primeras 1,500 coincidencias.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else item { UtilityEmptyState("Todavía no hay un archivo", "Selecciona un registro .txt o .log para empezar a buscar texto, errores o palabras clave.") }
    }
}
data class LogDocumentView(val name: String, val content: String, val truncated: Boolean)

@Composable
fun BackupScreen(status: String?, busy: Boolean, onExport: () -> Unit, onImport: () -> Unit) {
    var showConfirmation by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(17.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Icon(Icons.Default.SaveAlt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Tus datos son tuyos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text("Exporta una copia JSON para conservar proyectos, bitácora, inventario y biblioteca de comandos.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Button(onClick = onExport, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.SaveAlt, null); Spacer(Modifier.width(8.dp)); Text("Exportar respaldo JSON") } }
        item { OutlinedButton(onClick = { showConfirmation = true }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Restore, null); Spacer(Modifier.width(8.dp)); Text("Restaurar desde JSON") } }
        if (busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (status != null) item { Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(11.dp), modifier = Modifier.fillMaxWidth()) { Text(status, modifier = Modifier.padding(13.dp), style = MaterialTheme.typography.bodySmall) } }
        item { Text("La restauración reemplaza los datos locales actuales. Exporta un respaldo antes de importar otro archivo. TECHFORGE valida el formato y realiza el reemplazo en una sola transacción; si el archivo no es válido, la base permanece intacta.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
    }
    if (showConfirmation) AlertDialog(
        onDismissRequest = { showConfirmation = false },
        title = { Text("¿Reemplazar datos locales?") },
        text = { Text("La importación sustituirá los proyectos, la bitácora, el inventario y los comandos actuales. Conviene exportar primero una copia de seguridad.") },
        confirmButton = { Button(onClick = { showConfirmation = false; onImport() }) { Text("Continuar") } },
        dismissButton = { TextButton(onClick = { showConfirmation = false }) { Text("Cancelar") } }
    )
}
@Composable
private fun UtilityEmptyState(title: String, description: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}
