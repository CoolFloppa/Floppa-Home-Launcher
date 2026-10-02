package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_apps")
data class PinnedAppEntity(
    @PrimaryKey val packageName: String,
    val position: Int,
    val isDock: Boolean = false,
    val customLabel: String? = null
)

@Entity(tableName = "custom_icons")
data class CustomIconOverrideEntity(
    @PrimaryKey val packageName: String,
    val customIconType: String, // "BUILTIN_MEME" or "CUSTOM_IMAGE_URI"
    val customIconRef: String   // Built-in id or URI string
)

@Entity(tableName = "chat_messages")
data class FloppaChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isUser: Boolean,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "launcher_settings")
data class FloppaSettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "weather_notes")
data class WeatherNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val note: String,
    val cityName: String = "",
    val weatherCondition: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
