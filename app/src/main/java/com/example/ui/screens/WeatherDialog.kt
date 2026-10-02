package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.WeatherNoteEntity
import com.example.data.model.FloppaThemeType
import com.example.data.model.WeatherData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeatherDialog(
    weather: WeatherData,
    notes: List<WeatherNoteEntity>,
    theme: FloppaThemeType,
    isLoading: Boolean,
    currentCity: String,
    currentOwmApiKey: String,
    useFahrenheit: Boolean,
    onRefresh: () -> Unit,
    onCityChange: (String) -> Unit,
    onOwmApiKeyChange: (String) -> Unit,
    onToggleUnit: () -> Unit,
    onAddNote: (String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var tempCityInput by remember(currentCity) { mutableStateOf(currentCity) }
    var tempKeyInput by remember(currentOwmApiKey) { mutableStateOf(currentOwmApiKey) }
    var newNoteInput by remember { mutableStateOf("") }
    var isConfigExpanded by remember { mutableStateOf(false) }
    var isKeyVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("weather_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = theme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Bar: Title, Source Badge, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(weather.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Floppa Weather & Notes",
                                color = theme.primaryColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Status pill (OWM vs Default)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (weather.isLiveOwm) Color(0xFF10B981) else theme.primaryColor)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (weather.isLiveOwm) "Live OpenWeatherMap" else "Default / Free Weather Service",
                                color = theme.textColor.copy(alpha = 0.65f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onRefresh, modifier = Modifier.size(34.dp)) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = theme.primaryColor,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = theme.primaryColor
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = theme.textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Main Weather Display Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("weather_display_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = weather.city,
                                            color = theme.textColor,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = weather.description,
                                            color = theme.textColor.copy(alpha = 0.75f),
                                            fontSize = 13.sp
                                        )
                                    }

                                    // Big Temp with Unit Toggle
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(theme.surfaceColor)
                                            .clickable { onToggleUnit() }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .testTag("toggle_weather_unit_button")
                                    ) {
                                        Text(
                                            text = if (useFahrenheit) weather.tempDisplayF else weather.tempDisplayC,
                                            color = theme.primaryColor,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (useFahrenheit) "°F" else "°C",
                                            color = theme.textColor.copy(alpha = 0.6f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Floppa Advice Banner
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(theme.surfaceColor.copy(alpha = 0.7f))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = weather.floppaWeatherAdvice,
                                        color = theme.textColor,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Telemetry Row: Humidity & Wind Speed
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Humidity: ${weather.humidity}%", fontSize = 12.sp, color = theme.textColor)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Air, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(String.format(Locale.getDefault(), "Wind: %.1f km/h", weather.windSpeedKmh), fontSize = 12.sp, color = theme.textColor)
                                    }
                                }
                            }
                        }
                    }

                    // Expandable City & OWM API Key Configuration
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isConfigExpanded = !isConfigExpanded },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.cardColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.secondaryColor.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.EditLocation, contentDescription = null, tint = theme.secondaryColor, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Set City & OWM API Key",
                                            color = theme.textColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = if (isConfigExpanded) "▲ Hide" else "▼ Configure",
                                        color = theme.primaryColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                AnimatedVisibility(visible = isConfigExpanded) {
                                    Column(modifier = Modifier.padding(top = 12.dp)) {
                                        // City Input
                                        OutlinedTextField(
                                            value = tempCityInput,
                                            onValueChange = { tempCityInput = it },
                                            label = { Text("City Name (e.g. London, Tokyo, Moscow)", fontSize = 11.sp) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("weather_city_input"),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = theme.textColor,
                                                unfocusedTextColor = theme.textColor,
                                                focusedBorderColor = theme.primaryColor,
                                                unfocusedBorderColor = theme.primaryColor.copy(alpha = 0.3f),
                                                focusedContainerColor = theme.surfaceColor,
                                                unfocusedContainerColor = theme.surfaceColor
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // OWM API Key Input
                                        OutlinedTextField(
                                            value = tempKeyInput,
                                            onValueChange = { tempKeyInput = it },
                                            label = { Text("OpenWeatherMap (OWM) API Key (Optional)", fontSize = 11.sp) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("owm_api_key_input"),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                            trailingIcon = {
                                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                                    Icon(
                                                        imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = "Toggle visibility",
                                                        tint = theme.textColor.copy(alpha = 0.7f)
                                                    )
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = theme.textColor,
                                                unfocusedTextColor = theme.textColor,
                                                focusedBorderColor = theme.primaryColor,
                                                unfocusedBorderColor = theme.primaryColor.copy(alpha = 0.3f),
                                                focusedContainerColor = theme.surfaceColor,
                                                unfocusedContainerColor = theme.surfaceColor
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = {
                                                onCityChange(tempCityInput)
                                                onOwmApiKeyChange(tempKeyInput)
                                                isConfigExpanded = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("save_weather_settings_button")
                                        ) {
                                            Text("Save City & Update Weather", color = theme.backgroundColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Notes Section
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📝", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Floppa Day & Weather Notes",
                                color = theme.primaryColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Add Note Input Bar
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newNoteInput,
                                onValueChange = { newNoteInput = it },
                                placeholder = {
                                    Text("Write a note (e.g. buy pelmeni, sunny walk)...", fontSize = 12.sp, color = theme.textColor.copy(alpha = 0.5f))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_weather_note_input"),
                                shape = RoundedCornerShape(14.dp),
                                maxLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textColor,
                                    unfocusedTextColor = theme.textColor,
                                    focusedBorderColor = theme.primaryColor,
                                    unfocusedBorderColor = theme.primaryColor.copy(alpha = 0.25f),
                                    focusedContainerColor = theme.cardColor,
                                    unfocusedContainerColor = theme.cardColor
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newNoteInput.isNotBlank()) {
                                            onAddNote(newNoteInput)
                                            newNoteInput = ""
                                        }
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (newNoteInput.isNotBlank()) {
                                        onAddNote(newNoteInput)
                                        newNoteInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (newNoteInput.isNotBlank()) theme.primaryColor else theme.cardColor)
                                    .testTag("submit_weather_note_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Note",
                                    tint = if (newNoteInput.isNotBlank()) theme.backgroundColor else theme.textColor.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    // Saved Notes List
                    if (notes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No notes recorded yet. Add your daily caracal notes above!",
                                    color = theme.textColor.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        items(notes, key = { it.id }) { noteItem ->
                            WeatherNoteCard(
                                note = noteItem,
                                theme = theme,
                                onDelete = { onDeleteNote(noteItem.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", color = theme.textColor, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherNoteCard(
    note: WeatherNoteEntity,
    theme: FloppaThemeType,
    onDelete: () -> Unit
) {
    val dateStr = remember(note.timestamp) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(note.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weather_note_${note.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.note,
                    color = theme.textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.weatherCondition.isNotBlank()) {
                        Text(
                            text = note.weatherCondition,
                            color = theme.primaryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(" • ", color = theme.textColor.copy(alpha = 0.4f), fontSize = 10.sp)
                    }
                    Text(
                        text = dateStr,
                        color = theme.textColor.copy(alpha = 0.55f),
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Note",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
