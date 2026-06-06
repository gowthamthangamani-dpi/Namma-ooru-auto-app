package com.example.data.repository

import com.example.data.dao.CommuteDao
import com.example.data.dao.RideDao
import com.example.data.model.BookedRide
import com.example.data.model.FrequentCommute
import kotlinx.coroutines.flow.Flow

class AutoRepository(
    private val commuteDao: CommuteDao,
    private val rideDao: RideDao
) {
    val allCommutes: Flow<List<FrequentCommute>> = commuteDao.getAllCommutes()
    val allRides: Flow<List<BookedRide>> = rideDao.getAllRides()
    val activeRide: Flow<BookedRide?> = rideDao.getActiveRideFlow()

    suspend fun insertCommute(commute: FrequentCommute) {
        commuteDao.insertCommute(commute)
    }

    suspend fun deleteCommute(commute: FrequentCommute) {
        commuteDao.deleteCommute(commute)
    }

    suspend fun ensureSeededCommutes() {
        val count = commuteDao.getCount()
        if (count == 0) {
            val predefined = listOf(
                FrequentCommute(
                    title = "Home 🏠 to Central Bus Stand 🚌",
                    pickup = "Nellai Nagar Area, Sogathur Path, Dharmapuri",
                    dropoff = "Dharmapuri Central Bus Stand, Dharmapuri",
                    distance = 2.1,
                    estimatedMinutes = 8,
                    isPredefined = true
                ),
                FrequentCommute(
                    title = "House 🏡 to Govt Medical College 🏥",
                    pickup = "Bharathipuram Housing Board, Dharmapuri",
                    dropoff = "Govt Medical College Hospital, Dharmapuri",
                    distance = 3.5,
                    estimatedMinutes = 12,
                    isPredefined = true
                ),
                FrequentCommute(
                    title = "Railway Station 🚄 to Collectorate 🏢",
                    pickup = "Dharmapuri Railway Station (DPJ), Dharmapuri",
                    dropoff = "Dharmapuri District Collector Office, Dharmapuri",
                    distance = 4.2,
                    estimatedMinutes = 15,
                    isPredefined = true
                ),
                FrequentCommute(
                    title = "Sogathur Petrol Bunk ⛽ to Adhiyaman Kottai 🛕",
                    pickup = "Sogathur Petrol Bunk Road, Dharmapuri",
                    dropoff = "Adhiyaman Kottai Dakshina Kashi Kalabhairavar Temple, Dharmapuri",
                    distance = 6.8,
                    estimatedMinutes = 20,
                    isPredefined = true
                )
            )
            for (commute in predefined) {
                commuteDao.insertCommute(commute)
            }
        }
    }

    suspend fun insertRide(ride: BookedRide): Long {
        return rideDao.insertRide(ride)
    }

    suspend fun updateRideStatus(rideId: Int, status: String) {
        rideDao.updateRideStatus(rideId, status)
    }

    suspend fun submitFeedback(rideId: Int, rating: Int, feedback: String?, tipAmount: Double) {
        rideDao.submitFeedback(rideId, rating, feedback, tipAmount)
    }

    suspend fun deleteRide(ride: BookedRide) {
        rideDao.deleteRide(ride)
    }

    suspend fun clearHistory() {
        rideDao.clearAll()
    }

    suspend fun getActiveRideDirectly(): BookedRide? {
        return rideDao.getActiveRide()
    }
}
