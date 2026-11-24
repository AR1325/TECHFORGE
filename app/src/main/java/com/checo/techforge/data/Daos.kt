package com.checo.techforge.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    suspend fun getAll(): List<ProjectEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ProjectEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ProjectEntity>)
    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: String)
    @Query("DELETE FROM projects")
    suspend fun clear()
}

@Dao
interface LabRecordDao {
    @Query("SELECT * FROM lab_records ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<LabRecordEntity>>
    @Query("SELECT * FROM lab_records ORDER BY updatedAt DESC")
    suspend fun getAll(): List<LabRecordEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: LabRecordEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<LabRecordEntity>)
    @Query("DELETE FROM lab_records WHERE id = :id")
    suspend fun delete(id: String)
    @Query("DELETE FROM lab_records")
    suspend fun clear()
}

@Dao
interface SavedDeviceDao {
    @Query("SELECT * FROM saved_devices ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SavedDeviceEntity>>
    @Query("SELECT * FROM saved_devices ORDER BY createdAt DESC")
    suspend fun getAll(): List<SavedDeviceEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SavedDeviceEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SavedDeviceEntity>)
    @Query("DELETE FROM saved_devices WHERE id = :id")
    suspend fun delete(id: String)
    @Query("DELETE FROM saved_devices")
    suspend fun clear()
}

@Dao
interface CommandDao {
    @Query("SELECT * FROM commands ORDER BY platform, title")
    fun observeAll(): Flow<List<CommandEntity>>
    @Query("SELECT * FROM commands ORDER BY platform, title")
    suspend fun getAll(): List<CommandEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CommandEntity>)
    @Query("DELETE FROM commands")
    suspend fun clear()
}
