package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "frequent_commutes")
data class FrequentCommute(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val pickup: String,
    val dropoff: String,
    val distance: Double, // in km
    val estimatedMinutes: Int,
    val isPredefined: Boolean = false
)
