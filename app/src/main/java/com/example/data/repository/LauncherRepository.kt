package com.example.data.repository

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import com.example.data.local.AppDao
import com.example.data.local.CustomIconOverrideEntity
import com.example.data.local.FloppaChatMessageEntity
import com.example.data.local.FloppaSettingsEntity
import com.example.data.local.PinnedAppEntity
import com.example.data.model.AppInfo
import com.example.data.model.CustomIconType
import com.example.data.model.FloppaThemeType
import com.example.data.model.MemeTransitionType
import com.example.data.model.SystemStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LauncherRepository(
    private val context: Context,
    private val dao: AppDao
) {
    private val packageManager: PackageManager = context.packageManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private data class CachedRawApp(
        val packageName: String,
        val activityName: String,
        val defaultLabel: String,
        val iconDrawable: Drawable?,
        val isSystemApp: Boolean
    )

    @Volatile
    private var cachedRawApps: List<CachedRawApp>? = null

    private fun getRawApps(): List<CachedRawApp> {
        val cached = cachedRawApps
        if (cached != null) return cached

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        val list = resolveInfos.mapNotNull { resolveInfo ->
            val pkgName = resolveInfo.activityInfo.packageName
            if (pkgName == context.packageName) return@mapNotNull null
            val label = resolveInfo.loadLabel(packageManager).toString()
            val icon = try {
                resolveInfo.loadIcon(packageManager)
            } catch (_: Exception) {
                null
            }
            val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            CachedRawApp(
                packageName = pkgName,
                activityName = resolveInfo.activityInfo.name,
                defaultLabel = label,
                iconDrawable = icon,
                isSystemApp = isSystem
            )
        }
        cachedRawApps = list
        return list
    }

    // --- Query installed apps combined with Room overrides ---
    fun getInstalledAppsFlow(): Flow<List<AppInfo>> {
        return combine(
            dao.getAllPinnedApps(),
            dao.getAllCustomIcons()
        ) { pinnedList, customIconList ->
            val pinnedMap = pinnedList.associateBy { it.packageName }
            val customIconMap = customIconList.associateBy { it.packageName }
            val rawApps = getRawApps()

            rawApps.map { raw ->
                val pinnedEntity = pinnedMap[raw.packageName]
                val customIconEntity = customIconMap[raw.packageName]

                val customType = when (customIconEntity?.customIconType) {
                    "BUILTIN_MEME" -> CustomIconType.BUILTIN_MEME
                    "CUSTOM_IMAGE_URI" -> CustomIconType.CUSTOM_IMAGE_URI
                    else -> CustomIconType.NONE
                }

                AppInfo(
                    packageName = raw.packageName,
                    activityName = raw.activityName,
                    label = pinnedEntity?.customLabel ?: raw.defaultLabel,
                    iconDrawable = raw.iconDrawable,
                    isSystemApp = raw.isSystemApp,
                    isPinnedToHome = pinnedEntity != null && !pinnedEntity.isDock,
                    isDockApp = pinnedEntity?.isDock == true,
                    customIconType = customType,
                    customIconRef = customIconEntity?.customIconRef
                )
            }.sortedBy { it.label.lowercase() }
        }.flowOn(Dispatchers.IO)
    }

    // --- Pinned / Dock Apps ---
    suspend fun pinApp(packageName: String, isDock: Boolean, customLabel: String? = null) {
        withContext(Dispatchers.IO) {
            dao.insertPinnedApp(
                PinnedAppEntity(
                    packageName = packageName,
                    position = (System.currentTimeMillis() % 10000).toInt(),
                    isDock = isDock,
                    customLabel = customLabel
                )
            )
        }
    }

    suspend fun unpinApp(packageName: String) {
        withContext(Dispatchers.IO) {
            dao.deletePinnedApp(packageName)
        }
    }

    // --- Custom Icon Overrides ---
    suspend fun setAppIconOverride(packageName: String, type: CustomIconType, ref: String) {
        withContext(Dispatchers.IO) {
            val typeStr = when (type) {
                CustomIconType.BUILTIN_MEME -> "BUILTIN_MEME"
                CustomIconType.CUSTOM_IMAGE_URI -> "CUSTOM_IMAGE_URI"
                CustomIconType.NONE -> {
                    dao.deleteCustomIcon(packageName)
                    return@withContext
                }
            }
            dao.insertCustomIcon(
                CustomIconOverrideEntity(
                    packageName = packageName,
                    customIconType = typeStr,
                    customIconRef = ref
                )
            )
        }
    }

    suspend fun resetAppIcon(packageName: String) {
        withContext(Dispatchers.IO) {
            dao.deleteCustomIcon(packageName)
        }
    }

    // --- Launch App ---
    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    // --- Chat Messages ---
    fun getChatMessages(): Flow<List<FloppaChatMessageEntity>> = dao.getAllChatMessages()

    suspend fun saveChatMessage(isUser: Boolean, message: String) {
        withContext(Dispatchers.IO) {
            dao.insertChatMessage(
                FloppaChatMessageEntity(
                    isUser = isUser,
                    message = message
                )
            )
        }
    }

    suspend fun clearChatMessages() {
        withContext(Dispatchers.IO) {
            dao.clearChatMessages()
        }
    }

    // --- Settings ---
    fun getSettingsFlow(): Flow<List<FloppaSettingsEntity>> = dao.getAllSettings()

    suspend fun getSetting(key: String): String? = withContext(Dispatchers.IO) {
        dao.getSettingValue(key)
    }

    suspend fun saveSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        dao.setSetting(FloppaSettingsEntity(key = key, value = value))
    }

    // --- Weather & Weather Notes ---
    private val weatherService = com.example.data.remote.WeatherService()

    fun getWeatherNotesFlow(): Flow<List<com.example.data.local.WeatherNoteEntity>> = dao.getAllWeatherNotes()

    suspend fun addWeatherNote(note: String, city: String, condition: String) = withContext(Dispatchers.IO) {
        dao.insertWeatherNote(
            com.example.data.local.WeatherNoteEntity(
                note = note.trim(),
                cityName = city,
                weatherCondition = condition
            )
        )
    }

    suspend fun deleteWeatherNote(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteWeatherNote(id)
    }

    suspend fun fetchWeather(city: String, owmApiKey: String?): com.example.data.model.WeatherData {
        return weatherService.fetchWeather(city, owmApiKey)
    }

    // --- Real-time Android System Health Monitoring ---

    fun getRealtimeBatteryHealthFlow(): Flow<Intent> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    trySend(intent)
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(receiver, filter)
        if (initialIntent != null) {
            trySend(initialIntent)
        }
        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    fun getSystemStats(floppaHeftCount: Int, cachedBatteryIntent: Intent? = null): SystemStats {
        // Android System Health Battery & Thermal APIs
        val batteryIntent = cachedBatteryIntent ?: context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val rawTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280) ?: 280
        val tempC = rawTemp / 10f
        val voltageMv = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4150) ?: 4150

        val healthInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD) ?: BatteryManager.BATTERY_HEALTH_GOOD
        val batteryHealth = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Healthy"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            else -> "Good"
        }

        val pluggedInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val plugType = when (pluggedInt) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Qi Pad"
            else -> if (isCharging) "Charging" else "On Battery"
        }

        val thermalStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (powerManager?.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Cool & Optimal"
                PowerManager.THERMAL_STATUS_LIGHT -> "Light Warmth"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderate Throttling"
                PowerManager.THERMAL_STATUS_SEVERE -> "Severe Throttling"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critical Thermal State"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency Warning"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Thermal Shutdown"
                else -> if (tempC >= 42f) "Overheating" else if (tempC >= 36f) "Warm" else "Cool & Optimal"
            }
        } else {
            if (tempC >= 42f) "Overheating" else if (tempC >= 36f) "Warm" else "Cool & Optimal"
        }

        // RAM info
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)
        val ramTotal = memInfo.totalMem
        val ramAvail = memInfo.availMem
        val ramUsed = (ramTotal - ramAvail).coerceAtLeast(0L)
        val ramPct = if (ramTotal > 0) ((ramUsed * 100) / ramTotal).toInt() else 0

        // Storage info
        val statFs = try {
            StatFs(Environment.getDataDirectory().path)
        } catch (_: Exception) {
            null
        }
        val storageTotal = statFs?.totalBytes ?: (64L * 1024 * 1024 * 1024)
        val storageAvail = statFs?.availableBytes ?: (24L * 1024 * 1024 * 1024)
        val storageUsed = (storageTotal - storageAvail).coerceAtLeast(0L)
        val storagePct = if (storageTotal > 0) ((storageUsed * 100) / storageTotal).toInt() else 0

        val ramUsedGbFloat = kotlin.math.round((ramUsed / (1024f * 1024f * 1024f)) * 10f) / 10f
        val ramTotalGbFloat = kotlin.math.round((ramTotal / (1024f * 1024f * 1024f)) * 10f) / 10f
        val storageFreeGbFloat = kotlin.math.round(storageAvail / (1024f * 1024f * 1024f))
        val storageTotalGbFloat = kotlin.math.round(storageTotal / (1024f * 1024f * 1024f))

        // Uptime in minutes to avoid constant object inequality and recompositions
        val uptimeMinutes = SystemClock.elapsedRealtime() / 60000L

        // Network info
        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val netType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi 5GHz"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "5G Flop"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            else -> if (isOnline) "Connected" else "Offline Flop"
        }

        val batteryLabel = when {
            isCharging -> "⚡ Charging ($plugType)"
            batteryPct > 80 -> "✨ Maximum Caracal Vitality"
            batteryPct > 35 -> "🔋 Stable Flop Energy"
            else -> "🪫 Low Juice: Feed Pelmeni"
        }

        return SystemStats(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            batteryTemperatureC = tempC,
            batteryVoltageMv = voltageMv,
            batteryHealth = batteryHealth,
            thermalStatus = thermalStatus,
            plugType = plugType,
            batteryStatus = batteryLabel,
            ramUsedPercent = ramPct,
            ramUsedGb = ramUsedGbFloat,
            ramTotalGb = ramTotalGbFloat,
            storageFreeGb = storageFreeGbFloat,
            storageTotalGb = storageTotalGbFloat,
            storageUsedPercent = storagePct,
            uptimeMinutes = uptimeMinutes,
            networkType = netType,
            isOnline = isOnline,
            floppaHeftScore = floppaHeftCount
        )
    }
}
