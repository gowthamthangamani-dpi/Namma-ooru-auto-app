package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "booked_rides")
data class BookedRide(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pickup: String,
    val dropoff: String,
    val fare: Double,
    val autoType: String,
    val driverName: String,
    val driverPhone: String,
    val driverVehicleNo: String,
    val otp: String,
    val status: String, // "SEARCHING", "ACCEPTED", "ARRIVED", "STARTED", "COMPLETED", "CANCELLED"
    val paymentMethod: String = "Cash",
    val timestamp: Long = System.currentTimeMillis(),
    val rating: Int? = null,
    val feedback: String? = null,
    val tipAmount: Double = 0.0
)
