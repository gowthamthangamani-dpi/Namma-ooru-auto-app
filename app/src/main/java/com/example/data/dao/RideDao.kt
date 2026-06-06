package com.example.data.dao

import androidx.room.*
import com.example.data.model.BookedRide
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {
    @Query("SELECT * FROM booked_rides ORDER BY timestamp DESC")
    fun getAllRides(): Flow<List<BookedRide>>

    @Query("SELECT * FROM booked_rides WHERE id = :rideId LIMIT 1")
    suspend fun getRideById(rideId: Int): BookedRide?

    @Query("SELECT * FROM booked_rides WHERE status != 'COMPLETED' AND status != 'CANCELLED' ORDER BY timestamp DESC LIMIT 1")
    fun getActiveRideFlow(): Flow<BookedRide?>

    @Query("SELECT * FROM booked_rides WHERE status != 'COMPLETED' AND status != 'CANCELLED' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getActiveRide(): BookedRide?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: BookedRide): Long

    @Query("UPDATE booked_rides SET status = :status WHERE id = :rideId")
    suspend fun updateRideStatus(rideId: Int, status: String)

    @Query("UPDATE booked_rides SET rating = :rating, feedback = :feedback, tipAmount = :tipAmount WHERE id = :rideId")
    suspend fun submitFeedback(rideId: Int, rating: Int, feedback: String?, tipAmount: Double)

    @Delete
    suspend fun deleteRide(ride: BookedRide)

    @Query("DELETE FROM booked_rides")
    suspend fun clearAll()
}
