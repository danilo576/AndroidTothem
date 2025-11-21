package com.fashiontothem.ff.presentation.settings

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onBack: () -> Unit = {}
) {
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
            // Title
            Text(
                text = stringResource(id = R.string.settings_update_title),
                fontFamily = Fonts.Poppins,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
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
            
            // Category Settings Option
            SettingsOptionCard(
                title = stringResource(id = R.string.settings_category_settings_title),
                description = stringResource(id = R.string.settings_category_settings_description),
                onClick = debouncedCategorySettings
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Network Logger Option (for QA)
            SettingsOptionCard(
                title = "Network Logger",
                description = "View network requests and responses",
                onClick = debouncedNetworkLogger
            )
            
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
fun SettingsScreenPreviewPhilips() {
    SettingsScreen(
        onUpdateStoreLocations = {},
        onUpdatePickupPoint = {},
        onOpenCategorySettings = {},
        onBack = {}
    )
}

