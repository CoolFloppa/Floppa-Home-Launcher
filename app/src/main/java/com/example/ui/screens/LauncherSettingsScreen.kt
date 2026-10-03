package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FloppaThemeType
import com.example.data.model.MemeTransitionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherSettingsScreen(
    currentTheme: FloppaThemeType,
    currentTransition: MemeTransitionType,
    customApiKey: String,
    useDefaultApiKey: Boolean,
    isSoundEnabled: Boolean,
    isNeedsEnabled: Boolean = true,
    isCustomAiProviderEnabled: Boolean = false,
    customAiProviderEndpoint: String = "https://api.openai.com/v1/chat/completions",
    customAiProviderKey: String = "",
    customAiProviderModel: String = "gpt-4o-mini",
    onThemeChange: (FloppaThemeType) -> Unit,
    onTransitionChange: (MemeTransitionType) -> Unit,
    onApiKeyChange: (key: String, useDefault: Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onNeedsToggle: (Boolean) -> Unit = {},
    onCustomAiProviderSave: (enabled: Boolean, endpoint: String, key: String, model: String) -> Unit = { _, _, _, _ -> },
    onSimulateLagOverload: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var tempApiKey by remember(customApiKey) { mutableStateOf(customApiKey) }
    var tempUseDefault by remember(useDefaultApiKey) { mutableStateOf(useDefaultApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    var tempCustomAiEnabled by remember(isCustomAiProviderEnabled) { mutableStateOf(isCustomAiProviderEnabled) }
    var tempCustomAiEndpoint by remember(customAiProviderEndpoint) { mutableStateOf(customAiProviderEndpoint) }
    var tempCustomAiKey by remember(customAiProviderKey) { mutableStateOf(customAiProviderKey) }
    var tempCustomAiModel by remember(customAiProviderModel) { mutableStateOf(customAiProviderModel) }
    var isCustomKeyVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Floppa Launcher Settings",
                        color = currentTheme.primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = currentTheme.textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = currentTheme.surfaceColor
                )
            )
        },
        containerColor = currentTheme.backgroundColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Section 1: Default Launcher Configuration
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("default_launcher_card"),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = currentTheme.primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Default Home Launcher",
                                color = currentTheme.textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            "Apply Floppa Home Launcher as your device's default home screen so clicking the Home button always returns to Gosha.",
                            color = currentTheme.textColor.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                openHomeSettings(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primaryColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("set_default_launcher_button")
                        ) {
                            Text("Set as Default Launcher", color = currentTheme.backgroundColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 2: Floppa AI Assistant Configuration
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_settings_card"),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.secondaryColor.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = currentTheme.secondaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Floppa AI Assistant Engine",
                                color = currentTheme.textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            "Choose whether Floppa AI communicates using the default built-in Gemini API key or your personal custom Gemini API key.",
                            color = currentTheme.textColor.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Toggle default vs custom
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Use Default Gemini Key",
                                    color = currentTheme.textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    if (tempUseDefault) "Using platform-injected Gemini key" else "Using custom user key below",
                                    color = currentTheme.textColor.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }

                            Switch(
                                checked = tempUseDefault,
                                onCheckedChange = { checked ->
                                    tempUseDefault = checked
                                    onApiKeyChange(tempApiKey, checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = currentTheme.backgroundColor,
                                    checkedTrackColor = currentTheme.primaryColor
                                ),
                                modifier = Modifier.testTag("toggle_default_api_key")
                            )
                        }

                        if (!tempUseDefault) {
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = tempApiKey,
                                onValueChange = { tempApiKey = it },
                                label = { Text("Custom Gemini API Key", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_api_key_input"),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                        Icon(
                                            imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle visibility",
                                            tint = currentTheme.textColor.copy(alpha = 0.7f)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = currentTheme.textColor,
                                    unfocusedTextColor = currentTheme.textColor,
                                    focusedBorderColor = currentTheme.primaryColor,
                                    unfocusedBorderColor = currentTheme.primaryColor.copy(alpha = 0.3f),
                                    focusedContainerColor = currentTheme.cardColor,
                                    unfocusedContainerColor = currentTheme.cardColor
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    onApiKeyChange(tempApiKey, false)
                                    Toast.makeText(context, "Floppa AI Key Saved!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.secondaryColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Save Key", color = currentTheme.backgroundColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 3: Meme Theming Options
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = currentTheme.primaryColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Big Floppa Meme Themes",
                        color = currentTheme.textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FloppaThemeType.entries.forEach { themeItem ->
                        val isSelected = currentTheme == themeItem

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onThemeChange(themeItem) }
                                .testTag("theme_card_${themeItem.name}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) currentTheme.cardColor else currentTheme.surfaceColor
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) themeItem.primaryColor else themeItem.primaryColor.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Theme color palette preview dots
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(themeItem.primaryColor)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(themeItem.secondaryColor)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(themeItem.backgroundColor)
                                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = themeItem.title,
                                            color = currentTheme.textColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = themeItem.subtitle,
                                            color = currentTheme.textColor.copy(alpha = 0.65f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = themeItem.primaryColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Animated Meme Transitions
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Transform, contentDescription = null, tint = currentTheme.primaryColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Screen Switch Transitions",
                        color = currentTheme.textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MemeTransitionType.entries.forEach { transition ->
                        val isSelected = currentTransition == transition

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTransitionChange(transition) }
                                .testTag("transition_card_${transition.name}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) currentTheme.cardColor else currentTheme.surfaceColor
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) currentTheme.primaryColor else currentTheme.primaryColor.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(transition.iconEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = transition.title,
                                            color = currentTheme.textColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = transition.description,
                                            color = currentTheme.textColor.copy(alpha = 0.65f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = currentTheme.primaryColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 5: Sound Effects Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = currentTheme.primaryColor
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Floppa Soundboard Audio",
                                        color = currentTheme.textColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Synthesized caracal chirps, hisses & dumpling chomps",
                                        color = currentTheme.textColor.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = isSoundEnabled,
                                onCheckedChange = onSoundToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = currentTheme.backgroundColor,
                                    checkedTrackColor = currentTheme.primaryColor
                                )
                            )
                        }

                        if (isSoundEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { com.example.audio.FloppaSoundSynthesizer.playCaracalChirp() },
                                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primaryColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Test Chirp 🐱", color = currentTheme.backgroundColor, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { com.example.audio.FloppaSoundSynthesizer.playPelmeniChomp() },
                                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.secondaryColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Test Chomp 🥟", color = currentTheme.backgroundColor, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Section 6: Floppa Needs and Moods Feature (Virtual Pet Mode)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("floppa_needs_settings_card"),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🐾", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Floppa Needs and Moods",
                                    color = currentTheme.textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Take care of Floppa with 2 simple needs: Feeding Pelmeni 🥟 and Petting Gosha 🐾 to manage his royal mood.",
                                    color = currentTheme.textColor.copy(alpha = 0.65f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isNeedsEnabled,
                            onCheckedChange = onNeedsToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = currentTheme.backgroundColor,
                                checkedTrackColor = currentTheme.primaryColor
                            ),
                            modifier = Modifier.testTag("toggle_floppa_needs_switch")
                        )
                    }
                }
            }

            // Section 7: Experimental Option - Use Custom AI Provider with API Key
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_ai_provider_card"),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (tempCustomAiEnabled) Color(0xFFF59E0B) else currentTheme.primaryColor.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🧪", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Use Custom AI Provider with API Key",
                                        color = currentTheme.textColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "EXPERIMENTAL",
                                            color = Color(0xFFF59E0B),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            Switch(
                                checked = tempCustomAiEnabled,
                                onCheckedChange = {
                                    tempCustomAiEnabled = it
                                    onCustomAiProviderSave(it, tempCustomAiEndpoint, tempCustomAiKey, tempCustomAiModel)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = currentTheme.backgroundColor,
                                    checkedTrackColor = Color(0xFFF59E0B)
                                ),
                                modifier = Modifier.testTag("toggle_custom_ai_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Floppa will try to use your custom AI provider with your API key if it can and actually use it. If unreachable, Floppa falls back gracefully.",
                            color = currentTheme.textColor.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        if (tempCustomAiEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Endpoint URL
                            OutlinedTextField(
                                value = tempCustomAiEndpoint,
                                onValueChange = { tempCustomAiEndpoint = it },
                                label = { Text("Endpoint URL (OpenAI-compatible)", fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_ai_endpoint_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = currentTheme.textColor,
                                    unfocusedTextColor = currentTheme.textColor,
                                    focusedBorderColor = currentTheme.primaryColor,
                                    unfocusedBorderColor = currentTheme.primaryColor.copy(alpha = 0.3f),
                                    focusedContainerColor = currentTheme.cardColor,
                                    unfocusedContainerColor = currentTheme.cardColor
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Model Name
                            OutlinedTextField(
                                value = tempCustomAiModel,
                                onValueChange = { tempCustomAiModel = it },
                                label = { Text("Model Name (e.g. gpt-4o-mini, llama-3.1-8b)", fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_ai_model_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = currentTheme.textColor,
                                    unfocusedTextColor = currentTheme.textColor,
                                    focusedBorderColor = currentTheme.primaryColor,
                                    unfocusedBorderColor = currentTheme.primaryColor.copy(alpha = 0.3f),
                                    focusedContainerColor = currentTheme.cardColor,
                                    unfocusedContainerColor = currentTheme.cardColor
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom API Key
                            OutlinedTextField(
                                value = tempCustomAiKey,
                                onValueChange = { tempCustomAiKey = it },
                                label = { Text("Custom Provider API Key", fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_ai_key_input"),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (isCustomKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { isCustomKeyVisible = !isCustomKeyVisible }) {
                                        Icon(
                                            imageVector = if (isCustomKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle visibility",
                                            tint = currentTheme.textColor.copy(alpha = 0.7f)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = currentTheme.textColor,
                                    unfocusedTextColor = currentTheme.textColor,
                                    focusedBorderColor = currentTheme.primaryColor,
                                    unfocusedBorderColor = currentTheme.primaryColor.copy(alpha = 0.3f),
                                    focusedContainerColor = currentTheme.cardColor,
                                    unfocusedContainerColor = currentTheme.cardColor
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    onCustomAiProviderSave(true, tempCustomAiEndpoint, tempCustomAiKey, tempCustomAiModel)
                                    Toast.makeText(context, "Custom AI Provider Saved! Floppa will attempt to use it.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("save_custom_ai_provider_button")
                            ) {
                                Text("Save Custom Provider", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 8: Crash Prevention & Lag Watchdog
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lag_watchdog_card"),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Lag & Crash Watchdog",
                                    color = currentTheme.textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Monitors UI thread latency. If excessive lag occurs, halts everything to prevent an app crash and displays the recovery dialog.",
                                    color = currentTheme.textColor.copy(alpha = 0.65f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onSimulateLagOverload,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_lag_overload_button")
                        ) {
                            Text(
                                text = "Simulate Lag Overload (Test Emergency Pop Up)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

private fun openHomeSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Open Android Settings > Default apps > Home app to choose Floppa", Toast.LENGTH_LONG).show()
        }
    }
}
