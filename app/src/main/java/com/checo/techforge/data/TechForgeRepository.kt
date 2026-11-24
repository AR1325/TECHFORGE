package com.checo.techforge.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

class TechForgeRepository(private val database: TechForgeDatabase) {
    val projects: Flow<List<ProjectEntity>> = database.projectDao().observeAll()
    val labRecords: Flow<List<LabRecordEntity>> = database.labRecordDao().observeAll()
    val savedDevices: Flow<List<SavedDeviceEntity>> = database.savedDeviceDao().observeAll()
    val commands: Flow<List<CommandEntity>> = database.commandDao().observeAll()

    suspend fun addProject(name: String, description: String, category: String) {
        val now = System.currentTimeMillis()
        database.projectDao().upsert(ProjectEntity(newId(), name.trim(), description.trim(), category, "Activo", now, now))
    }
    suspend fun updateProjectStatus(project: ProjectEntity, status: String) {
        database.projectDao().upsert(project.copy(status = status, updatedAt = System.currentTimeMillis()))
    }
    suspend fun deleteProject(id: String) = database.projectDao().delete(id)

    suspend fun addLabRecord(title: String, category: String, description: String) {
        val now = System.currentTimeMillis()
        database.labRecordDao().upsert(LabRecordEntity(newId(), title.trim(), category, description.trim(), "Abierto", now, now))
    }
    suspend fun toggleLabRecord(record: LabRecordEntity) {
        val next = if (record.status == "Resuelto") "Abierto" else "Resuelto"
        database.labRecordDao().upsert(record.copy(status = next, updatedAt = System.currentTimeMillis()))
    }
    suspend fun deleteLabRecord(id: String) = database.labRecordDao().delete(id)

    suspend fun addDevice(name: String, manufacturer: String, model: String, platform: String, notes: String) {
        database.savedDeviceDao().upsert(SavedDeviceEntity(newId(), name.trim(), manufacturer.trim(), model.trim(), platform, notes.trim(), System.currentTimeMillis()))
    }
    suspend fun deleteDevice(id: String) = database.savedDeviceDao().delete(id)

    suspend fun seedCommandLibraryIfEmpty() {
        if (database.commandDao().getAll().isEmpty()) database.commandDao().insertAll(defaultCommands())
    }

    suspend fun exportBackup(): String {
        val root = JSONObject()
            .put("app", "TECHFORGE").put("schemaVersion", 1).put("exportedAt", Instant.now().toString())
            .put("projects", JSONArray().apply { database.projectDao().getAll().forEach { put(it.toJson()) } })
            .put("labRecords", JSONArray().apply { database.labRecordDao().getAll().forEach { put(it.toJson()) } })
            .put("savedDevices", JSONArray().apply { database.savedDeviceDao().getAll().forEach { put(it.toJson()) } })
            .put("commands", JSONArray().apply { database.commandDao().getAll().forEach { put(it.toJson()) } })
        return root.toString(2)
    }

    suspend fun restoreBackup(json: String) {
        val root = JSONObject(json)
        require(root.optString("app") == "TECHFORGE") { "El archivo no pertenece a TECHFORGE." }
        require(root.optInt("schemaVersion", -1) == 1) { "Versión de respaldo no compatible." }
        require(root.has("projects") && root.has("labRecords") && root.has("savedDevices") && root.has("commands")) {
            "El respaldo está incompleto. No se modificó la base de datos."
        }
        val projects = root.getJSONArray("projects").mapObjects { it.toProject() }
        val records = root.getJSONArray("labRecords").mapObjects { it.toLabRecord() }
        val devices = root.getJSONArray("savedDevices").mapObjects { it.toSavedDevice() }
        val commands = root.getJSONArray("commands").mapObjects { it.toCommand() }
        require(projects.size + records.size + devices.size + commands.size <= 20_000) { "El respaldo contiene demasiados registros." }
        database.withTransaction {
            database.projectDao().clear(); database.labRecordDao().clear()
            database.savedDeviceDao().clear(); database.commandDao().clear()
            database.projectDao().insertAll(projects); database.labRecordDao().insertAll(records)
            database.savedDeviceDao().insertAll(devices); database.commandDao().insertAll(commands)
        }
    }

