package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.LauncherScreen
import com.example.ui.LauncherViewModel
import com.example.ui.components.BatteryFullDialog
import com.example.ui.components.LauncherOverloadDialog
import com.example.ui.components.MemeTransitions
import com.example.ui.screens.AppDrawerScreen
import com.example.ui.screens.FloppaAiSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IconCustomizerDialog
import com.example.ui.screens.LauncherSettingsScreen
import com.example.ui.screens.WeatherDialog
import com.example.ui.theme.FloppaLauncherTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            FloppaLauncherTheme(theme = uiState.currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = uiState.currentTheme.backgroundColor
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Animated Screen Transitions for screen switching (Flop Flip, Dumpling Bounce, etc.)
                        AnimatedContent(
                            targetState = uiState.currentScreen,
                            transitionSpec = MemeTransitions.getTransitionSpec(uiState.currentTransition),
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                LauncherScreen.HOME -> {
                                    HomeScreen(
                                        theme = uiState.currentTheme,
                                        systemStats = uiState.systemStats,
                                        pinnedApps = uiState.pinnedApps,
                                        dockApps = uiState.dockApps,
                                        floppaSpeech = uiState.floppaSpeechText,
                                        weather = uiState.weatherData,
                                        latestNote = uiState.weatherNotes.firstOrNull(),
                                        useFahrenheit = uiState.useFahrenheit,
                                        onOpenWeather = { viewModel.openWeatherDialog(true) },
                                        onPetFloppa = { viewModel.petFloppa() },
                                        onFeedFloppa = { viewModel.feedFloppaPelmeni() },
                                        onHissFloppa = { viewModel.hissFloppa() },
                                        onOpenAi = { viewModel.openFloppaAi(true) },
                                        onOpenDrawer = { viewModel.navigateTo(LauncherScreen.APP_DRAWER) },
                                        onOpenSettings = { viewModel.navigateTo(LauncherScreen.SETTINGS) },
                                        onLaunchApp = { packageName -> viewModel.launchApp(packageName) },
                                        onTogglePinToHome = { app -> viewModel.togglePinToHome(app) },
                                        onTogglePinToDock = { app -> viewModel.togglePinToDock(app) },
                                        onCustomizeIcon = { app -> viewModel.openIconCustomizer(app) },
                                        onResetIcon = { packageName -> viewModel.resetAppIcon(packageName) },
                                        onTestBatteryAlert = { viewModel.testBatteryFullAlert() },
                                        isNeedsEnabled = uiState.isFloppaNeedsEnabled,
                                        floppaNeeds = uiState.floppaNeeds
                                    )
                                }

                                LauncherScreen.APP_DRAWER -> {
                                    AppDrawerScreen(
                                        apps = uiState.filteredApps,
                                        searchQuery = uiState.searchQuery,
                                        selectedCategory = uiState.selectedDrawerCategory,
                                        theme = uiState.currentTheme,
                                        onSearchChange = { viewModel.setSearchQuery(it) },
                                        onCategoryChange = { viewModel.setDrawerCategory(it) },
                                        onLaunchApp = { packageName -> viewModel.launchApp(packageName) },
                                        onTogglePinToHome = { app -> viewModel.togglePinToHome(app) },
                                        onTogglePinToDock = { app -> viewModel.togglePinToDock(app) },
                                        onCustomizeIcon = { app -> viewModel.openIconCustomizer(app) },
                                        onResetIcon = { packageName -> viewModel.resetAppIcon(packageName) },
                                        onBack = { viewModel.navigateTo(LauncherScreen.HOME) }
                                    )
                                }

                                LauncherScreen.SETTINGS -> {
                                    LauncherSettingsScreen(
                                        currentTheme = uiState.currentTheme,
                                        currentTransition = uiState.currentTransition,
                                        customApiKey = uiState.customApiKey,
                                        useDefaultApiKey = uiState.useDefaultApiKey,
                                        isSoundEnabled = uiState.isSoundEnabled,
                                        isNeedsEnabled = uiState.isFloppaNeedsEnabled,
                                        isCustomAiProviderEnabled = uiState.isCustomAiProviderEnabled,
                                        customAiProviderEndpoint = uiState.customAiProviderEndpoint,
                                        customAiProviderKey = uiState.customAiProviderKey,
                                        customAiProviderModel = uiState.customAiProviderModel,
                                        onThemeChange = { viewModel.setTheme(it) },
                                        onTransitionChange = { viewModel.setTransition(it) },
                                        onApiKeyChange = { key, useDefault -> viewModel.setCustomApiKey(key, useDefault) },
                                        onSoundToggle = { viewModel.setSoundEnabled(it) },
                                        onNeedsToggle = { viewModel.setFloppaNeedsEnabled(it) },
                                        onCustomAiProviderSave = { enabled, endpoint, key, model ->
                                            viewModel.setCustomAiProvider(enabled, endpoint, key, model)
                                        },
                                        onSimulateLagOverload = { viewModel.simulateLagOverload() },
                                        onBack = { viewModel.navigateTo(LauncherScreen.HOME) }
                                    )
                                }
                            }
                        }

                        // Floppa AI Assistant Sheet
                        if (uiState.isFloppaAiOpen) {
                            FloppaAiSheet(
                                theme = uiState.currentTheme,
                                messages = uiState.chatMessages,
                                isLoading = uiState.isAiLoading,
                                errorMessage = uiState.aiErrorMessage,
                                customApiKey = uiState.customApiKey,
                                useDefaultKey = uiState.useDefaultApiKey,
                                installedApps = uiState.installedApps,
                                onSendMessage = { prompt -> viewModel.sendAiPrompt(prompt) },
                                onClearChat = { viewModel.clearChat() },
                                onOpenSettings = {
                                    viewModel.openFloppaAi(false)
                                    viewModel.navigateTo(LauncherScreen.SETTINGS)
                                },
                                onDismiss = { viewModel.openFloppaAi(false) }
                            )
                        }

                        // Meme Icon Customizer Dialog
                        if (uiState.isIconCustomizerOpen && uiState.selectedAppForCustomization != null) {
                            IconCustomizerDialog(
                                app = uiState.selectedAppForCustomization!!,
                                theme = uiState.currentTheme,
                                onSelectBuiltin = { iconId ->
                                    viewModel.setCustomMemeIcon(
                                        uiState.selectedAppForCustomization!!.packageName,
                                        iconId
                                    )
                                },
                                onSelectCustomImage = { uriString ->
                                    viewModel.setCustomImageUriIcon(
                                        uiState.selectedAppForCustomization!!.packageName,
                                        uriString
                                    )
                                },
                                onReset = {
                                    viewModel.resetAppIcon(
                                        uiState.selectedAppForCustomization!!.packageName
                                    )
                                },
                                onDismiss = { viewModel.openIconCustomizer(null) }
                            )
                        }

                        // Weather & Notes Dialog
                        if (uiState.isWeatherDialogOpen) {
                            WeatherDialog(
                                weather = uiState.weatherData,
                                notes = uiState.weatherNotes,
                                theme = uiState.currentTheme,
                                isLoading = uiState.isWeatherLoading,
                                currentCity = uiState.weatherCity,
                                currentOwmApiKey = uiState.owmApiKey,
                                useFahrenheit = uiState.useFahrenheit,
                                onRefresh = { viewModel.refreshWeather() },
                                onCityChange = { viewModel.setWeatherCity(it) },
                                onOwmApiKeyChange = { viewModel.setOwmApiKey(it) },
                                onToggleUnit = { viewModel.toggleTemperatureUnit() },
                                onAddNote = { viewModel.addWeatherNote(it) },
                                onDeleteNote = { viewModel.deleteWeatherNote(it) },
                                onDismiss = { viewModel.openWeatherDialog(false) }
                            )
                        }

                        // Battery 100% Full Advisory Pop Up
                        if (uiState.showBatteryFullDialog) {
                            BatteryFullDialog(
                                theme = uiState.currentTheme,
                                aiResponse = uiState.batteryFullAiResponse,
                                onContinueChargingAnyway = { viewModel.continueChargingAnyway() },
                                onUnplugDismiss = { viewModel.dismissBatteryFullAlert() }
                            )
                        }

                        // Emergency Overload Pop Up (Lag / ANR crash prevention)
                        if (uiState.isLauncherOverloaded) {
                            LauncherOverloadDialog(
                                theme = uiState.currentTheme,
                                onDisableLauncher = { viewModel.disableDefaultLauncher(this@MainActivity) },
                                onReopenLauncher = { viewModel.reopenLauncher(this@MainActivity) }
                            )
                        }
                    }
                }
            }
        }
    }
}
