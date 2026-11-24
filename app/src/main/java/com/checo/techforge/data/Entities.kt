package com.checo.techforge.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "lab_records")
data class LabRecordEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val description: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "saved_devices")
data class SavedDeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val manufacturer: String,
    val model: String,
    val platform: String,
    val notes: String,
    val createdAt: Long
)

@Entity(tableName = "commands")
data class CommandEntity(
    @PrimaryKey val id: String,
    val title: String,
    val platform: String,
    val category: String,
    val command: String,
    val explanation: String,
    val risk: String
)
