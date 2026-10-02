package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Pinned Apps ---
    @Query("SELECT * FROM pinned_apps ORDER BY position ASC")
    fun getAllPinnedApps(): Flow<List<PinnedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPinnedApp(app: PinnedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPinnedApps(apps: List<PinnedAppEntity>)

    @Query("DELETE FROM pinned_apps WHERE packageName = :packageName")
    suspend fun deletePinnedApp(packageName: String)

    @Query("DELETE FROM pinned_apps")
    suspend fun clearPinnedApps()

    // --- Custom Icon Overrides ---
    @Query("SELECT * FROM custom_icons")
    fun getAllCustomIcons(): Flow<List<CustomIconOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomIcon(override: CustomIconOverrideEntity)

    @Query("DELETE FROM custom_icons WHERE packageName = :packageName")
    suspend fun deleteCustomIcon(packageName: String)

    @Query("DELETE FROM custom_icons")
    suspend fun clearCustomIcons()

    // --- Floppa Chat Messages ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<FloppaChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: FloppaChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    // --- Launcher Settings ---
    @Query("SELECT * FROM launcher_settings")
    fun getAllSettings(): Flow<List<FloppaSettingsEntity>>

    @Query("SELECT value FROM launcher_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: FloppaSettingsEntity)

    // --- Weather Notes ---
    @Query("SELECT * FROM weather_notes ORDER BY timestamp DESC")
    fun getAllWeatherNotes(): Flow<List<WeatherNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherNote(note: WeatherNoteEntity)

    @Query("DELETE FROM weather_notes WHERE id = :id")
    suspend fun deleteWeatherNote(id: Long)
}
