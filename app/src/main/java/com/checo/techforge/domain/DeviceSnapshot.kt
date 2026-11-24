package com.checo.techforge.domain

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.StatFs
import java.util.Locale

data class DeviceSnapshot(
    val manufacturer: String,
    val model: String,
    val device: String,
    val androidVersion: String,
    val apiLevel: String,
    val securityPatch: String,
    val supportedAbis: String,
    val totalRam: String,
    val availableRam: String,
    val totalStorage: String,
    val availableStorage: String,
    val battery: String
)

fun readDeviceSnapshot(context: Context): DeviceSnapshot {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
    val statFs = StatFs(context.filesDir.absolutePath)
    val totalStorage = statFs.totalBytes
    val availableStorage = statFs.availableBytes
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val battery = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val batteryScale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val batteryPercent = if (battery >= 0 && batteryScale > 0) (battery * 100 / batteryScale) else -1

    return DeviceSnapshot(
        manufacturer = Build.MANUFACTURER.orUnknown(),
        model = Build.MODEL.orUnknown(),
        device = Build.DEVICE.orUnknown(),
        androidVersion = Build.VERSION.RELEASE.orUnknown(),
        apiLevel = Build.VERSION.SDK_INT.toString(),
        securityPatch = Build.VERSION.SECURITY_PATCH.orUnknown(),
        supportedAbis = Build.SUPPORTED_ABIS.joinToString(", ").ifBlank { "No disponible" },
        totalRam = humanBytes(memoryInfo.totalMem),
        availableRam = humanBytes(memoryInfo.availMem),
        totalStorage = humanBytes(totalStorage),
        availableStorage = humanBytes(availableStorage),
        battery = if (batteryPercent >= 0) "$batteryPercent%" else "No disponible"
    )
}

private fun String?.orUnknown(): String = this?.takeIf { it.isNotBlank() } ?: "No disponible"

fun humanBytes(bytes: Long): String {
    if (bytes < 0) return "No disponible"
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = -1
    do {
        value /= 1024.0
        index++
    } while (value >= 1024 && index < units.lastIndex)
    return String.format(Locale.getDefault(), "%.1f %s", value, units[index])
}
