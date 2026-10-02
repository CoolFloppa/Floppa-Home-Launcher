package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.FloppaSoundSynthesizer
import com.example.data.local.FloppaChatMessageEntity
import com.example.data.local.FloppaDatabase
import com.example.data.model.AppInfo
import com.example.data.model.CustomIconType
import com.example.data.model.FloppaThemeType
import com.example.data.model.MemeTransitionType
import com.example.data.model.SystemStats
import com.example.data.remote.FloppaAiService
import com.example.data.repository.LauncherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class LauncherScreen {
    HOME,
    APP_DRAWER,
    SETTINGS
}

data class LauncherUiState(
    val currentScreen: LauncherScreen = LauncherScreen.HOME,
    val installedApps: List<AppInfo> = emptyList(),
    val pinnedApps: List<AppInfo> = emptyList(),
    val dockApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val filteredApps: List<AppInfo> = emptyList(),
    val selectedDrawerCategory: String = "All",
    val systemStats: SystemStats = SystemStats(),
    val currentTheme: FloppaThemeType = FloppaThemeType.CLASSIC_GOSHA,
    val currentTransition: MemeTransitionType = MemeTransitionType.FLOP_FLIP,
    val floppaHeftCount: Int = 142,
    val floppaSpeechText: String = "Flop for no one. Tap dumplings to feed me!",
    val isFloppaAiOpen: Boolean = false,
    val isIconCustomizerOpen: Boolean = false,
    val selectedAppForCustomization: AppInfo? = null,
    val chatMessages: List<FloppaChatMessageEntity> = emptyList(),
    val isAiLoading: Boolean = false,
    val aiErrorMessage: String? = null,
    val customApiKey: String = "",
    val useDefaultApiKey: Boolean = true,
    val customWallpaperUri: String? = null,
    val isSoundEnabled: Boolean = true,
    val weatherData: com.example.data.model.WeatherData = com.example.data.model.WeatherData(),
    val weatherNotes: List<com.example.data.local.WeatherNoteEntity> = emptyList(),
    val weatherCity: String = "Floppa Sanctuary",
    val owmApiKey: String = "",
    val isWeatherLoading: Boolean = false,
    val isWeatherDialogOpen: Boolean = false,
    val useFahrenheit: Boolean = false,
    val showBatteryFullDialog: Boolean = false,
    val batteryFullAiResponse: String? = null,
    val isFloppaNeedsEnabled: Boolean = true,
    val floppaNeeds: com.example.data.model.FloppaNeeds = com.example.data.model.FloppaNeeds(),
    val isCustomAiProviderEnabled: Boolean = false,
    val customAiProviderEndpoint: String = "https://api.openai.com/v1/chat/completions",
    val customAiProviderKey: String = "",
    val customAiProviderModel: String = "gpt-4o-mini",
    val isLauncherOverloaded: Boolean = false
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FloppaDatabase.getInstance(application)
    private val repository = LauncherRepository(application, db.appDao())
    private val aiService = FloppaAiService()
    private var hasShownBatteryFullAlert = false
    private var hasLoadedInitialWeather = false
    private var hasLoadedInitialSettings = false

    private val lagWatchdog = com.example.util.LauncherLagWatchdog(
        checkIntervalMs = 3000L,
        lagThresholdMs = 6000L,
        onOverloadDetected = { triggerLauncherOverload() }
    )

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        FloppaSoundSynthesizer.initialize(application)
        lagWatchdog.start(viewModelScope)

        // Observe apps
        viewModelScope.launch {
            repository.getInstalledAppsFlow().collectLatest { allApps ->
                val pinned = allApps.filter { it.isPinnedToHome }
                val dock = allApps.filter { it.isDockApp }

                // Default initial pinned & dock apps if empty
                if (pinned.isEmpty() && dock.isEmpty() && allApps.isNotEmpty()) {
                    autoPopulateDefaults(allApps)
                }

                _uiState.update { state ->
                    state.copy(
                        installedApps = allApps,
                        pinnedApps = pinned,
                        dockApps = dock,
                        filteredApps = filterApps(allApps, state.searchQuery, state.selectedDrawerCategory)
                    )
                }
            }
        }

        // Observe chat messages
        viewModelScope.launch {
            repository.getChatMessages().collectLatest { messages ->
                _uiState.update { it.copy(chatMessages = messages) }
            }
        }

        // Observe weather notes
        viewModelScope.launch {
            repository.getWeatherNotesFlow().collectLatest { notes ->
                _uiState.update { it.copy(weatherNotes = notes) }
            }
        }

        // Observe settings
        viewModelScope.launch {
            repository.getSettingsFlow().collectLatest { settingsList ->
                val settingsMap = settingsList.associate { it.key to it.value }
                val themeName = settingsMap["theme"]
                val transitionName = settingsMap["transition"]
                val customKey = settingsMap["custom_api_key"] ?: ""
                val useDefault = settingsMap["use_default_key"] != "false"
                val wallpaper = settingsMap["wallpaper_uri"]
                val heft = settingsMap["floppa_heft"]?.toIntOrNull() ?: 142
                val sound = settingsMap["sound_enabled"] != "false"
                val city = settingsMap["weather_city"] ?: "Floppa Sanctuary"
                val owmKey = settingsMap["owm_api_key"] ?: ""
                val fahr = settingsMap["use_fahrenheit"] == "true"
                val needsEnabled = settingsMap["floppa_needs_enabled"] != "false"
                val hungerVal = settingsMap["floppa_hunger"]?.toIntOrNull() ?: 85
                val affectionVal = settingsMap["floppa_affection"]?.toIntOrNull() ?: 80
                val customAiEnabled = settingsMap["custom_ai_provider_enabled"] == "true"
                val customAiEndpoint = settingsMap["custom_ai_provider_endpoint"] ?: "https://api.openai.com/v1/chat/completions"
                val customAiKey = settingsMap["custom_ai_provider_key"] ?: ""
                val customAiModel = settingsMap["custom_ai_provider_model"] ?: "gpt-4o-mini"

                val cityChanged = hasLoadedInitialSettings && (city != _uiState.value.weatherCity || owmKey != _uiState.value.owmApiKey)

                _uiState.update { current ->
                    current.copy(
                        currentTheme = FloppaThemeType.fromName(themeName),
                        currentTransition = MemeTransitionType.fromName(transitionName),
                        customApiKey = customKey,
                        useDefaultApiKey = useDefault,
                        customWallpaperUri = wallpaper,
                        floppaHeftCount = if (!hasLoadedInitialSettings) heft else current.floppaHeftCount,
                        isSoundEnabled = sound,
                        weatherCity = city,
                        owmApiKey = owmKey,
                        useFahrenheit = fahr,
                        isFloppaNeedsEnabled = needsEnabled,
                        floppaNeeds = if (!hasLoadedInitialSettings) com.example.data.model.FloppaNeeds(hungerVal, affectionVal) else current.floppaNeeds,
                        isCustomAiProviderEnabled = customAiEnabled,
                        customAiProviderEndpoint = customAiEndpoint,
                        customAiProviderKey = customAiKey,
                        customAiProviderModel = customAiModel
                    )
                }

                val needWeather = cityChanged || !hasLoadedInitialWeather
                hasLoadedInitialSettings = true

                if (needWeather) {
                    hasLoadedInitialWeather = true
                    refreshWeather()
                }
            }
        }

        // Real-time Android System Health (battery level & device temperature) broadcast listener
        viewModelScope.launch(Dispatchers.IO) {
            repository.getRealtimeBatteryHealthFlow()
                .conflate()
                .collectLatest { batteryIntent ->
                    val currentStats = _uiState.value.systemStats
                    val stats = repository.getSystemStats(_uiState.value.floppaHeftCount, batteryIntent)
                    if (currentStats != stats) {
                        _uiState.update { it.copy(systemStats = stats) }
                    }

                    if (stats.batteryPercent >= 100 && stats.isCharging) {
                        if (!hasShownBatteryFullAlert) {
                            hasShownBatteryFullAlert = true
                            triggerBatteryFullAlert()
                        }
                    } else if (!stats.isCharging) {
                        hasShownBatteryFullAlert = false
                    }
                }
        }

        // Periodic system stats polling & needs decay (gentle 30s cadence to eliminate lag)
        var loopCount = 0
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(30000L)
                val currentStats = _uiState.value.systemStats
                val stats = repository.getSystemStats(_uiState.value.floppaHeftCount)
                if (currentStats != stats) {
                    _uiState.update { it.copy(systemStats = stats) }
                }

                if (stats.batteryPercent >= 100 && stats.isCharging) {
                    if (!hasShownBatteryFullAlert) {
                        hasShownBatteryFullAlert = true
                        triggerBatteryFullAlert()
                    }
                } else if (!stats.isCharging) {
                    hasShownBatteryFullAlert = false
                }

                // Every ~2 minutes (4 ticks of 30s), decay needs slightly if enabled
                loopCount++
                if (loopCount % 4 == 0 && _uiState.value.isFloppaNeedsEnabled) {
                    val currentNeeds = _uiState.value.floppaNeeds
                    val newHunger = (currentNeeds.hunger - 1).coerceAtLeast(10)
                    val newAffection = (currentNeeds.affection - 1).coerceAtLeast(10)
                    if (newHunger != currentNeeds.hunger || newAffection != currentNeeds.affection) {
                        _uiState.update {
                            it.copy(floppaNeeds = com.example.data.model.FloppaNeeds(newHunger, newAffection))
                        }
                    }
                }
            }
        }
    }

    private suspend fun autoPopulateDefaults(allApps: List<AppInfo>) {
        // Dock apps: phone, dialer, chrome, camera, messages or first 4
        val dockCandidates = allApps.filter { app ->
            val pkg = app.packageName.lowercase()
            pkg.contains("dialer") || pkg.contains("phone") || pkg.contains("chrome") ||
                pkg.contains("browser") || pkg.contains("message") || pkg.contains("mms") ||
                pkg.contains("camera")
        }.take(4)

        if (dockCandidates.isNotEmpty()) {
            dockCandidates.forEach { repository.pinApp(it.packageName, isDock = true) }
        } else {
            allApps.take(4).forEach { repository.pinApp(it.packageName, isDock = true) }
        }

        // Home pinned apps: next few apps
        val remaining = allApps.filter { app -> dockCandidates.none { it.packageName == app.packageName } }.take(8)
        remaining.forEach { repository.pinApp(it.packageName, isDock = false) }
    }

    fun navigateTo(screen: LauncherScreen) {
        if (_uiState.value.isSoundEnabled) {
            FloppaSoundSynthesizer.playCaracalChirp()
        }
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun openFloppaAi(open: Boolean) {
        if (open && _uiState.value.isSoundEnabled) {
            FloppaSoundSynthesizer.playCaracalPurr()
        }
        _uiState.update { it.copy(isFloppaAiOpen = open) }
    }

    fun openIconCustomizer(app: AppInfo?) {
        _uiState.update {
            it.copy(
                isIconCustomizerOpen = app != null,
                selectedAppForCustomization = app
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredApps = filterApps(state.installedApps, query, state.selectedDrawerCategory)
            )
        }
    }

    fun setDrawerCategory(category: String) {
        _uiState.update { state ->
            state.copy(
                selectedDrawerCategory = category,
                filteredApps = filterApps(state.installedApps, state.searchQuery, category)
            )
        }
    }

    private fun filterApps(apps: List<AppInfo>, query: String, category: String): List<AppInfo> {
        val q = query.trim().lowercase()
        return apps.filter { app ->
            val matchesQuery = q.isEmpty() || app.label.lowercase().contains(q) || app.packageName.lowercase().contains(q)
            val matchesCategory = when (category) {
                "Pinned" -> app.isPinnedToHome || app.isDockApp
                "System" -> app.isSystemApp
                "Custom Icons" -> app.customIconType != CustomIconType.NONE
                else -> true
            }
            matchesQuery && matchesCategory
        }
    }

    fun launchApp(packageName: String) {
        if (_uiState.value.isSoundEnabled) {
            FloppaSoundSynthesizer.playLaserFlop()
        }
        repository.launchApp(packageName)
    }

    fun togglePinToHome(app: AppInfo) {
        viewModelScope.launch {
            if (app.isPinnedToHome) {
                repository.unpinApp(app.packageName)
            } else {
                repository.pinApp(app.packageName, isDock = false)
            }
        }
    }

    fun togglePinToDock(app: AppInfo) {
        viewModelScope.launch {
            if (app.isDockApp) {
                repository.unpinApp(app.packageName)
            } else {
                repository.pinApp(app.packageName, isDock = true)
            }
        }
    }

    fun setCustomMemeIcon(packageName: String, iconId: String) {
        viewModelScope.launch {
            if (_uiState.value.isSoundEnabled) {
                FloppaSoundSynthesizer.playCaracalChirp()
            }
            repository.setAppIconOverride(packageName, CustomIconType.BUILTIN_MEME, iconId)
            openIconCustomizer(null)
        }
    }

    fun setCustomImageUriIcon(packageName: String, uriString: String) {
        viewModelScope.launch {
            if (_uiState.value.isSoundEnabled) {
                FloppaSoundSynthesizer.playCaracalChirp()
            }
            repository.setAppIconOverride(packageName, CustomIconType.CUSTOM_IMAGE_URI, uriString)
            openIconCustomizer(null)
        }
    }

    fun resetAppIcon(packageName: String) {
        viewModelScope.launch {
            repository.resetAppIcon(packageName)
            openIconCustomizer(null)
        }
    }

    // Tamagotchi / Floppa mini-game interactions
    fun feedFloppaPelmeni() {
        val newHeft = _uiState.value.floppaHeftCount + 10
        val currentNeeds = _uiState.value.floppaNeeds
        val newHunger = (currentNeeds.hunger + 22).coerceAtMost(100)
        val newNeeds = currentNeeds.copy(hunger = newHunger)

        val speech = listOf(
            "🥟 *CHOMP* Savory dumpling devoured! Satiety +22.",
            "🥟 Glorious pelmeni! Gosha's stomach is blessed.",
            "🥟 *purr* Another dumpling for the sovereign caracal.",
            "🥟 Delicious! My ear tufts stand extra tall today."
        ).random()

        if (_uiState.value.isSoundEnabled) {
            FloppaSoundSynthesizer.playPelmeniChomp()
        }

        _uiState.update {
            it.copy(
                floppaHeftCount = newHeft,
                floppaSpeechText = speech,
                floppaNeeds = newNeeds
            )
        }

        viewModelScope.launch {
            repository.saveSetting("floppa_heft", newHeft.toString())
            repository.saveSetting("floppa_hunger", newHunger.toString())
        }
    }

    fun petFloppa() {
        if (_uiState.value.isSoundEnabled) {
            FloppaSoundSynthesizer.playCaracalPurr()
        }

        val currentNeeds = _uiState.value.floppaNeeds
        val newAffection = (currentNeeds.affection + 20).coerceAtMost(100)
        val newNeeds = currentNeeds.copy(affection = newAffection)

        val speech = listOf(
            "😻 *purr karrr* You gently stroked the ear tufts. Affection +20!",
            "👑 Big Floppa permits your tribute. Ear tufts thoroughly pampered.",
            "✨ *happy ear wiggles* Peak caracal serenity achieved.",
            "🐱 Floppa remembers your loyalty and purrs deeply."
        ).random()

        _uiState.update {
            it.copy(
                floppaSpeechText = speech,
                floppaNeeds = newNeeds
            )
        }

        viewModelScope.launch {
            repository.saveSetting("floppa_affection", newAffection.toString())
        }
    }

    fun hissFloppa() {
        if (_uiState.value.isSoundEnabled) {
            FloppaSoundSynthesizer.playCaracalHiss()
        }
        val speech = listOf(
            "😾 *HISSS* Who disturbed my dumpling digestion?!",
            "😾 *spicy caracal sounds* Don't touch the launcher settings without asking!",
            "😾 Beware the fury of Big Floppa."
        ).random()
        _uiState.update { it.copy(floppaSpeechText = speech) }
    }

    // Floppa AI Chat
    fun sendAiPrompt(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isEmpty()) return

        val history = _uiState.value.chatMessages.map { it.isUser to it.message }

        viewModelScope.launch {
            // Save user message
            repository.saveChatMessage(isUser = true, message = cleanPrompt)
            _uiState.update { it.copy(isAiLoading = true, aiErrorMessage = null) }

            if (_uiState.value.isSoundEnabled) {
                FloppaSoundSynthesizer.playCaracalChirp()
            }

            val appLabels = _uiState.value.installedApps.map { it.label }

            val result = aiService.askFloppa(
                userPrompt = cleanPrompt,
                customApiKey = _uiState.value.customApiKey,
                useDefaultKey = _uiState.value.useDefaultApiKey,
                conversationHistory = history,
                installedAppCatalog = appLabels,
                useCustomProvider = _uiState.value.isCustomAiProviderEnabled,
                customProviderEndpoint = _uiState.value.customAiProviderEndpoint,
                customProviderKey = _uiState.value.customAiProviderKey,
                customProviderModel = _uiState.value.customAiProviderModel
            )

            result.onSuccess { reply ->
                repository.saveChatMessage(isUser = false, message = reply)
                _uiState.update {
                    it.copy(
                        isAiLoading = false,
                        floppaSpeechText = reply.take(80) + if (reply.length > 80) "..." else ""
                    )
                }
                if (_uiState.value.isSoundEnabled) {
                    FloppaSoundSynthesizer.playCaracalPurr()
                }
            }.onFailure { err ->
                val fallback = "⚠️ *hiss* Floppa AI connection issue: ${err.message}. (Check your API Key in Settings!)"
                repository.saveChatMessage(isUser = false, message = fallback)
                _uiState.update {
                    it.copy(
                        isAiLoading = false,
                        aiErrorMessage = err.message
                    )
                }
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatMessages()
        }
    }

    // Settings adjustments
    fun setTheme(theme: FloppaThemeType) {
        viewModelScope.launch {
            repository.saveSetting("theme", theme.name)
        }
    }

    fun setTransition(transition: MemeTransitionType) {
        viewModelScope.launch {
            repository.saveSetting("transition", transition.name)
        }
    }

    fun setCustomApiKey(key: String, useDefault: Boolean) {
        viewModelScope.launch {
            repository.saveSetting("custom_api_key", key)
            repository.saveSetting("use_default_key", useDefault.toString())
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSetting("sound_enabled", enabled.toString())
        }
    }

    fun setCustomWallpaper(uri: String?) {
        viewModelScope.launch {
            repository.saveSetting("wallpaper_uri", uri.orEmpty())
        }
    }

    // --- Weather & Weather Notes ---
    fun openWeatherDialog(open: Boolean) {
        if (open && _uiState.value.isSoundEnabled) {
            FloppaSoundSynthesizer.playCaracalChirp()
        }
        _uiState.update { it.copy(isWeatherDialogOpen = open) }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWeatherLoading = true) }
            val currentCity = _uiState.value.weatherCity
            val owmKey = _uiState.value.owmApiKey
            val data = repository.fetchWeather(currentCity, owmKey)
            _uiState.update {
                it.copy(
                    weatherData = data,
                    isWeatherLoading = false
                )
            }
        }
    }

    fun setWeatherCity(city: String) {
        val trimmed = city.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.saveSetting("weather_city", trimmed)
            _uiState.update { it.copy(weatherCity = trimmed) }
            refreshWeather()
        }
    }

    fun setOwmApiKey(key: String) {
        viewModelScope.launch {
            repository.saveSetting("owm_api_key", key.trim())
            _uiState.update { it.copy(owmApiKey = key.trim()) }
            refreshWeather()
        }
    }

    fun toggleTemperatureUnit() {
        val newUnit = !_uiState.value.useFahrenheit
        viewModelScope.launch {
            repository.saveSetting("use_fahrenheit", newUnit.toString())
            _uiState.update { it.copy(useFahrenheit = newUnit) }
        }
    }

    fun addWeatherNote(noteText: String) {
        val clean = noteText.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            if (_uiState.value.isSoundEnabled) {
                FloppaSoundSynthesizer.playPelmeniChomp()
            }
            val current = _uiState.value.weatherData
            val cond = "${current.iconEmoji} ${current.condition} (${current.tempDisplayC})"
            repository.addWeatherNote(
                note = clean,
                city = current.city,
                condition = cond
            )
        }
    }

    fun deleteWeatherNote(id: Long) {
        viewModelScope.launch {
            repository.deleteWeatherNote(id)
        }
    }

    // --- Battery 100% Pop Up & AI Advisory ---
    fun triggerBatteryFullAlert(customAiText: String? = null) {
        viewModelScope.launch {
            if (_uiState.value.isSoundEnabled) {
                FloppaSoundSynthesizer.playCaracalChirp()
            }

            val fallbackAiResponses = listOf(
                "⚡ *karrr* Gosha says: 'Your battery is stuffed like a Siberian pelmeni! Unplugging now keeps the lithium cells cool and preserves your phone lifespan.'",
                "🔋 *ear tufts twitch* '100% capacity achieved! Trickle charging over 100% stresses the battery cathode. Disconnect for royal battery longevity!'",
                "👑 'Big Floppa commands: unplug the charger! The vessel is full, the dumplings are hot, and overcharging degrades battery health.'",
                "✨ *flops lazily* 'Pure 100% juice! Unplug the cable so the electrons can rest like a caracal on a velvet sofa.'"
            )

            val aiAdvice = customAiText ?: fallbackAiResponses.random()

            _uiState.update {
                it.copy(
                    showBatteryFullDialog = true,
                    batteryFullAiResponse = aiAdvice
                )
            }
        }
    }

    fun continueChargingAnyway() {
        // Dismiss dialog but remember to not spam again for this charging session
        _uiState.update { it.copy(showBatteryFullDialog = false) }
    }

    fun dismissBatteryFullAlert() {
        _uiState.update { it.copy(showBatteryFullDialog = false) }
    }

    fun testBatteryFullAlert() {
        triggerBatteryFullAlert()
    }

    // --- Needs & Moods Settings ---
    fun setFloppaNeedsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSetting("floppa_needs_enabled", enabled.toString())
            _uiState.update { it.copy(isFloppaNeedsEnabled = enabled) }
        }
    }

    // --- Experimental Custom AI Provider Settings ---
    fun setCustomAiProvider(
        enabled: Boolean,
        endpoint: String,
        apiKey: String,
        model: String
    ) {
        viewModelScope.launch {
            repository.saveSetting("custom_ai_provider_enabled", enabled.toString())
            repository.saveSetting("custom_ai_provider_endpoint", endpoint.trim())
            repository.saveSetting("custom_ai_provider_key", apiKey.trim())
            repository.saveSetting("custom_ai_provider_model", model.trim())
            _uiState.update {
                it.copy(
                    isCustomAiProviderEnabled = enabled,
                    customAiProviderEndpoint = endpoint.trim(),
                    customAiProviderKey = apiKey.trim(),
                    customAiProviderModel = model.trim()
                )
            }
        }
    }

    // --- Emergency Overload & Crash Prevention ---
    fun triggerLauncherOverload() {
        _uiState.update { it.copy(isLauncherOverloaded = true) }
    }

    fun reopenLauncher(context: Context) {
        lagWatchdog.reset()
        _uiState.update { it.copy(isLauncherOverloaded = false) }
        com.example.util.LauncherRecoveryUtils.restartLauncher(context)
    }

    fun disableDefaultLauncher(context: Context) {
        com.example.util.LauncherRecoveryUtils.disableDefaultLauncher(context)
    }

    fun simulateLagOverload() {
        lagWatchdog.triggerOverload()
    }
}
