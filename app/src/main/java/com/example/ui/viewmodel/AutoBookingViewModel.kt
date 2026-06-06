package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AutoDatabase
import com.example.data.model.BookedRide
import com.example.data.model.FrequentCommute
import com.example.data.repository.AutoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class AutoBookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AutoRepository
    val allCommutes: StateFlow<List<FrequentCommute>>
    val allRides: StateFlow<List<BookedRide>>
    val activeRide: StateFlow<BookedRide?>

    // Form inputs for booking
    var pickupState by mutableStateOf("")
    var dropoffState by mutableStateOf("")
    var distanceState by mutableStateOf(0.0)
    var estimatedPriceState by mutableStateOf(0.0)
    var selectedAutoTypeState by mutableStateOf("Standard Yellow Auto") // "Standard Yellow Auto", "Electric Namma Auto ⚡", "Share Auto 🛺"
    var selectedPaymentMethodState by mutableStateOf("Cash") // "Cash", "UPI", "Digital Wallet"

    // Detailed interactive fare calculator states
    var isNightCharge by mutableStateOf(false)
    var trafficLevel by mutableStateOf("Medium") // "Low", "Medium", "Heavy"
    var luggageChargeEnabled by mutableStateOf(false)

    var baseFareComponent by mutableStateOf(0.0)
    var distanceFareComponent by mutableStateOf(0.0)
    var trafficSurchargeComponent by mutableStateOf(0.0)
    var luggageSurchargeComponent by mutableStateOf(0.0)
    var nightSurchargeComponent by mutableStateOf(0.0)

    // Active Tab state
    var selectedTabState by mutableStateOf(0) // 0: Book Ride, 1: My Commutes, 2: History

    // Map Simulation state (Coordinates 0-1000 range for canvas)
    var mapPickupX by mutableStateOf(250f)
    var mapPickupY by mutableStateOf(650f)
    var mapDropoffX by mutableStateOf(750f)
    var mapDropoffY by mutableStateOf(250f)
    var mapDriverX by mutableStateOf(100f)
    var mapDriverY by mutableStateOf(100f)
    var simulationProgress by mutableStateOf(0f) // 0.0 to 1.0
    var trackingMessage by mutableStateOf("Ready to commute")

    // Nearby autos for map simulation
    var nearbyAutos by mutableStateOf(listOf<Pair<Float, Float>>())

    // Feedback popup trigger after active completion
    var showRatingDialogForRideId by mutableStateOf<Int?>(null)

    // SOS alert state variables
    var sosActiveState by mutableStateOf(false)
    var showSosAlertDialog by mutableStateOf(false)

    fun triggerSosAlert() {
        sosActiveState = true
        showSosAlertDialog = true
    }

    fun dismissSosAlert() {
        showSosAlertDialog = false
    }

    fun cancelSosEmergency() {
        sosActiveState = false
        showSosAlertDialog = false
    }

    // Current active background simulation job
    private var simulationJob: Job? = null

    // Predefined Kannada and driver details list for realistic matches
    private val driverNames = listOf(
        "Manjunath K.", "Ramesh Gowda", "Chethan Kumar", "Anand Swamy", 
        "Siddaraju M.", "Naveen Shetty", "Kemparaiah V.", "Sunil R."
    )
    private val vehicles = listOf(
        "KA-03-EQ-4512", "KA-01-FF-8239", "KA-05-MK-9102", "KA-02-JH-3814",
        "KA-51-AB-1122", "KA-04-ZY-6754", "KA-41-HG-8843", "KA-03-MN-7123"
    )

    init {
        val db = AutoDatabase.getDatabase(application)
        repository = AutoRepository(db.commuteDao(), db.rideDao())
        
        allCommutes = repository.allCommutes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        allRides = repository.allRides.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        activeRide = repository.activeRide.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        viewModelScope.launch {
            repository.ensureSeededCommutes()
            generateNearbyAutos()
            resumeOngoingSimulationIfNeeded()
        }
    }

    private fun generateNearbyAutos() {
        // Generate random points near center
        val list = mutableListOf<Pair<Float, Float>>()
        repeat(5) {
            list.add(Pair(Random.nextFloat() * 800f + 100f, Random.nextFloat() * 800f + 100f))
        }
        nearbyAutos = list
    }

    private fun resumeOngoingSimulationIfNeeded() {
        viewModelScope.launch {
            val ongoing = repository.getActiveRideDirectly()
            if (ongoing != null) {
                // Determine ride status and start/continue simulation matching
                when (ongoing.status) {
                    "SEARCHING", "ACCEPTED", "ARRIVED", "STARTED" -> {
                        startSimulationLoop(ongoing)
                    }
                }
            }
        }
    }

    fun onPickupChange(value: String) {
        pickupState = value
        estimateFare()
    }

    fun onDropoffChange(value: String) {
        dropoffState = value
        estimateFare()
    }

    fun selectCommuteShortcut(commute: FrequentCommute) {
        pickupState = commute.pickup
        dropoffState = commute.dropoff
        distanceState = commute.distance
        estimateFare()
        // Randomize pick/drop coordinates mildly so paths look different
        mapPickupX = 200f + Random.nextInt(150).toFloat()
        mapPickupY = 550f + Random.nextInt(150).toFloat()
        mapDropoffX = 600f + Random.nextInt(200).toFloat()
        mapDropoffY = 150f + Random.nextInt(150).toFloat()
        generateNearbyAutos()
    }

    fun toggleNightCharge() {
        isNightCharge = !isNightCharge
        estimateFare()
    }

    fun setTraffic(level: String) {
        trafficLevel = level
        estimateFare()
    }

    fun toggleLuggage() {
        luggageChargeEnabled = !luggageChargeEnabled
        estimateFare()
    }

    // Direct distance adjustment for interactive testing helper
    fun adjustDistance(delta: Double) {
        if (pickupState.isBlank() || dropoffState.isBlank()) {
            pickupState = "Nellai Nagar, Dharmapuri"
            dropoffState = "Dharmapuri Bus Stand"
        }
        val current = if (distanceState == 0.0) {
            val combinedLength = (pickupState.length + dropoffState.length).toDouble()
            ((combinedLength % 8) + 1.5).roundTo(1)
        } else {
            distanceState
        }
        val newVal = (current + delta).coerceAtLeast(0.5).roundTo(1)
        distanceState = newVal
        estimateFare()
    }

    private fun estimateFare() {
        if (pickupState.isBlank() || dropoffState.isBlank()) {
            distanceState = 0.0
            estimatedPriceState = 0.0
            baseFareComponent = 0.0
            distanceFareComponent = 0.0
            trafficSurchargeComponent = 0.0
            luggageSurchargeComponent = 0.0
            nightSurchargeComponent = 0.0
            return
        }
        
        // Generate a deterministic distance based on lengths if it's not a preset commute
        if (distanceState == 0.0) {
            val combinedLength = (pickupState.length + dropoffState.length).toDouble()
            distanceState = ((combinedLength % 8) + 1.5).roundTo(1)
        }

        val baseFare = when (selectedAutoTypeState) {
            "Electric Namma Auto ⚡" -> 35.0 // Clean energy promo
            "Share Auto 🛺" -> 15.0 // Economical split
            else -> 30.0 // Standard M3 Auto
        }
        val perKm = when (selectedAutoTypeState) {
            "Electric Namma Auto ⚡" -> 13.0
            "Share Auto 🛺" -> 7.0
            else -> 15.0
        }

        // Base distance covered by baseFare
        val minBaseDistance = 1.5
        
        val calculatedBase: Double
        val calculatedDist: Double
        if (distanceState <= minBaseDistance) {
            calculatedBase = baseFare
            calculatedDist = 0.0
        } else {
            calculatedBase = baseFare
            calculatedDist = (distanceState - minBaseDistance) * perKm
        }

        val trafficFee = when (trafficLevel) {
            "Heavy" -> 15.0
            "Medium" -> 5.0
            else -> 0.0
        }

        val luggageFee = if (luggageChargeEnabled) 10.0 else 0.0

        val subTotal = calculatedBase + calculatedDist + trafficFee + luggageFee
        val nightFee = if (isNightCharge) (subTotal * 0.5) else 0.0

        baseFareComponent = calculatedBase.roundTo(1)
        distanceFareComponent = calculatedDist.roundTo(1)
        trafficSurchargeComponent = trafficFee.roundTo(1)
        luggageSurchargeComponent = luggageFee.roundTo(1)
        nightSurchargeComponent = nightFee.roundTo(1)

        estimatedPriceState = (subTotal + nightFee).roundTo(1)
    }

    fun selectAutoType(type: String) {
        selectedAutoTypeState = type
        estimateFare()
    }

    fun bookRide() {
        if (pickupState.isBlank() || dropoffState.isBlank()) return

        val randDriverIndex = Random.nextInt(driverNames.size)
        val randVehicleIndex = Random.nextInt(vehicles.size)
        val otpCode = (1000 + Random.nextInt(9000)).toString()

        val ride = BookedRide(
            pickup = pickupState,
            dropoff = dropoffState,
            fare = estimatedPriceState,
            autoType = selectedAutoTypeState,
            driverName = driverNames[randDriverIndex],
            driverPhone = "+91 9886${100000 + Random.nextInt(899999)}",
            driverVehicleNo = vehicles[randVehicleIndex],
            otp = otpCode,
            status = "SEARCHING",
            paymentMethod = selectedPaymentMethodState
        )

        viewModelScope.launch {
            val id = repository.insertRide(ride)
            val fullRide = ride.copy(id = id.toInt())
            startSimulationLoop(fullRide)
        }
    }

    fun cancelActiveRide() {
        simulationJob?.cancel()
        simulationJob = null
        trackingMessage = "Ride cancelled"
        viewModelScope.launch {
            val active = repository.getActiveRideDirectly()
            if (active != null) {
                repository.updateRideStatus(active.id, "CANCELLED")
            }
        }
    }

    fun submitFeedback(rating: Int, feedback: String, tipAmount: Double) {
        val rideId = showRatingDialogForRideId
        if (rideId != null) {
            viewModelScope.launch {
                repository.submitFeedback(rideId, rating, feedback, tipAmount)
                showRatingDialogForRideId = null
            }
        }
    }

    fun addCustomCommute(title: String, pickup: String, dropoff: String) {
        if (title.isBlank() || pickup.isBlank() || dropoff.isBlank()) return
        viewModelScope.launch {
            val combinedLength = (pickup.length + dropoff.length).toDouble()
            val distance = ((combinedLength % 6) + 1.2).roundTo(1)
            val minutes = (distance * 3.5).toInt().coerceAtLeast(4)

            val custom = FrequentCommute(
                title = title,
                pickup = pickup,
                dropoff = dropoff,
                distance = distance,
                estimatedMinutes = minutes,
                isPredefined = false
            )
            repository.insertCommute(custom)
        }
    }

    fun deleteCommute(commute: FrequentCommute) {
        viewModelScope.launch {
            repository.deleteCommute(commute)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Active trip flow simulator
    private fun startSimulationLoop(ride: BookedRide) {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            var currentStatus = ride.status
            var currentRideId = ride.id

            // Step 1: SEARCHING (Match driver)
            if (currentStatus == "SEARCHING") {
                trackingMessage = "Contacting nearby Namma Autos... 🛺"
                delay(3000)
                currentStatus = "ACCEPTED"
                repository.updateRideStatus(currentRideId, currentStatus)
                // Initialize driver coordinates away from pickup
                mapDriverX = mapPickupX - 150f
                mapDriverY = mapPickupY - 120f
                simulationProgress = 0f
            }

            // Step 2: ACCEPTED (Driver is driving to pickup point)
            if (currentStatus == "ACCEPTED") {
                trackingMessage = "Driver ${ride.driverName} matched! Arriving soon."
                val startX = mapDriverX
                val startY = mapDriverY
                val steps = 40
                for (i in 1..steps) {
                    delay(100)
                    val fraction = i.toFloat() / steps
                    mapDriverX = startX + (mapPickupX - startX) * fraction
                    mapDriverY = startY + (mapPickupY - startY) * fraction
                    simulationProgress = fraction
                }
                currentStatus = "ARRIVED"
                repository.updateRideStatus(currentRideId, currentStatus)
                simulationProgress = 0f
            }

            // Step 3: ARRIVED (Waiting for OTP details or start click)
            if (currentStatus == "ARRIVED") {
                trackingMessage = "Namma Auto arrived! OTP is ${ride.otp}."
                // Wait for either a automatic progress trigger or user starts ride
                // We let it automatically transition after a friendly 6-second delay, or instantly if started by user
                delay(6000)
                currentStatus = "STARTED"
                repository.updateRideStatus(currentRideId, currentStatus)
                simulationProgress = 0f
            }

            // Step 4: STARTED (Driving from pickup to dropoff)
            if (currentStatus == "STARTED") {
                trackingMessage = "On the way! Shubhayati... 🛣️"
                val steps = 80
                for (i in 1..steps) {
                    delay(150)
                    val fraction = i.toFloat() / steps
                    mapDriverX = mapPickupX + (mapDropoffX - mapPickupX) * fraction
                    mapDriverY = mapPickupY + (mapDropoffY - mapPickupY) * fraction
                    simulationProgress = fraction
                }
                currentStatus = "COMPLETED"
                repository.updateRideStatus(currentRideId, currentStatus)
                trackingMessage = "Ride completed successfully!"
                showRatingDialogForRideId = currentRideId
                
                // Reset states
                cleanupSessionInputStates()
            }
        }
    }

    // Force transition if user clicks Start Ride earlier
    fun forceStartTrip() {
        viewModelScope.launch {
            val ongoing = repository.getActiveRideDirectly()
            if (ongoing != null && ongoing.status == "ARRIVED") {
                repository.updateRideStatus(ongoing.id, "STARTED")
                // Restart simulation loop so it picks up the STARTED block instantly
                val updated = ongoing.copy(status = "STARTED")
                startSimulationLoop(updated)
            }
        }
    }

    // Calculate distance and ETA to pickup point
    fun getPickupEtaAndDistance(): Pair<String, String> {
        val dx = mapDriverX - mapPickupX
        val dy = mapDriverY - mapPickupY
        val pixelDist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
        
        // 100 pixels = 1.0 km. Convert to km.
        val distanceKm = pixelDist / 120.0
        val etaMinutes = (distanceKm * 2.5).coerceAtLeast(0.0) // 2.5 minutes per km
        
        val distanceStr = if (distanceKm < 0.1) {
            "${(distanceKm * 1000).roundToInt()} m"
        } else {
            "${distanceKm.roundTo(1)} km"
        }
        
        val etaStr = if (etaMinutes < 0.8) {
            "Arrived"
        } else {
            "${etaMinutes.roundToInt()} mins"
        }
        
        return Pair(etaStr, distanceStr)
    }

    // Calculate distance and ETA to dropoff point
    fun getDropoffEtaAndDistance(): Pair<String, String> {
        val dx = mapDriverX - mapDropoffX
        val dy = mapDriverY - mapDropoffY
        val pixelDist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
        
        // 100 pixels = 1.0 km.
        val distanceKm = pixelDist / 120.0
        val etaMinutes = (distanceKm * 2.5).coerceAtLeast(0.0)
        
        val distanceStr = if (distanceKm < 0.1) {
            "${(distanceKm * 1000).roundToInt()} m"
        } else {
            "${distanceKm.roundTo(1)} km"
        }
        
        val etaStr = if (etaMinutes < 0.8) {
            "Arrived"
        } else {
            "${etaMinutes.roundToInt()} mins"
        }
        
        return Pair(etaStr, distanceStr)
    }

    private fun cleanupSessionInputStates() {
        pickupState = ""
        dropoffState = ""
        distanceState = 0.0
        estimatedPriceState = 0.0
        isNightCharge = false
        trafficLevel = "Medium"
        luggageChargeEnabled = false
        baseFareComponent = 0.0
        distanceFareComponent = 0.0
        trafficSurchargeComponent = 0.0
        luggageSurchargeComponent = 0.0
        nightSurchargeComponent = 0.0
        sosActiveState = false
        showSosAlertDialog = false
    }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (this * multiplier).roundToInt() / multiplier
    }
}
