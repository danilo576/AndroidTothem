package com.fashiontothem.ff.presentation.settings

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fashiontothem.ff.data.local.preferences.EnvironmentPreferences
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.rememberDebouncedClick
import humer.UvcCamera.R

/**
 * F&F Tothem - Settings Screen
 * 
 * Allows user to choose which setting to update: Store Locations or Pickup Point
 */
@Composable
fun SettingsScreen(
    onUpdateStoreLocations: () -> Unit,
    onUpdatePickupPoint: () -> Unit,
    onOpenNetworkLogger: () -> Unit = {},
    onOpenCategorySettings: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    SettingsScreenContent(
        onUpdateStoreLocations = onUpdateStoreLocations,
        onUpdatePickupPoint = onUpdatePickupPoint,
        onOpenNetworkLogger = onOpenNetworkLogger,
        onOpenCategorySettings = onOpenCategorySettings,
        onBack = onBack,
        selectedEnvironmentFlow = viewModel.selectedEnvironment,
        onEnvironmentChange = { newEnvironment ->
            viewModel.changeEnvironment(newEnvironment, context = context)
        }
    )
}

@Composable
private fun SettingsScreenContent(
    onUpdateStoreLocations: () -> Unit,
    onUpdatePickupPoint: () -> Unit,
    onOpenNetworkLogger: () -> Unit,
    onOpenCategorySettings: () -> Unit,
    onBack: () -> Unit,
    selectedEnvironmentFlow: StateFlow<String>,
    onEnvironmentChange: (String) -> Unit
) {
    val context = LocalContext.current
    val selectedEnvironment by selectedEnvironmentFlow.collectAsState()
    var showEnvironmentDropdown by remember { mutableStateOf(false) }
    
    // Hidden items state (for tester activation)
    var showHiddenItems by remember { mutableStateOf(false) }
    var clickCount by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    
    // Reset click count if more than 1 second passes between clicks
    LaunchedEffect(clickCount) {
        if (clickCount > 0) {
            delay(1000) // 1 second timeout
            if (clickCount < 5) {
                clickCount = 0
                lastClickTime = 0L
            }
        }
    }
    
    // Reset state when screen is disposed (user leaves)
    DisposableEffect(Unit) {
        onDispose {
            showHiddenItems = false
            clickCount = 0
            lastClickTime = 0L
        }
    }
    
    // Handle title click for hidden items activation
    val onTitleClick = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 1000) {
            // Click within 1 second - increment counter
            clickCount++
            if (clickCount >= 5) {
                showHiddenItems = true
                clickCount = 0
                lastClickTime = 0L
            }
        } else {
            // First click or too much time passed - reset
            clickCount = 1
        }
        lastClickTime = currentTime
    }
    
    // Debounced clicks to prevent rapid clicks
    val debouncedStoreLocations = rememberDebouncedClick(onClick = onUpdateStoreLocations)
    val debouncedPickupPoint = rememberDebouncedClick(onClick = onUpdatePickupPoint)
    val debouncedNetworkLogger = rememberDebouncedClick(onClick = onOpenNetworkLogger)
    val debouncedCategorySettings = rememberDebouncedClick(onClick = onOpenCategorySettings)
    val debouncedBack = rememberDebouncedClick(onClick = onBack)
    
    // Background - splash_background
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title (clickable for hidden items activation)
            Text(
                text = stringResource(id = R.string.settings_update_title),
                fontFamily = Fonts.Poppins,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { onTitleClick() }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Environment Selection Dropdown (hidden by default)
            if (showHiddenItems) {
                EnvironmentDropdownCard(
                    selectedEnvironment = selectedEnvironment,
                    onEnvironmentSelected = { newEnvironment ->
                        onEnvironmentChange(newEnvironment)
                        showEnvironmentDropdown = false
                    },
                    showDropdown = showEnvironmentDropdown,
                    onDropdownToggle = { showEnvironmentDropdown = !showEnvironmentDropdown }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Store Locations Option
            SettingsOptionCard(
                title = stringResource(id = R.string.settings_store_location_title),
                description = stringResource(id = R.string.settings_store_location_description),
                onClick = debouncedStoreLocations
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pickup Point Option
            SettingsOptionCard(
                title = stringResource(id = R.string.settings_pickup_point_title),
                description = stringResource(id = R.string.settings_pickup_point_description),
                onClick = debouncedPickupPoint
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Category Settings Option (hidden by default)
            if (showHiddenItems) {
                SettingsOptionCard(
                    title = stringResource(id = R.string.settings_category_settings_title),
                    description = stringResource(id = R.string.settings_category_settings_description),
                    onClick = debouncedCategorySettings
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Network Logger Option (hidden by default, for QA)
            if (showHiddenItems) {
                SettingsOptionCard(
                    title = "Network Logger",
                    description = "View network requests and responses",
                    onClick = debouncedNetworkLogger
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Close Button
            Button(
                onClick = debouncedBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB50938)
                )
            ) {
                Text(
                    text = "Zatvori",
                    fontFamily = Fonts.Poppins,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun EnvironmentDropdownCard(
    selectedEnvironment: String,
    onEnvironmentSelected: (String) -> Unit,
    showDropdown: Boolean,
    onDropdownToggle: () -> Unit
) {
    val environmentOptions = listOf(
        EnvironmentPreferences.ENVIRONMENT_PRODUCTION to "Produkcija",
        EnvironmentPreferences.ENVIRONMENT_DEVELOPMENT to "Development"
    )
    
    val selectedLabel = environmentOptions.find { it.first == selectedEnvironment }?.second 
        ?: selectedEnvironment
    
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDropdownToggle() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Okruženje",
                        fontFamily = Fonts.Poppins,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Trenutno: $selectedLabel",
                        fontFamily = Fonts.Poppins,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF808080)
                    )
                }
                
                Spacer(modifier = Modifier.size(16.dp))
                
                // Dropdown arrow
                Text(
                    text = if (showDropdown) "▲" else "▼",
                    fontSize = 24.sp,
                    color = Color(0xFFB50938),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { onDropdownToggle() },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.White)
        ) {
            environmentOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            fontFamily = Fonts.Poppins,
                            fontSize = 18.sp,
                            fontWeight = if (value == selectedEnvironment) FontWeight.Bold else FontWeight.Normal,
                            color = if (value == selectedEnvironment) Color(0xFFB50938) else Color.Black
                        )
                    },
                    onClick = {
                        onEnvironmentSelected(value)
                    },
                    colors = androidx.compose.material3.MenuDefaults.itemColors(
                        leadingIconColor = if (value == selectedEnvironment) Color(0xFFB50938) else Color.Black,
                        trailingIconColor = if (value == selectedEnvironment) Color(0xFFB50938) else Color.Black
                    )
                )
            }
        }
    }
}

@Composable
private fun SettingsOptionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontFamily = Fonts.Poppins,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    fontFamily = Fonts.Poppins,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF808080)
                )
            }
            
            Spacer(modifier = Modifier.size(16.dp))
            
            // Arrow icon (using a simple text arrow for now)
            Text(
                text = "→",
                fontSize = 32.sp,
                color = Color(0xFFB50938),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
private fun SettingsScreenPreviewPhilips() {
    // Preview without ViewModel - use default values
    val previewEnvironment = remember { mutableStateOf(EnvironmentPreferences.ENVIRONMENT_PRODUCTION) }
    SettingsScreenContent(
        onUpdateStoreLocations = {},
        onUpdatePickupPoint = {},
        onOpenNetworkLogger = {},
        onOpenCategorySettings = {},
        onBack = {},
        selectedEnvironmentFlow = remember { 
            kotlinx.coroutines.flow.MutableStateFlow(EnvironmentPreferences.ENVIRONMENT_PRODUCTION) as StateFlow<String>
        },
        onEnvironmentChange = { previewEnvironment.value = it }
    )
}

