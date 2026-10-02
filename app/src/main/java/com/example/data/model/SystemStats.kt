package com.example.data.model

import androidx.compose.runtime.Immutable
import kotlin.math.abs

@Immutable
data class SystemStats(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val batteryTemperatureC: Float = 28.0f,
    val batteryVoltageMv: Int = 4150,
    val batteryHealth: String = "Healthy",
    val thermalStatus: String = "Cool & Optimal",
    val plugType: String = "On Battery",
    val batteryStatus: String = "Optimal Flop",
    val ramUsedPercent: Int = 0,
    val ramUsedGb: Float = 2.0f,
    val ramTotalGb: Float = 6.0f,
    val storageFreeGb: Float = 24.0f,
    val storageTotalGb: Float = 64.0f,
    val storageUsedPercent: Int = 0,
    val uptimeMinutes: Long = 0L,
    val networkType: String = "WiFi",
    val isOnline: Boolean = true,
    val floppaHeftScore: Int = 420
) {
    val batteryTemperatureF: Float get() = (batteryTemperatureC * 9f / 5f) + 32f

    val uptimeFormatted: String
        get() {
            val hours = uptimeMinutes / 60
            val minutes = uptimeMinutes % 60
            return "${hours}h ${minutes}m"
        }

    val memeHeftRank: String
        get() = when {
            floppaHeftScore >= 1000 -> "Gigantic Gosha (Godlike)"
            floppaHeftScore >= 500 -> "Certified Hefty Boy"
            floppaHeftScore >= 250 -> "Pelmeni Connoisseur"
            floppaHeftScore >= 100 -> "Growing Caracal"
            else -> "Kitten Flop"
        }

    val isOverheated: Boolean get() = batteryTemperatureC >= 42.0f || batteryHealth == "Overheated"
    val isWarm: Boolean get() = batteryTemperatureC in 36.0f..41.9f

    // High performance structural stability: suppresses sensor jitter and memory byte fluctuations
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SystemStats) return false

        return batteryPercent == other.batteryPercent &&
                isCharging == other.isCharging &&
                abs(batteryTemperatureC - other.batteryTemperatureC) < 0.4f &&
                batteryHealth == other.batteryHealth &&
                thermalStatus == other.thermalStatus &&
                plugType == other.plugType &&
                ramUsedPercent == other.ramUsedPercent &&
                storageUsedPercent == other.storageUsedPercent &&
                uptimeMinutes == other.uptimeMinutes &&
                networkType == other.networkType &&
                isOnline == other.isOnline &&
                floppaHeftScore == other.floppaHeftScore
    }

    override fun hashCode(): Int {
        var result = batteryPercent
        result = 31 * result + isCharging.hashCode()
        result = 31 * result + (batteryTemperatureC * 2).toInt()
        result = 31 * result + batteryHealth.hashCode()
        result = 31 * result + thermalStatus.hashCode()
        result = 31 * result + plugType.hashCode()
        result = 31 * result + ramUsedPercent
        result = 31 * result + storageUsedPercent
        result = 31 * result + uptimeMinutes.hashCode()
        result = 31 * result + networkType.hashCode()
        result = 31 * result + isOnline.hashCode()
        result = 31 * result + floppaHeftScore
        return result
    }
}
