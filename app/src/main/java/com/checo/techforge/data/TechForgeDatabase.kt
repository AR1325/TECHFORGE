package com.checo.techforge.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        LabRecordEntity::class,
        SavedDeviceEntity::class,
        CommandEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class TechForgeDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun labRecordDao(): LabRecordDao
    abstract fun savedDeviceDao(): SavedDeviceDao
    abstract fun commandDao(): CommandDao

    companion object {
        @Volatile private var INSTANCE: TechForgeDatabase? = null

        fun getInstance(context: Context): TechForgeDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TechForgeDatabase::class.java,
                    "techforge.db"
                ).build().also { INSTANCE = it }
            }
    }
}