    private fun defaultCommands() = listOf(
        CommandEntity("adb-devices", "Dispositivos conectados", "ADB", "Conexión", "adb devices -l", "Lista dispositivos ADB conectados.", "Bajo"),
        CommandEntity("adb-android-version", "Versión de Android", "ADB", "Sistema", "adb shell getprop ro.build.version.release", "Consulta la versión de Android.", "Bajo"),
        CommandEntity("adb-build", "Información de compilación", "ADB", "Sistema", "adb shell getprop ro.build.display.id", "Muestra el identificador de compilación.", "Bajo"),
        CommandEntity("adb-battery", "Estado de batería", "ADB", "Diagnóstico", "adb shell dumpsys battery", "Consulta información de batería.", "Bajo"),
        CommandEntity("adb-storage", "Espacio de almacenamiento", "ADB", "Diagnóstico", "adb shell df -h /data", "Consulta espacio usado y disponible.", "Bajo"),
        CommandEntity("adb-logcat", "Últimas líneas de logcat", "ADB", "Registros", "adb logcat -d -t 1000", "Exporta logs que pueden contener datos privados.", "Medio"),
        CommandEntity("linux-inxi", "Resumen de hardware", "Linux", "Hardware", "inxi -Fxxxz", "Genera resumen de hardware.", "Bajo"),
        CommandEntity("linux-block", "Discos y particiones", "Linux", "Almacenamiento", "lsblk -f", "Lista dispositivos de bloques.", "Bajo"),
        CommandEntity("linux-space", "Uso de discos", "Linux", "Almacenamiento", "df -hT", "Muestra espacio de sistemas de archivos.", "Bajo"),
        CommandEntity("linux-memory", "Memoria disponible", "Linux", "Rendimiento", "free -h", "Muestra RAM y swap.", "Bajo"),
        CommandEntity("ps-info", "Información de Windows", "PowerShell", "Sistema", "Get-ComputerInfo | Select-Object OsName,OsVersion,CsName", "Consulta información del sistema.", "Bajo"),
        CommandEntity("ps-disk", "Unidades de Windows", "PowerShell", "Almacenamiento", "Get-Volume | Select-Object DriveLetter,FileSystem,Size,SizeRemaining", "Lista volúmenes y espacio disponible.", "Bajo")
    )

    private fun ProjectEntity.toJson() = JSONObject().put("id", id).put("name", name).put("description", description).put("category", category).put("status", status).put("createdAt", createdAt).put("updatedAt", updatedAt)
    private fun LabRecordEntity.toJson() = JSONObject().put("id", id).put("title", title).put("category", category).put("description", description).put("status", status).put("createdAt", createdAt).put("updatedAt", updatedAt)
    private fun SavedDeviceEntity.toJson() = JSONObject().put("id", id).put("name", name).put("manufacturer", manufacturer).put("model", model).put("platform", platform).put("notes", notes).put("createdAt", createdAt)
    private fun CommandEntity.toJson() = JSONObject().put("id", id).put("title", title).put("platform", platform).put("category", category).put("command", command).put("explanation", explanation).put("risk", risk)

    private fun JSONObject.toProject() = ProjectEntity(getString("id"), getString("name"), optString("description"), optString("category", "General"), optString("status", "Activo"), optLong("createdAt"), optLong("updatedAt"))
    private fun JSONObject.toLabRecord() = LabRecordEntity(getString("id"), getString("title"), optString("category", "General"), optString("description"), optString("status", "Abierto"), optLong("createdAt"), optLong("updatedAt"))
    private fun JSONObject.toSavedDevice() = SavedDeviceEntity(getString("id"), getString("name"), optString("manufacturer"), optString("model"), optString("platform", "Otro"), optString("notes"), optLong("createdAt"))
    private fun JSONObject.toCommand() = CommandEntity(getString("id"), getString("title"), getString("platform"), optString("category", "General"), getString("command"), optString("explanation"), optString("risk", "Bajo"))
    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> = (0 until length()).map { transform(getJSONObject(it)) }
    private fun newId(): String = java.util.UUID.randomUUID().toString()
}
