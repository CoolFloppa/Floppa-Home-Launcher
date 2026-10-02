package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PinnedAppEntity::class,
        CustomIconOverrideEntity::class,
        FloppaChatMessageEntity::class,
        FloppaSettingsEntity::class,
        WeatherNoteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FloppaDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: FloppaDatabase? = null

        fun getInstance(context: Context): FloppaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FloppaDatabase::class.java,
                    "floppa_launcher.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
