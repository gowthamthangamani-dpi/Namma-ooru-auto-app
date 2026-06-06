package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.BookedRide
import com.example.data.model.FrequentCommute
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoDarkText
import com.example.ui.theme.BentoGreen
import com.example.ui.theme.BentoYellow
import com.example.ui.theme.BentoLightGreen
import com.example.ui.theme.BentoSoftRed
import com.example.ui.theme.BentoDarkRed
import com.example.ui.theme.BentoGreyBorder
import com.example.ui.viewmodel.AutoBookingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NammaAutoDashboard()
                }
            }
        }
    }
}

@Composable
fun NammaAutoDashboard(
    viewModel: AutoBookingViewModel = viewModel()
) {
    val commutes by viewModel.allCommutes.collectAsStateWithLifecycle()
    val ridesHistory by viewModel.allRides.collectAsStateWithLifecycle()
    val activeRide by viewModel.activeRide.collectAsStateWithLifecycle()

    var customTitle by remember { mutableStateOf("") }
    var customPickup by remember { mutableStateOf("") }
    var customDropoff by remember { mutableStateOf("") }
    var isAddingCommute by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_nav_bar")
                    .navigationBarsPadding(),
                containerColor = Color(0xFFF3F4F1),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Place, contentDescription = "Book") },
                    label = { Text("Book Auto") },
                    selected = viewModel.selectedTabState == 0,
                    onClick = { viewModel.selectedTabState = 0 },
                    modifier = Modifier.testTag("tab_book"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoGreen,
                        selectedTextColor = BentoGreen,
                        indicatorColor = BentoLightGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "My Commutes") },
                    label = { Text("My Commutes") },
                    selected = viewModel.selectedTabState == 1,
                    onClick = { viewModel.selectedTabState = 1 },
                    modifier = Modifier.testTag("tab_commutes"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoGreen,
                        selectedTextColor = BentoGreen,
                        indicatorColor = BentoLightGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "History") },
                    label = { Text("History") },
                    selected = viewModel.selectedTabState == 2,
                    onClick = { viewModel.selectedTabState = 2 },
                    modifier = Modifier.testTag("tab_history"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BentoGreen,
                        selectedTextColor = BentoGreen,
                        indicatorColor = BentoLightGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (viewModel.selectedTabState) {
                0 -> {
                    // Book Ride Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Section resembling Bento: Location + Avatar
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Current Location",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoGreen,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Location Pin",
                                        tint = BentoGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (viewModel.pickupState.isNotBlank()) viewModel.pickupState.substringBefore(",") else "Nellai Nagar, Dharmapuri",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp,
                                        color = BentoDarkText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            // Profile Avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8DEF8))
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color(0xFF1D192B)
                                )
                            }
                        }

                        // Simulated Map Card (Crucial Visual anchor)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        ) {
                            NammaAutoMap(
                                activeRide = activeRide,
                                pickupX = viewModel.mapPickupX,
                                pickupY = viewModel.mapPickupY,
                                dropoffX = viewModel.mapDropoffX,
                                dropoffY = viewModel.mapDropoffY,
                                driverX = viewModel.mapDriverX,
                                driverY = viewModel.mapDriverY,
                                hasPickup = viewModel.pickupState.isNotBlank(),
                                hasDropoff = viewModel.dropoffState.isNotBlank(),
                                nearbyAutos = viewModel.nearbyAutos,
                                viewModel = viewModel
                            )

                            // Overlay showing map statuses
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .padding(12.dp)
                                    .align(Alignment.TopStart)
                                    .shadow(2.dp, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (activeRide != null) MaterialTheme.colorScheme.secondary 
                                                else Color(0xFFFFC72C)
                                            )
                                    )
                                    Text(
                                        text = viewModel.trackingMessage,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Ride booking inputs / Active ride monitor
                        if (activeRide == null) {
                            // Booking flow screen
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Dharmapuri Landmarks Quick Selector
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "📍 Dharmapuri Landmarks Node Map",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = BentoDarkText
                                    )
                                    Text(
                                        text = "Tap a major hub to locate and set routes instantly:",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        dharmapuriLandmarks.forEach { landmark ->
                                            val isSelected = viewModel.pickupState.contains(landmark.name) || viewModel.dropoffState.contains(landmark.name)
                                            Card(
                                                modifier = Modifier
                                                    .clickable {
                                                        if (viewModel.pickupState.isBlank() || viewModel.pickupState.contains(landmark.name)) {
                                                            viewModel.onPickupChange(landmark.fullName)
                                                            viewModel.mapPickupX = landmark.gridX
                                                            viewModel.mapPickupY = landmark.gridY
                                                            Toast.makeText(context, "🟢 Set Pickup to ${landmark.name}! Select drop-off next.", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            viewModel.onDropoffChange(landmark.fullName)
                                                            viewModel.mapDropoffX = landmark.gridX
                                                            viewModel.mapDropoffY = landmark.gridY
                                                            Toast.makeText(context, "🔴 Set Drop-off to ${landmark.name}!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                    .testTag("landmark_chip_${landmark.id}"),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFFBFCFB)
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = 1.dp,
                                                    color = if (isSelected) BentoGreen else BentoGreyBorder
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(landmark.icon, fontSize = 14.sp)
                                                    Text(
                                                        text = landmark.name,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = BentoDarkText
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Shortcuts header & Row
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "⚡ Frequent Commutes shortcuts",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    if (commutes.isEmpty()) {
                                        Text(
                                            text = "No saved shortcuts. Add them in My Commutes tab!",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            commutes.take(3).forEach { commute ->
                                                Card(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            viewModel.selectCommuteShortcut(commute)
                                                            Toast.makeText(context, "Filled: ${commute.title}", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .testTag("shortcut_${commute.id}"),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                                    ),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(8.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = commute.title,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "${commute.distance} km • ${commute.estimatedMinutes}m",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Bento Focus Search Card
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, BentoGreyBorder, RoundedCornerShape(28.dp)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(28.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        // "Where to, Anna?" Header accent
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF0F1EF), RoundedCornerShape(16.dp))
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search icon",
                                                tint = BentoGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Where to, Anna?",
                                                color = Color(0xFF404944),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        // Pick-up Input
                                        OutlinedTextField(
                                            value = viewModel.pickupState,
                                            onValueChange = { viewModel.onPickupChange(it) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("pickup_input"),
                                            label = { Text("Pickup Location", fontWeight = FontWeight.Medium) },
                                            placeholder = { Text("e.g. Nellai Nagar, Dharmapuri") },
                                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = "Pickup", tint = BentoGreen, modifier = Modifier.size(18.dp)) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BentoGreen,
                                                unfocusedBorderColor = BentoGreyBorder,
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent
                                            )
                                        )

                                        // Drop-off Input
                                        OutlinedTextField(
                                            value = viewModel.dropoffState,
                                            onValueChange = { viewModel.onDropoffChange(it) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("dropoff_input"),
                                            label = { Text("Dropoff Location", fontWeight = FontWeight.Medium) },
                                            placeholder = { Text("e.g. Dharmapuri Bus Stand") },
                                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Dropoff", tint = BentoGreen, modifier = Modifier.size(18.dp)) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BentoGreen,
                                                unfocusedBorderColor = BentoGreyBorder,
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent
                                            )
                                        )

                                        // Bento quick destinations (Home / Work) row at the bottom
                                        HorizontalDivider(color = BentoGreyBorder, thickness = 1.dp)

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceAround,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        viewModel.onPickupChange("Nellai Nagar Area, Sogathur Path, Dharmapuri")
                                                        viewModel.onDropoffChange("Dharmapuri Central Bus Stand, Dharmapuri")
                                                        Toast.makeText(context, "Prefilled Home destination!", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(BentoLightGreen),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Home, contentDescription = "Home", tint = BentoGreen, modifier = Modifier.size(12.dp))
                                                }
                                                Text("Home", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoDarkText)
                                            }

                                            Box(modifier = Modifier.width(1.dp).height(16.dp).background(BentoGreyBorder))

                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        viewModel.onPickupChange("Nellai Nagar Area, Sogathur Path, Dharmapuri")
                                                        viewModel.onDropoffChange("Govt Medical College Hospital, Dharmapuri")
                                                        Toast.makeText(context, "Prefilled Work destination!", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(BentoLightGreen),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Build, contentDescription = "Work", tint = BentoGreen, modifier = Modifier.size(12.dp))
                                                }
                                                Text("Work", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoDarkText)
                                            }

                                            Box(modifier = Modifier.width(1.dp).height(16.dp).background(BentoGreyBorder))

                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        viewModel.selectedTabState = 1 // Open Saved Commutes to configure more
                                                        Toast.makeText(context, "Configure Saved Commutes!", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(BentoLightGreen),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = BentoGreen, modifier = Modifier.size(12.dp))
                                                }
                                                Text("More", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoDarkText)
                                            }
                                        }
                                    }
                                }

                                if (viewModel.pickupState.isBlank() || viewModel.dropoffState.isBlank()) {
                                    // 1. Promo Event Banner resembling Bento Green promo
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                Toast.makeText(context, "Promotion: Flat ₹50 off on your first Electric Auto ride today! 🔋", Toast.LENGTH_LONG).show()
                                            },
                                        colors = CardDefaults.cardColors(containerColor = BentoGreen),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "WEEKEND OFFER",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoLightGreen,
                                                    letterSpacing = 1.sp
                                                )
                                                Text(
                                                    text = "Zero cancellation fee today",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 15.sp
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = "Offer details",
                                                tint = Color.White
                                            )
                                        }
                                    }

                                    // 2. Bento Grid Layout
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Left yellow booking card (Book Auto Now)
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                            colors = CardDefaults.cardColors(containerColor = BentoYellow),
                                            shape = RoundedCornerShape(28.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(Color.White.copy(alpha = 0.4f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("🛺", fontSize = 18.sp)
                                                    }
                                                    Text(
                                                        text = "Book\nAuto Now",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 19.sp,
                                                        color = BentoDarkText,
                                                        lineHeight = 22.sp
                                                    )
                                                }
                                                // Start ride action
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(24.dp))
                                                        .background(BentoDarkText)
                                                        .clickable {
                                                            viewModel.onPickupChange("Nellai Nagar, Dharmapuri")
                                                            viewModel.onDropoffChange("Dharmapuri Bus Stand")
                                                            Toast.makeText(context, "Quick Setup Active! Select option below.", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = "START RIDE",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        // Right column of Bento Grid (Namma Wallet + Safety)
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Namma Wallet card
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f),
                                                colors = CardDefaults.cardColors(containerColor = BentoLightGreen),
                                                shape = RoundedCornerShape(24.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(12.dp),
                                                    verticalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.Top
                                                    ) {
                                                        Text(
                                                            text = "NAMMA WALLET",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = BentoGreen,
                                                            letterSpacing = 0.5.sp
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.ShoppingCart,
                                                            contentDescription = "Wallet Icon",
                                                            tint = BentoGreen,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Column {
                                                        Text(text = "₹420", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                                                        Text(text = "+ ₹50 cashback pending", fontSize = 8.sp, color = BentoGreen, fontWeight = FontWeight.Medium)
                                                    }
                                                }
                                            }

                                            // Safety Control Card
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                                    .clickable {
                                                        Toast.makeText(context, "Emergency Safe Mode activated. Customer safety is monitored 24/7! 🛡️", Toast.LENGTH_LONG).show()
                                                    },
                                                colors = CardDefaults.cardColors(containerColor = BentoSoftRed),
                                                shape = RoundedCornerShape(24.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White.copy(alpha = 0.6f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Lock, contentDescription = "Safety", tint = BentoDarkRed, modifier = Modifier.size(14.dp))
                                                    }
                                                    Text(
                                                        text = "24/7 Safety Support",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BentoDarkRed,
                                                        lineHeight = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (viewModel.pickupState.isNotBlank() && viewModel.dropoffState.isNotBlank()) {
                                    // Selection of Auto Types
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Select Auto Option",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = BentoDarkText
                                        )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val autoTypes = listOf(
                                            Triple("Standard Yellow Auto", "₹15/km • Fixed base", "🛺"),
                                            Triple("Electric Namma Auto ⚡", "₹13/km • Eco-friendly", "🔋"),
                                            Triple("Share Auto 🛺", "₹7/km • Commute sharing", "👥")
                                        )

                                        autoTypes.forEach { (type, desc, emoji) ->
                                            val isSelected = viewModel.selectedAutoTypeState == type
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(
                                                        width = if (isSelected) 2.dp else 1.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { viewModel.selectAutoType(type) }
                                                    .testTag("auto_type_$type"),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(10.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(text = emoji, fontSize = 24.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = type.substringBefore(" "),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = desc,
                                                        fontSize = 9.sp,
                                                        color = Color.Gray,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 2,
                                                        lineHeight = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                }

                                // Booking Summary & Smart Fare Calculator Breakdown Call-to-action
                                if (viewModel.pickupState.isNotBlank() && viewModel.dropoffState.isNotBlank()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(24.dp))
                                            .border(1.dp, BentoGreyBorder, RoundedCornerShape(24.dp))
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        // Header
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "🧮",
                                                    fontSize = 18.sp
                                                )
                                                Text(
                                                    text = "Smart Meter Fare Calculator",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = BentoDarkText
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(BentoLightGreen, RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "Dharmapuri Rates",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoGreen
                                                )
                                            }
                                        }

                                        // Section 1: Simulated Distance Controller (Tactile Testing)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF7F8F6), RoundedCornerShape(12.dp))
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "METERED DISTANCE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Gray,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Text(
                                                    text = "${viewModel.distanceState} km",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = BentoDarkText
                                                )
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Decrement Distance button
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White)
                                                        .border(1.dp, BentoGreyBorder, CircleShape)
                                                        .clickable { viewModel.adjustDistance(-1.0) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("–", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BentoDarkText)
                                                }
                                                // Increment Distance button
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White)
                                                        .border(1.dp, BentoGreyBorder, CircleShape)
                                                        .clickable { viewModel.adjustDistance(1.0) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BentoDarkText)
                                                }
                                            }
                                        }

                                        // Section 2: Interactive meter parameters (Surcharges toggles)
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "ADJUST FARE FACTOR OPTION",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray,
                                                letterSpacing = 0.5.sp
                                            )

                                            // Night commute rate (1.5x)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (viewModel.isNightCharge) BentoGreen else BentoGreyBorder,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { viewModel.toggleNightCharge() }
                                                    .background(if (viewModel.isNightCharge) BentoLightGreen.copy(alpha = 0.2f) else Color.Transparent)
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text("🌙", fontSize = 16.sp)
                                                    Column {
                                                        Text("Night Ride (10 PM - 5 AM)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                                                        Text("Adds standard 1.5x local surcharge", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                                Icon(
                                                    imageVector = if (viewModel.isNightCharge) Icons.Default.Check else Icons.Default.Add,
                                                    contentDescription = "Night toggle icon",
                                                    tint = if (viewModel.isNightCharge) BentoGreen else Color.Gray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            // Luggage helper rate (+10)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (viewModel.luggageChargeEnabled) BentoGreen else BentoGreyBorder,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { viewModel.toggleLuggage() }
                                                    .background(if (viewModel.luggageChargeEnabled) BentoLightGreen.copy(alpha = 0.2f) else Color.Transparent)
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text("💼", fontSize = 16.sp)
                                                    Column {
                                                        Text("Luggage & Assistance (+₹10)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                                                        Text("Heavy bags or additional carrier support", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                                Icon(
                                                    imageVector = if (viewModel.luggageChargeEnabled) Icons.Default.Check else Icons.Default.Add,
                                                    contentDescription = "Luggage toggle icon",
                                                    tint = if (viewModel.luggageChargeEnabled) BentoGreen else Color.Gray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            // Traffic Segment Selector Slider/Buttons
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF7F8F6), RoundedCornerShape(12.dp))
                                                    .padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "🚦 Traffic Overhead",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoDarkText,
                                                    modifier = Modifier.padding(start = 6.dp)
                                                )

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    listOf("Low", "Medium", "Heavy").forEach { level ->
                                                        val isSelected = viewModel.trafficLevel == level
                                                        val chipBg = if (isSelected) {
                                                            when (level) {
                                                                "Heavy" -> BentoSoftRed
                                                                "Medium" -> BentoYellow.copy(alpha = 0.4f)
                                                                else -> BentoLightGreen
                                                            }
                                                        } else Color.White

                                                        val chipText = if (isSelected) {
                                                            when (level) {
                                                                "Heavy" -> BentoDarkRed
                                                                "Medium" -> BentoDarkText
                                                                else -> BentoGreen
                                                            }
                                                        } else Color.Gray

                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(chipBg)
                                                                .border(1.dp, if (isSelected) Color.Transparent else BentoGreyBorder, RoundedCornerShape(8.dp))
                                                                .clickable { viewModel.setTraffic(level) }
                                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(
                                                                text = level,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = chipText
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Section 3: Detailed Breakdown Card (Official Meter Styling List)
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFFBFCF8), RoundedCornerShape(14.dp))
                                                .border(1.dp, BentoGreyBorder, RoundedCornerShape(14.dp))
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "OFFICIAL FARE COMPUTATION DETAILED BREAKDOWN",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray,
                                                letterSpacing = 0.5.sp
                                            )

                                            // Row 1: Base Fare Component
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Base Fare (First 1.5 km min rate)", fontSize = 11.sp, color = BentoDarkText)
                                                Text("₹${viewModel.baseFareComponent}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                                            }

                                            // Row 2: Distance Charge Component
                                            if (viewModel.distanceFareComponent > 0.0) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Subsequent distance calculated charge", fontSize = 11.sp, color = BentoDarkText)
                                                    Text("₹${viewModel.distanceFareComponent}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                                                }
                                            }

                                            // Row 3: Traffic Overhead Charge
                                            if (viewModel.trafficSurchargeComponent > 0.0) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("🚦 Traffic delay/waiting overhead", fontSize = 11.sp, color = BentoDarkText)
                                                    Text("+ ₹${viewModel.trafficSurchargeComponent}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                                                }
                                            }

                                            // Row 4: Luggage Surcharge
                                            if (viewModel.luggageSurchargeComponent > 0.0) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("💼 Luggage assistance", fontSize = 11.sp, color = BentoDarkText)
                                                    Text("+ ₹${viewModel.luggageSurchargeComponent}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                                                }
                                            }

                                            // Row 5: Night Ride Surcharge
                                            if (viewModel.nightSurchargeComponent > 0.0) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("🌙 Night surcharge factor (50% multiplier)", fontSize = 11.sp, color = BentoDarkText)
                                                    Text("+ ₹${viewModel.nightSurchargeComponent}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                                                }
                                            }

                                            HorizontalDivider(color = BentoGreyBorder.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                                            // Row 6: Auto Type Info
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Auto Fleet Mode: ${viewModel.selectedAutoTypeState}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = BentoGreen
                                                )
                                                Text(
                                                    text = "Meter Base Charged",
                                                    fontSize = 9.sp,
                                                    color = Color.Gray
                                                )
                                            }

                                            HorizontalDivider(color = BentoGreyBorder.copy(alpha = 0.3f), thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))

                                            // Payment selection UI
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "CHOOSE PAYMENT MODE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Gray,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    val paymentOptionsList = listOf(
                                                        Triple("Cash", "💵 Cash", "payment_cash"),
                                                        Triple("UPI", "📱 UPI", "payment_upi"),
                                                        Triple("Digital Wallet", "👛 Wallet", "payment_wallet")
                                                    )
                                                    paymentOptionsList.forEach { (value, label, tag) ->
                                                        val isSelected = viewModel.selectedPaymentMethodState == value
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(48.dp)
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(
                                                                    if (isSelected) BentoYellow.copy(alpha = 0.15f) else Color(0xFFFBFBFB)
                                                                )
                                                                .border(
                                                                    width = if (isSelected) 2.dp else 1.dp,
                                                                    color = if (isSelected) BentoYellow else BentoGreyBorder,
                                                                    shape = RoundedCornerShape(12.dp)
                                                                )
                                                                .clickable {
                                                                    viewModel.selectedPaymentMethodState = value
                                                                }
                                                                .testTag(tag),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = label,
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isSelected) BentoDarkText else Color.Gray
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Total Fare & Book CTR Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "FINAL METER FARE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Gray,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Text(
                                                    text = "₹${viewModel.estimatedPriceState}",
                                                    fontSize = 26.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = BentoGreen
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    keyboardController?.hide()
                                                    viewModel.bookRide()
                                                    Toast.makeText(context, "Searching Namma Auto rickshaws... 🛺", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = BentoYellow,
                                                    contentColor = BentoDarkText
                                                ),
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier
                                                    .height(52.dp)
                                                    .testTag("book_button")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text("Book Auto 🛺", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                                    Icon(Icons.Default.Check, contentDescription = "Book Confirmation", tint = BentoDarkText, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Active Commute Display Card (Matches live simulation status)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("active_ride_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Status Header & OTP Code
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (activeRide!!.status == "SEARCHING") Color(0xFFFFB300)
                                                            else if (activeRide!!.status == "STARTED") MaterialTheme.colorScheme.secondary
                                                            else Color(0xFF0F9D58)
                                                        )
                                                )
                                                Text(
                                                    text = activeRide!!.status,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Text(
                                                text = "Fare: ₹${activeRide!!.fare} • ${activeRide!!.autoType.substringBefore(" ")}",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        if (activeRide!!.status != "SEARCHING") {
                                         /*
                                     }
                                     */
                                         /*
                                     }
                                     */
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("OTP code", fontSize = 9.sp, color = Color.Gray)
                                                    Text(
                                                        text = activeRide!!.otp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        letterSpacing = 1.sp,
                                                        modifier = Modifier.testTag("otp_code")
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Pickup & Dropoff Detail lines
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Place, contentDescription = "From", tint = Color(0xFF0288D1), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = activeRide!!.pickup,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, contentDescription = "To", tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = activeRide!!.dropoff,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                                    // Driver Profile block (Matches accepted statuses) NEW_MAIN_DRIVER_CARD
                                    if (activeRide!!.status != "SEARCHING") {
                                        ActiveDriverDetailsCard(
                                            activeRide = activeRide!!,
                                            context = context,
                                            viewModel = viewModel
                                        )
                                    }
                                    if (false) {
                                    if (activeRide!!.status != "SEARCHING") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                // Driver avatar emoji
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("👨🏽‍✈️", fontSize = 24.sp)
                                                }

                                                Column {
                                                    Text(
                                                        text = activeRide!!.driverName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        modifier = Modifier.testTag("driver_name")
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                                        Text("4.9 Rating", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = activeRide!!.driverVehicleNo,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.testTag("vehicle_no")
                                                )
                                                Text(
                                                    text = activeRide!!.driverPhone,
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }

                                }

                                    // Active trip status tracker or helper triggers
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (activeRide!!.status == "ARRIVED") {
                                            Button(
                                                onClick = { viewModel.forceStartTrip() },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.secondary
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("start_trip_button")
                                            ) {
                                                Text("Start Trip with OTP", fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.cancelActiveRide() },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFFD32F2F)
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.3f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("cancel_booking_button")
                                        ) {
                                            Text("Cancel booking")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // SOS Emergency triggering banner/button
                                    if (viewModel.sosActiveState) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFFFECEB), RoundedCornerShape(12.dp))
                                                .border(1.5.dp, Color(0xFFD32F2F), RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                                .testTag("sos_active_banner"),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🚨", fontSize = 20.sp)
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "EMERGENCY SOS ACTIVE",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFFD32F2F)
                                                )
                                                Text(
                                                    text = "Simulated GPS location is being continuously uploaded.",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF410002).copy(alpha = 0.85f)
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.triggerSosAlert() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFD32F2F)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("sos_button")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("🚨", fontSize = 16.sp)
                                            Text(
                                                text = if (viewModel.sosActiveState) "VIEW ACTIVE SOS SCREEN" else "TRIGGER EMERGENCY SOS",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // My Commutes Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "My Saved Commutes 🛣️",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = { isAddingCommute = !isAddingCommute },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAddingCommute) Color.Gray else MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isAddingCommute) "Cancel" else "Add Custom")
                            }
                        }

                        // Add custom Accordion Form
                        AnimatedVisibility(visible = isAddingCommute) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Add Daily Shortcut Route",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    OutlinedTextField(
                                        value = customTitle,
                                        onValueChange = { customTitle = it },
                                        label = { Text("Commute Name / Nickname") },
                                        placeholder = { Text("e.g. Home to Metro Station 🚇") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = customPickup,
                                        onValueChange = { customPickup = it },
                                        label = { Text("Pickup Address") },
                                        placeholder = { Text("e.g. Golden Towers Main Road") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = customDropoff,
                                        onValueChange = { customDropoff = it },
                                        label = { Text("Dropoff Address") },
                                        placeholder = { Text("e.g. Dharmapuri Bus Stand Entrance") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            if (customTitle.isNotBlank() && customPickup.isNotBlank() && customDropoff.isNotBlank()) {
                                                viewModel.addCustomCommute(customTitle, customPickup, customDropoff)
                                                // Clear form
                                                customTitle = ""
                                                customPickup = ""
                                                customDropoff = ""
                                                isAddingCommute = false
                                                Toast.makeText(context, "Commute route saved!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Save Route Shortcut")
                                    }
                                }
                            }
                        }

                        // Commute short cuts List
                        if (commutes.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🛺", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No Commute Routes Saved", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Predefined routes will spawn automatically.", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(commutes) { commute ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = commute.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )

                                                IconButton(
                                                    onClick = { 
                                                        viewModel.deleteCommute(commute)
                                                        Toast.makeText(context, "Route deleted", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Delete Route",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Place, contentDescription = "From", tint = Color(0xFF0288D1), modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = commute.pickup, fontSize = 12.sp, color = Color.Gray)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.LocationOn, contentDescription = "To", tint = Color(0xFFD32F2F), modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = commute.dropoff, fontSize = 12.sp, color = Color.Gray)
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${commute.distance} km • Est. ${commute.estimatedMinutes} mins",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )

                                                Button(
                                                    onClick = {
                                                        viewModel.selectCommuteShortcut(commute)
                                                        viewModel.selectedTabState = 0
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                        contentColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text("Use Shortcut", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // History Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Commutes Log 📋",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (ridesHistory.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { 
                                        viewModel.clearHistory()
                                        Toast.makeText(context, "Log cleared", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Gray
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Clear All", fontSize = 11.sp)
                                }
                            }
                        }

                        if (ridesHistory.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📝", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No book history found", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Book autos from the search screen to log commutes.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(ridesHistory) { ride ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = ride.autoType,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = if (ride.tipAmount > 0.0) "Paid: ₹${(ride.fare + ride.tipAmount).toInt()} (incl. ₹${ride.tipAmount.toInt()} tip)"
                                                                   else "Paid: ₹${ride.fare.toInt()}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            text = "•",
                                                            fontSize = 11.sp,
                                                            color = Color.Gray
                                                        )
                                                        val pmLabel = when (ride.paymentMethod) {
                                                            "UPI" -> "📱 UPI"
                                                            "Digital Wallet" -> "👛 Wallet"
                                                            else -> "💵 Cash"
                                                        }
                                                        Text(
                                                            text = pmLabel,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = BentoGreen
                                                        )
                                                    }
                                                }

                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (ride.status == "COMPLETED") MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                                        else if (ride.status == "CANCELLED") Color.Red.copy(alpha = 0.1f)
                                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                    ),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = ride.status,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (ride.status == "COMPLETED") MaterialTheme.colorScheme.secondary
                                                        else if (ride.status == "CANCELLED") Color.Red
                                                        else MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Place, contentDescription = "From", tint = Color(0xFF0288D1), modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = ride.pickup, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.LocationOn, contentDescription = "To", tint = Color(0xFFD32F2F), modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = ride.dropoff, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }

                                            if (ride.driverName.isNotBlank() && ride.status == "COMPLETED") {
                                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text("👨🏽‍✈️", fontSize = 14.sp)
                                                        Text(text = ride.driverName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                    }

                                                    if (ride.rating != null) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                            repeat(ride.rating) {
                                                                Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                                            }
                                                            if (ride.feedback != null && ride.feedback.isNotBlank()) {
                                                                Text(
                                                                    text = " • \"${ride.feedback}\"",
                                                                    fontSize = 11.sp,
                                                                    color = Color.Gray
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        // Fallback prompt rating trigger
                                                        Button(
                                                            onClick = { viewModel.showRatingDialogForRideId = ride.id },
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color.LightGray.copy(alpha = 0.2f),
                                                                contentColor = MaterialTheme.colorScheme.primary
                                                            ),
                                                            modifier = Modifier.height(24.dp)
                                                        ) {
                                                            Text("Rate ride", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Star Rating & Feedback Dialog Overlay (Active after completing ride)
            if (viewModel.showRatingDialogForRideId != null) {
                var selectedStars by remember { mutableStateOf(5) }
                var ratingFeedback by remember { mutableStateOf("") }
                var selectedTip by remember { mutableStateOf(0.0) }
                val targetRide = ridesHistory.find { it.id == viewModel.showRatingDialogForRideId }
                val originalFare = targetRide?.fare ?: 0.0

                val autoFeedbacks = listOf(
                    "Semma Ride! 🛺",
                    "Polite Driver, Ontime!",
                    "Clean Vehicle 🧼",
                    "Helped bypass Dharmapuri traffic 🚦"
                )

                Dialog(onDismissRequest = { viewModel.showRatingDialogForRideId = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("rating_dialog"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "How was your commute? 🛺",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Rate driver to improve Namma Auto services",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )

                            // Animated Star Selection Grid
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star $i",
                                        tint = if (i <= selectedStars) Color(0xFFFFB300) else Color.LightGray,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable { selectedStars = i }
                                            .testTag("star_rating_$i")
                                    )
                                }
                            }

                            // Preset Feedback chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        autoFeedbacks.take(2).forEach { chipText ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                    .clickable { ratingFeedback = chipText }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text(chipText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        autoFeedbacks.drop(2).forEach { chipText ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                    .clickable { ratingFeedback = chipText }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text(chipText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }

                            // Feedback Text fields
                            OutlinedTextField(
                                value = ratingFeedback,
                                onValueChange = { ratingFeedback = it },
                                label = { Text("Comment (optional)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("feedback_comment_input"),
                                singleLine = true
                            )

                            HorizontalDivider(color = BentoGreyBorder.copy(alpha = 0.5f), thickness = 1.dp)

                            // Select an optional Tip amount
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Add a Tip for Driver 💚",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoDarkText
                                    )
                                    Text(
                                        text = "100% goes to driver",
                                        fontSize = 10.sp,
                                        color = BentoGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val tips = listOf(0.0, 10.0, 20.0, 50.0)
                                    tips.forEach { amount ->
                                        val isTipSelected = selectedTip == amount
                                        val label = if (amount == 0.0) "No Tip" else "₹${amount.toInt()}"
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isTipSelected) BentoGreen.copy(alpha = 0.12f) else Color(0xFFF7F8F7)
                                                )
                                                .border(
                                                    width = if (isTipSelected) 1.5.dp else 1.dp,
                                                    color = if (isTipSelected) BentoGreen else BentoGreyBorder,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    selectedTip = amount
                                                }
                                                .testTag("tip_chip_${amount.toInt()}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isTipSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isTipSelected) BentoGreen else BentoDarkText
                                            )
                                        }
                                    }
                                }
                            }

                            // Dynamically calculated Total Summary
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BentoLightGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, BentoGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TOTAL COMMUTE CHARGE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "₹${(originalFare + selectedTip).toInt()}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoDarkText,
                                        modifier = Modifier.testTag("total_paid_summary")
                                    )
                                }
                                if (selectedTip > 0.0) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Fare: ₹${originalFare.toInt()}", fontSize = 10.sp, color = Color.Gray)
                                        Text("Tip: +₹${selectedTip.toInt()}", fontSize = 10.sp, color = BentoGreen, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text(
                                        text = "No Tip selected",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Button(
                                onClick = {
                                    viewModel.submitFeedback(selectedStars, ratingFeedback, selectedTip)
                                    Toast.makeText(context, "Dhanyavada (Thank you)! 🙏", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_rating_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Submit rating & tip", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // SOS Emergency Status & Simulated Alert Dialog Overlay
            if (viewModel.showSosAlertDialog && activeRide != null) {
                Dialog(onDismissRequest = { viewModel.dismissSosAlert() }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(2.dp, Color(0xFFD32F2F), RoundedCornerShape(24.dp))
                            .testTag("sos_alert_dialog"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF8F8))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFDADA)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🚨",
                                    fontSize = 32.sp
                                )
                            }

                            Text(
                                text = "EMERGENCY ALERT TRIGGERED",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD32F2F),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Simulating emergency dispatch. Real-time GPS telemetry has been shared with Namma Auto Emergency response team & family contacts.",
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            HorizontalDivider(color = Color(0xFFE5D5D5), thickness = 1.dp)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFE5D5D5), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "TRANSMITTED LIVE TRIP STATUS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 0.5.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Ride Ref:", fontSize = 11.sp, color = Color.Gray)
                                    Text("#${activeRide!!.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDarkText, modifier = Modifier.testTag("sos_ref_value"))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Match Status:", fontSize = 11.sp, color = Color.Gray)
                                    Text(activeRide!!.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Auto Details:", fontSize = 11.sp, color = Color.Gray)
                                    Text("${activeRide!!.driverVehicleNo} (${activeRide!!.autoType})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Driver Person:", fontSize = 11.sp, color = Color.Gray)
                                    Text(activeRide!!.driverName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                                }

                                val (eta, dist) = when {
                                    activeRide!!.status == "STARTED" -> viewModel.getDropoffEtaAndDistance()
                                    activeRide!!.status == "ARRIVED" -> Pair("0 mins", "0 m")
                                    else -> viewModel.getPickupEtaAndDistance()
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Current ETA / Distance:", fontSize = 11.sp, color = Color.Gray)
                                    Text("$eta ($dist left)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoGreen, modifier = Modifier.testTag("sos_eta_dist_value"))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Live Coordinates:", fontSize = 11.sp, color = Color.Gray)
                                    Text("Lat/Lng: (${viewModel.mapDriverX.toInt()}, ${viewModel.mapDriverY.toInt()})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1), modifier = Modifier.testTag("sos_coordinates_value"))
                                }
                            }

                            HorizontalDivider(color = Color(0xFFE5D5D5), thickness = 1.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        android.widget.Toast.makeText(context, "Contacting Karnataka Police Control Room (112)... 📞", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFECEB), contentColor = Color(0xFFD32F2F)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Call police", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        android.widget.Toast.makeText(context, "Shared live status SMS to contacts!", android.widget.Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1F5FE), contentColor = Color(0xFF0288D1)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("SMS contacts", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.dismissSosAlert() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                                ) {
                                    Text("Close window", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.cancelSosEmergency()
                                        android.widget.Toast.makeText(context, "Emergency state deactivated", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("cancel_sos_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                ) {
                                    Text("Deactivate SOS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Infix align and spacing helpers
private infix fun RowScope.valign(alignment: Alignment.Vertical): RowScope {
    return this
}

// Dharmapuri Landmark Model
data class DharmapuriLandmark(
    val id: String,
    val name: String,
    val icon: String,
    val gridX: Float, // 0 to 1000 standard scale
    val gridY: Float, // 0 to 1000 standard scale
    val fullName: String,
    val description: String
)

val dharmapuriLandmarks = listOf(
    DharmapuriLandmark(
        id = "bus_stand",
        name = "Central Bus Stand",
        icon = "🚌",
        gridX = 500f,
        gridY = 480f,
        fullName = "Dharmapuri Central Bus Stand, Dharmapuri",
        description = "Principal regional transit transit hub"
    ),
    DharmapuriLandmark(
        id = "medical_college",
        name = "Govt Medical College",
        icon = "🏥",
        gridX = 220f,
        gridY = 250f,
        fullName = "Govt Medical College Hospital, Dharmapuri",
        description = "Large primary healthcare district hospital"
    ),
    DharmapuriLandmark(
        id = "railway_station",
        name = "Railway Station",
        icon = "🚄",
        gridX = 180f,
        gridY = 700f,
        fullName = "Dharmapuri Railway Station (DPJ), Dharmapuri",
        description = "South Western Railway terminal"
    ),
    DharmapuriLandmark(
        id = "collectorate",
        name = "Collectorate Office",
        icon = "🏢",
        gridX = 780f,
        gridY = 180f,
        fullName = "Dharmapuri District Collector Office, Dharmapuri",
        description = "Administrative center of Dharmapuri district"
    ),
    DharmapuriLandmark(
        id = "temple",
        name = "Adhiyaman Kottai",
        icon = "🛕",
        gridX = 820f,
        gridY = 800f,
        fullName = "Adhiyaman Kottai Dakshina Kashi Kalabhairavar Temple, Dharmapuri",
        description = "Historic temple built by King Adhiyaman"
    ),
    DharmapuriLandmark(
        id = "petrol_bunk",
        name = "Sogathur Petrol Bunk",
        icon = "⛽",
        gridX = 480f,
        gridY = 780f,
        fullName = "Sogathur Petrol Bunk Road, Dharmapuri",
        description = "Active fuel station along bypass"
    ),
    DharmapuriLandmark(
        id = "nellai_nagar",
        name = "Nellai Nagar",
        icon = "🏡",
        gridX = 150f,
        gridY = 850f,
        fullName = "Nellai Nagar Area, Sogathur Path, Dharmapuri",
        description = "Residential neighborhood near town center"
    )
)

// Simulated beautiful interactive vector map layout
@Composable
fun NammaAutoMap(
    activeRide: BookedRide?,
    pickupX: Float,
    pickupY: Float,
    dropoffX: Float,
    dropoffY: Float,
    driverX: Float,
    driverY: Float,
    hasPickup: Boolean,
    hasDropoff: Boolean,
    nearbyAutos: List<Pair<Float, Float>>,
    viewModel: AutoBookingViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedLandmark by remember { mutableStateOf<DharmapuriLandmark?>(null) }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val widthDp = maxWidth
        val heightDp = maxHeight

        fun mapX(x: Float): Float = (x / 1000f) * width
        fun mapY(y: Float): Float = (y / 1000f) * height

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9)) // Light green lawn tone
        ) {
            // 1. Draw D3 topological grid lines (connections between landmarks)
            val networkColor = Color(0xFF4CAF50).copy(alpha = 0.25f)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)

            val links = listOf(
                Pair("bus_stand", "medical_college"),
                Pair("bus_stand", "railway_station"),
                Pair("bus_stand", "collectorate"),
                Pair("bus_stand", "petrol_bunk"),
                Pair("petrol_bunk", "nellai_nagar"),
                Pair("petrol_bunk", "temple"),
                Pair("railway_station", "nellai_nagar"),
                Pair("medical_college", "collectorate")
            )

            links.forEach { link ->
                val fromNode = dharmapuriLandmarks.find { it.id == link.first }
                val toNode = dharmapuriLandmarks.find { it.id == link.second }
                if (fromNode != null && toNode != null) {
                    drawLine(
                        color = networkColor,
                        start = Offset(mapX(fromNode.gridX), mapY(fromNode.gridY)),
                        end = Offset(mapX(toNode.gridX), mapY(toNode.gridY)),
                        strokeWidth = 3f,
                        pathEffect = dashEffect
                    )
                }
            }

            // Draw Vathalmalai scenic hills (top-right background contour)
            val hillsPath = Path().apply {
                moveTo(width * 0.65f, 0f)
                quadraticTo(width * 0.8f, height * 0.22f, width, height * 0.05f)
                lineTo(width, 0f)
                close()
            }
            drawPath(
                path = hillsPath,
                color = Color(0xFF1B5E20).copy(alpha = 0.12f)
            )

            // Draw Chinnar River (top-left curvy waterbody)
            val riverPath = Path().apply {
                moveTo(0f, height * 0.25f)
                cubicTo(width * 0.15f, height * 0.2f, width * 0.1f, height * 0.65f, 0f, height * 0.85f)
            }
            drawPath(
                path = riverPath,
                color = Color(0xFF4FC3F7).copy(alpha = 0.35f),
                style = Stroke(width = 14f, join = StrokeJoin.Round)
            )

            // Draw primary streets (national highway NH-44 & Pennagaram Road)
            val roadColor = Color(0xFFFFFFFF)
            val roadBorderColor = Color(0xFFE0E0E0)
            val roadWidth = 22.dp.toPx()

            // NH-44 horizontal highway
            drawRect(
                color = roadColor,
                topLeft = Offset(0f, mapY(480f) - roadWidth / 2),
                size = Size(width, roadWidth)
            )
            drawLine(color = roadBorderColor, start = Offset(0f, mapY(480f) - roadWidth / 2), end = Offset(width, mapY(480f) - roadWidth / 2), strokeWidth = 1.5f)
            drawLine(color = roadBorderColor, start = Offset(0f, mapY(480f) + roadWidth / 2), end = Offset(width, mapY(480f) + roadWidth / 2), strokeWidth = 1.5f)

            // Pennagaram Road vertical main st
            drawRect(
                color = roadColor,
                topLeft = Offset(mapX(500f) - roadWidth / 2, 0f),
                size = Size(roadWidth, height)
            )
            drawLine(color = roadBorderColor, start = Offset(mapX(500f) - roadWidth / 2, 0f), end = Offset(mapX(500f) - roadWidth / 2, height), strokeWidth = 1.5f)
            drawLine(color = roadBorderColor, start = Offset(mapX(500f) + roadWidth / 2, 0f), end = Offset(mapX(500f) + roadWidth / 2, height), strokeWidth = 1.5f)

            // Highway middle dashed dividers (Yellow/White)
            val highwayDivider = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
            drawLine(color = Color(0xFFFFCC00), start = Offset(0f, mapY(480f)), end = Offset(width, mapY(480f)), strokeWidth = 2f, pathEffect = highwayDivider)
            drawLine(color = Color(0xFFFFCC00), start = Offset(mapX(500f), 0f), end = Offset(mapX(500f), height), strokeWidth = 2f, pathEffect = highwayDivider)

            // 2. Draw Route Link Path if Trip starts and locations selected
            if (activeRide != null && hasPickup && hasDropoff) {
                val routeBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0288D1), Color(0xFFFFC72C), Color(0xFFD32F2F))
                )
                drawLine(
                    brush = routeBrush,
                    start = Offset(mapX(pickupX), mapY(pickupY)),
                    end = Offset(mapX(dropoffX), mapY(dropoffY)),
                    strokeWidth = 8f
                )
            }

            // 3. Draw idle autos as tiny animated pulsing yellow dots with dark core
            if (activeRide == null) {
                nearbyAutos.forEach { auto ->
                    drawCircle(color = Color(0xFFFFC72C), center = Offset(mapX(auto.first), mapY(auto.second)), radius = 5.dp.toPx())
                    drawCircle(color = Color.Black, center = Offset(mapX(auto.first), mapY(auto.second)), radius = 2.dp.toPx())
                }
            }

            // 4. Draw selected pickup pin shape
            if (hasPickup) {
                val pX = mapX(pickupX)
                val pY = mapY(pickupY)
                drawCircle(
                    color = Color(0xFF0288D1).copy(alpha = pulseAlpha),
                    center = Offset(pX, pY),
                    radius = pulseScale.dp.toPx()
                )
                drawCircle(color = Color(0xFF0288D1), center = Offset(pX, pY), radius = 7.dp.toPx())
                drawCircle(color = Color.White, center = Offset(pX, pY), radius = 3.dp.toPx())
            }

            // 5. Draw selected dropoff pin shape
            if (hasDropoff) {
                val dX = mapX(dropoffX)
                val dY = mapY(dropoffY)
                drawCircle(
                    color = Color(0xFFD32F2F).copy(alpha = pulseAlpha),
                    center = Offset(dX, dY),
                    radius = pulseScale.dp.toPx()
                )
                drawCircle(color = Color(0xFFD32F2F), center = Offset(dX, dY), radius = 7.dp.toPx())
                drawCircle(color = Color.White, center = Offset(dX, dY), radius = 3.dp.toPx())
            }

            // 6. Draw booking driver cabin
            if (activeRide != null && (activeRide.status == "ACCEPTED" || activeRide.status == "ARRIVED" || activeRide.status == "STARTED")) {
                val drX = mapX(driverX)
                val drY = mapY(driverY)
                val autoAccent = if (activeRide.autoType.contains("Electric")) Color(0xFF0F9D58) else Color.DarkGray

                // wheels
                drawCircle(color = Color.Black, center = Offset(drX - 6.dp.toPx(), drY + 6.dp.toPx()), radius = 3.dp.toPx())
                drawCircle(color = Color.Black, center = Offset(drX + 6.dp.toPx(), drY + 6.dp.toPx()), radius = 3.dp.toPx())

                // cabin
                drawCircle(color = Color(0xFFFFC72C), center = Offset(drX, drY), radius = 8.dp.toPx())

                // chassis
                drawRect(
                    color = autoAccent,
                    topLeft = Offset(drX - 5.dp.toPx(), drY + 1.dp.toPx()),
                    size = Size(10.dp.toPx(), 3.dp.toPx())
                )
                // canopy
                drawArc(
                    color = Color.Black,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(drX - 8.dp.toPx(), drY - 8.dp.toPx()),
                    size = Size(16.dp.toPx(), 14.dp.toPx())
                )
            }
        }

        // 7. Interactive Overlay Nodes for Landmarks
        dharmapuriLandmarks.forEach { landmark ->
            val lX = widthDp * (landmark.gridX / 1000f)
            val lY = heightDp * (landmark.gridY / 1000f)

            val isCurrentPickup = viewModel.pickupState.contains(landmark.name, ignoreCase = true)
            val isCurrentDropoff = viewModel.dropoffState.contains(landmark.name, ignoreCase = true)
            val isSelectedOnMap = selectedLandmark?.id == landmark.id

            Box(
                modifier = Modifier
                    .offset(x = lX - 16.dp, y = lY - 16.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        color = when {
                            isCurrentPickup -> Color(0xFFE0F2F1)
                            isCurrentDropoff -> Color(0xFFFFEBEE)
                            isSelectedOnMap -> Color(0xFFFFF3E0)
                            else -> Color.White.copy(alpha = 0.95f)
                        }
                    )
                    .border(
                        width = when {
                            isCurrentPickup -> 2.dp
                            isCurrentDropoff -> 2.dp
                            isSelectedOnMap -> 2.dp
                            else -> 1.dp
                        },
                        color = when {
                            isCurrentPickup -> Color(0xFF009688)
                            isCurrentDropoff -> Color(0xFFE53935)
                            isSelectedOnMap -> Color(0xFFFB8C00)
                            else -> Color.LightGray.copy(alpha = 0.8f)
                        },
                        shape = CircleShape
                    )
                    .clickable {
                        selectedLandmark = landmark
                    }
                    .testTag("map_landmark_${landmark.id}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = landmark.icon,
                    fontSize = 15.sp
                )
            }
        }

        // 8. Bottom Hud Overlay Card when a Landmark node is Selected
        selectedLandmark?.let { landmark ->
            Card(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.95f)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .testTag("landmark_hud_panel"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(landmark.icon, fontSize = 20.sp)
                            Column {
                                Text(
                                    text = landmark.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoDarkText
                                )
                                Text(
                                    text = landmark.description,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = { selectedLandmark = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close description",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.onPickupChange(landmark.fullName)
                                viewModel.mapPickupX = landmark.gridX
                                viewModel.mapPickupY = landmark.gridY
                                Toast.makeText(context, "🟢 Set Pickup to ${landmark.name}", Toast.LENGTH_SHORT).show()
                                selectedLandmark = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Set Pickup 🟢", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.onDropoffChange(landmark.fullName)
                                viewModel.mapDropoffX = landmark.gridX
                                viewModel.mapDropoffY = landmark.gridY
                                Toast.makeText(context, "🔴 Set Drop-off to ${landmark.name}", Toast.LENGTH_SHORT).show()
                                selectedLandmark = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Set Drop-off 🔴", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveDriverDetailsCard(
    activeRide: BookedRide,
    context: android.content.Context,
    viewModel: AutoBookingViewModel? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BentoGreyBorder, RoundedCornerShape(20.dp))
            .testTag("driver_details_card"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFBFCFB)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header status line inside the driver card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🛺", fontSize = 14.sp)
                    Text(
                        text = "Verified Namma Auto Driver",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoGreen
                    )
                }
                
                // Rating badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(Color(0xFFFEF9E7), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFF9E79F), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star Rating",
                        tint = Color(0xFFF1C40F),
                        modifier = Modifier.size(12.dp)
                    )
                    val ratingValue = remember(activeRide.driverName) {
                        val hash = activeRide.driverName.hashCode()
                        val positiveHash = if (hash < 0) -hash else hash
                        val offset = (positiveHash % 5) * 0.1
                        (4.5 + offset).toString().take(3)
                    }
                    Text(
                        text = "$ratingValue Rating",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF7D6608)
                    )
                }
            }

            Divider(color = BentoGreyBorder.copy(alpha = 0.5f), thickness = 1.dp)

            // Dynamic Live ETA / Distance display
            if (viewModel != null) {
                val isArriving = activeRide.status == "ACCEPTED"
                val isStarted = activeRide.status == "STARTED"
                val isArrived = activeRide.status == "ARRIVED"
                
                if (isArriving || isStarted || isArrived) {
                    val (eta, dist) = when {
                        isStarted -> viewModel.getDropoffEtaAndDistance()
                        isArrived -> Pair("0 mins", "0 m")
                        else -> viewModel.getPickupEtaAndDistance()
                    }
                    
                    val titleText = when {
                        isStarted -> "ETA to Dropoff"
                        isArrived -> "Arrived at Pickup"
                        else -> "ETA to Pickup"
                    }
                    
                    val bcolor = when {
                        isStarted -> BentoGreen
                        isArrived -> BentoYellow
                        else -> Color(0xFF0288D1)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bcolor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .border(1.dp, bcolor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                            .testTag("eta_container"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isStarted) "🏁" else "📍",
                                fontSize = 16.sp
                            )
                            Column {
                                Text(
                                    text = titleText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = if (isArrived) "Driver is waiting" else "Distance: $dist",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoDarkText,
                                    modifier = Modifier.testTag("eta_distance_text")
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(bcolor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "ETA",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isArrived) "0 mins" else eta,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("eta_minutes_text")
                                )
                            }
                        }
                    }
                    
                    Divider(color = BentoGreyBorder.copy(alpha = 0.3f), thickness = 1.dp)
                }
            }

            // Main details row (Avatar, Name, vehicle plate, interactive call)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Driver avatar emoji with live decoration
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(BentoYellow.copy(alpha = 0.25f))
                            .border(1.5.dp, BentoYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👨🏽‍✈️", fontSize = 26.sp)
                    }

                    Column {
                        Text(
                            text = activeRide.driverName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BentoDarkText,
                            modifier = Modifier.testTag("driver_name")
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        // Phone contact with tap-to-toast trigger
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    Toast.makeText(context, "Dialing ${activeRide.driverPhone}... 📞", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Contact phone",
                                tint = BentoGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = activeRide.driverPhone,
                                fontSize = 11.sp,
                                color = BentoGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Realistic Indian Yellow-Black License Plate Design for Vehicle details!
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFEA3A), RoundedCornerShape(6.dp))
                            .border(1.5.dp, Color.Black, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeRide.driverVehicleNo,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color.Black,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.testTag("vehicle_no")
                        )
                    }
                    Text(
                        text = "Namma Auto Standard",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Divider(color = BentoGreyBorder.copy(alpha = 0.3f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("💳", fontSize = 14.sp)
                    Text(
                        text = "Selected Payment Method",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                }
                val pmLabel = when (activeRide.paymentMethod) {
                    "UPI" -> "📱 UPI Option"
                    "Digital Wallet" -> "👛 Digital Wallet"
                    else -> "💵 Cash on Arrival"
                }
                Text(
                    text = pmLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoDarkText,
                    modifier = Modifier.testTag("active_ride_payment_method")
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
