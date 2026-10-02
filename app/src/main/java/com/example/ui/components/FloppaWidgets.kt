package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.FloppaSoundSynthesizer
import com.example.data.model.FloppaThemeType
import com.example.data.model.SystemStats

@Composable
fun FloppaSystemMonitorWidget(
    stats: SystemStats,
    theme: FloppaThemeType,
    modifier: Modifier = Modifier,
    onTestBatteryAlert: (() -> Unit)? = null
) {
    var isDiagnosticsExpanded by remember { mutableStateOf(false) }

    val tempColor = when {
        stats.isOverheated -> Color(0xFFEF4444) // Overheat red
        stats.isWarm -> Color(0xFFF59E0B) // Warm amber
        else -> theme.secondaryColor // Optimal cool cyan/green
    }

    val batteryColor = when {
        stats.batteryPercent > 40 -> theme.primaryColor
        stats.batteryPercent > 20 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("system_monitor_widget"),
        shape = RoundedCornerShape(22.dp),
        color = theme.surfaceColor.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Bar: Title, Live Health Indicator & Network Uptime
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "System Health",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Android System Health",
                        color = theme.primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Live Pulse Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.primaryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "● LIVE",
                            color = theme.primaryColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = "${stats.networkType} • ${stats.uptimeFormatted}",
                    color = theme.textColor.copy(alpha = 0.65f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dual Hero Cards: Real-Time Battery Level & Real-Time Device Temperature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Real-time Battery Level (BatteryManager API)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("realtime_battery_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, batteryColor.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔋 Battery", color = theme.textColor.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            if (stats.isCharging) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(theme.primaryColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("⚡ Charging", color = theme.primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Large percentage
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${stats.batteryPercent}%",
                                color = batteryColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stats.batteryHealth,
                                color = theme.textColor.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Progress gauge
                        LinearProgressIndicator(
                            progress = { (stats.batteryPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = batteryColor,
                            trackColor = theme.surfaceColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${stats.plugType} • ${(stats.batteryVoltageMv / 1000f).let { String.format("%.2fV", it) }}",
                            color = theme.textColor.copy(alpha = 0.65f),
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                // Card 2: Real-time Device Temperature (Hardware & BatteryManager API)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("realtime_temperature_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, tempColor.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🌡️ Temperature", color = theme.textColor.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(tempColor.copy(alpha = 0.18f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (stats.isOverheated) "Hot" else if (stats.isWarm) "Warm" else "Normal",
                                    color = tempColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Large temperature
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%.1f°C", stats.batteryTemperatureC),
                                color = tempColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${stats.batteryTemperatureF.toInt()}°F)",
                                color = theme.textColor.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Temperature gauge
                        LinearProgressIndicator(
                            progress = { ((stats.batteryTemperatureC - 20f) / 35f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = tempColor,
                            trackColor = theme.surfaceColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stats.thermalStatus,
                            color = theme.textColor.copy(alpha = 0.65f),
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Row: RAM & Storage telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // RAM Gauge
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🧠 Caracal RAM", fontSize = 11.sp, color = theme.textColor.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
                            Text("${stats.ramUsedPercent}%", fontSize = 11.sp, color = theme.primaryColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (stats.ramUsedPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = theme.primaryColor,
                            trackColor = theme.surfaceColor
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = String.format("%.1f/%.1f GB", stats.ramUsedGb, stats.ramTotalGb),
                            fontSize = 9.sp,
                            color = theme.textColor.copy(alpha = 0.55f)
                        )
                    }
                }

                // Storage Vault
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardColor.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🥟 Dumpling Vault", fontSize = 11.sp, color = theme.textColor.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
                            Text("${stats.storageUsedPercent}%", fontSize = 11.sp, color = theme.secondaryColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (stats.storageUsedPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = theme.secondaryColor,
                            trackColor = theme.surfaceColor
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = String.format("%.0f GB free", stats.storageFreeGb),
                            fontSize = 9.sp,
                            color = theme.textColor.copy(alpha = 0.55f)
                        )
                    }
                }
            }

            // Expandable Diagnostics Toggle Button
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { isDiagnosticsExpanded = !isDiagnosticsExpanded }
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Android System Health API Diagnostics",
                        fontSize = 11.sp,
                        color = theme.primaryColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = if (isDiagnosticsExpanded) "▲ Hide" else "▼ Details",
                    fontSize = 10.sp,
                    color = theme.primaryColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = isDiagnosticsExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.cardColor)
                        .padding(10.dp)
                ) {
                    DiagnosticRow("Battery Level (Capacity)", "${stats.batteryPercent}% (via BatteryManager.EXTRA_LEVEL)", theme)
                    DiagnosticRow("Battery Temperature", "${String.format("%.1f", stats.batteryTemperatureC)}°C / ${stats.batteryTemperatureF.toInt()}°F (EXTRA_TEMPERATURE)", theme)
                    DiagnosticRow("Battery Voltage", "${stats.batteryVoltageMv} mV (EXTRA_VOLTAGE)", theme)
                    DiagnosticRow("Battery Chemical Health", "${stats.batteryHealth} (EXTRA_HEALTH)", theme)
                    DiagnosticRow("Power Source / Plug", "${stats.plugType} (EXTRA_PLUGGED)", theme)
                    DiagnosticRow("Thermal Throttling Status", "${stats.thermalStatus} (PowerManager.currentThermalStatus)", theme)
                    DiagnosticRow("Event Stream Channel", "Intent.ACTION_BATTERY_CHANGED (Continuous broadcastFlow)", theme)

                    if (onTestBatteryAlert != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onTestBatteryAlert,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .testTag("test_battery_alert_diagnostics_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Simulate 100% Full Battery Notification", fontSize = 10.sp, color = theme.primaryColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String, theme: FloppaThemeType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = theme.textColor.copy(alpha = 0.65f), fontSize = 10.sp)
        Text(value, color = theme.textColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FloppaMemeWisdomWidget(
    theme: FloppaThemeType,
    onAskAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quotes = remember {
        listOf(
            "Flop for no hoe. Keep the caracal grind.",
            "A bowl of pelmeni in the belly brings peace to the soul.",
            "When in doubt, stretch your ear tufts toward the heavens.",
            "Heft is not measured in kilograms, but in royal presence.",
            "Sogga asks questions; Floppa executes solutions.",
            "You cannot rush the boiling of pelmeni or the booting of apps.",
            "Stay calm and purr karrr."
        )
    }

    var currentQuoteIndex by remember { mutableStateOf(0) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("meme_wisdom_widget"),
        shape = RoundedCornerShape(20.dp),
        color = theme.surfaceColor.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.secondaryColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(theme.cardColor)
                    .clickable {
                        currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("📜", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Floppa Meme Wisdom",
                    color = theme.primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                AnimatedContent(targetState = quotes[currentQuoteIndex], label = "quote") { quote ->
                    Text(
                        text = "\"$quote\"",
                        color = theme.textColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "New Quote",
                    tint = theme.primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FloppaSoundboardWidget(
    theme: FloppaThemeType,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("soundboard_widget"),
        shape = RoundedCornerShape(20.dp),
        color = theme.surfaceColor.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = theme.primaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Caracal Soundboard",
                    color = theme.primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SoundBtn("Chirp", "🐱", theme) { FloppaSoundSynthesizer.playCaracalChirp() }
                SoundBtn("Purr", "😻", theme) { FloppaSoundSynthesizer.playCaracalPurr() }
                SoundBtn("Hiss", "😾", theme) { FloppaSoundSynthesizer.playCaracalHiss() }
                SoundBtn("Chomp", "🥟", theme) { FloppaSoundSynthesizer.playPelmeniChomp() }
                SoundBtn("Laser", "⚡", theme) { FloppaSoundSynthesizer.playLaserFlop() }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.SoundBtn(
    name: String,
    emoji: String,
    theme: FloppaThemeType,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(34.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$emoji $name", fontSize = 10.sp, color = theme.textColor)
        }
    }
}
