package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppInfo
import com.example.data.model.FloppaNeeds
import com.example.data.model.FloppaThemeType
import com.example.data.model.SystemStats
import com.example.ui.components.AppItemView
import com.example.ui.components.FloppaAvatar
import com.example.ui.components.FloppaMemeWisdomWidget
import com.example.ui.components.FloppaNeedsWidget
import com.example.ui.components.FloppaSoundboardWidget
import com.example.ui.components.FloppaSystemMonitorWidget
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    theme: FloppaThemeType,
    systemStats: SystemStats,
    pinnedApps: List<AppInfo>,
    dockApps: List<AppInfo>,
    floppaSpeech: String,
    weather: com.example.data.model.WeatherData,
    latestNote: com.example.data.local.WeatherNoteEntity?,
    useFahrenheit: Boolean,
    onOpenWeather: () -> Unit,
    onPetFloppa: () -> Unit,
    onFeedFloppa: () -> Unit,
    onHissFloppa: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onLaunchApp: (String) -> Unit,
    onTogglePinToHome: (AppInfo) -> Unit,
    onTogglePinToDock: (AppInfo) -> Unit,
    onCustomizeIcon: (AppInfo) -> Unit,
    onResetIcon: (String) -> Unit,
    onTestBatteryAlert: () -> Unit = {},
    isNeedsEnabled: Boolean = true,
    floppaNeeds: FloppaNeeds = FloppaNeeds()
) {
    val backgroundBrush = remember(theme.backgroundColor, theme.surfaceColor) {
        Brush.verticalGradient(
            colors = listOf(
                theme.backgroundColor,
                theme.surfaceColor.copy(alpha = 0.85f),
                theme.backgroundColor
            )
        )
    }

    val weatherTempText by remember(useFahrenheit, weather.tempDisplayF, weather.tempDisplayC) {
        derivedStateOf {
            if (useFahrenheit) weather.tempDisplayF else weather.tempDisplayC
        }
    }

    val pinnedAppRows by remember(pinnedApps) {
        derivedStateOf {
            pinnedApps.chunked(4)
        }
    }

    val displayedDockApps by remember(dockApps) {
        derivedStateOf {
            dockApps.take(4)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("home_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Scrollable Content Area: Header, Floppa Avatar, System Telemetry, Pinned Apps
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp)
            ) {
                // Top Header: Date, Clock & Settings
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FloppaClockDisplay(theme = theme, heftRank = systemStats.memeHeftRank)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Weather Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(theme.surfaceColor)
                                    .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                    .clickable { onOpenWeather() }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .testTag("weather_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(weather.iconEmoji, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = weatherTempText,
                                        color = theme.primaryColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (latestNote != null) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("📝", fontSize = 10.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Quick App Search Trigger
                            IconButton(
                                onClick = onOpenDrawer,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(theme.surfaceColor)
                                    .border(1.dp, theme.primaryColor.copy(alpha = 0.2f), CircleShape)
                                    .testTag("search_trigger_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Apps",
                                    tint = theme.primaryColor
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Settings Trigger
                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(theme.surfaceColor)
                                    .border(1.dp, theme.primaryColor.copy(alpha = 0.2f), CircleShape)
                                    .testTag("open_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = theme.primaryColor
                                )
                            }
                        }
                    }
                }

                // Weather & Notes Glance Banner
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(theme.surfaceColor.copy(alpha = 0.88f))
                            .border(1.dp, theme.primaryColor.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                            .clickable { onOpenWeather() }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                            .testTag("weather_glance_banner"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(weather.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${weather.city} • $weatherTempText",
                                        color = theme.primaryColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = weather.condition,
                                        color = theme.textColor.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                }

                                if (latestNote != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "📝 Note: ${latestNote.note}",
                                        color = theme.textColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        text = "Tap to set City, OWM Key, or add Day Notes",
                                        color = theme.textColor.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Notes ›",
                            color = theme.primaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Interactive Floppa Avatar & Speech Bubble
                item {
                    FloppaAvatar(
                        theme = theme,
                        speechText = floppaSpeech,
                        onPet = onPetFloppa,
                        onFeed = onFeedFloppa,
                        onHiss = onHissFloppa,
                        onOpenAi = onOpenAi
                    )
                }

                // Floppa Needs & Moods Care Widget (Tamagotchi Mode)
                if (isNeedsEnabled) {
                    item {
                        FloppaNeedsWidget(
                            needs = floppaNeeds,
                            theme = theme,
                            onFeed = onFeedFloppa,
                            onPet = onPetFloppa
                        )
                    }
                }

                // Integrated System Telemetry Widget (Battery, RAM, Storage)
                item {
                    FloppaSystemMonitorWidget(
                        stats = systemStats,
                        theme = theme,
                        onTestBatteryAlert = onTestBatteryAlert
                    )
                }

                // Soundboard Widget
                item {
                    FloppaSoundboardWidget(theme = theme)
                }

                // Meme Wisdom Widget
                item {
                    FloppaMemeWisdomWidget(theme = theme, onAskAi = onOpenAi)
                }

                // Pinned Apps Header
                if (pinnedApps.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Favorite Apps",
                                color = theme.primaryColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⭐", fontSize = 11.sp)
                        }
                    }

                    // Pinned Apps Grid (Flow / Chunked in rows of 4)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            pinnedAppRows.forEach { rowApps ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    rowApps.forEach { app ->
                                        androidx.compose.runtime.key(app.packageName) {
                                            AppItemView(
                                                app = app,
                                                theme = theme,
                                                onClick = { onLaunchApp(app.packageName) },
                                                onPinToHome = { onTogglePinToHome(app) },
                                                onPinToDock = { onTogglePinToDock(app) },
                                                onCustomizeIcon = { onCustomizeIcon(app) },
                                                onResetIcon = { onResetIcon(app.packageName) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    // Filler if row < 4
                                    repeat(4 - rowApps.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fixed Bottom Dock Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("launcher_dock"),
                shape = RoundedCornerShape(26.dp),
                color = theme.surfaceColor.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dock Apps (up to 4)
                    displayedDockApps.forEach { app ->
                        androidx.compose.runtime.key(app.packageName) {
                            AppItemView(
                                app = app,
                                theme = theme,
                                onClick = { onLaunchApp(app.packageName) },
                                onPinToHome = { onTogglePinToHome(app) },
                                onPinToDock = { onTogglePinToDock(app) },
                                onCustomizeIcon = { onCustomizeIcon(app) },
                                onResetIcon = { onResetIcon(app.packageName) },
                                showLabel = false,
                                iconSize = 46.dp
                            )
                        }
                    }

                    // App Drawer Trigger in center / dock
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(theme.primaryColor, theme.secondaryColor)
                                )
                            )
                            .clickable { onOpenDrawer() }
                            .testTag("app_drawer_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = "All Apps",
                            tint = theme.backgroundColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Quick AI Trigger
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(theme.cardColor)
                            .border(1.dp, theme.primaryColor.copy(alpha = 0.4f), CircleShape)
                            .clickable { onOpenAi() }
                            .testTag("dock_ai_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FloppaClockDisplay(theme: FloppaThemeType, heftRank: String) {
    var currentTime by remember { mutableStateOf(getFormattedTime()) }
    var currentDate by remember { mutableStateOf(getFormattedDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            val newTime = getFormattedTime()
            val newDate = getFormattedDate()
            if (newTime != currentTime) currentTime = newTime
            if (newDate != currentDate) currentDate = newDate
            delay(15000L)
        }
    }

    Column {
        Text(
            text = currentTime,
            color = theme.primaryColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "$currentDate • $heftRank",
            color = theme.textColor.copy(alpha = 0.75f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
private val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

private fun getFormattedTime(): String {
    return timeFormat.format(Date())
}

private fun getFormattedDate(): String {
    return dateFormat.format(Date())
}
