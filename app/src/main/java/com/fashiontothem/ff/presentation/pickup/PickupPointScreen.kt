package com.fashiontothem.ff.presentation.pickup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.rememberDebouncedClick
import humer.UvcCamera.R

/**
 * F&F Tothem - Pickup Point Configuration Screen
 * 
 * Allows user to enable/disable pick-up point delivery option.
 */
@Composable
fun PickupPointScreen(
    viewModel: PickupPointViewModel = hiltViewModel(),
    onContinue: () -> Unit,
    isUpdateMode: Boolean = false
) {
    val storeName by viewModel.selectedStoreName.collectAsState(initial = "")
    val pickupEnabled by viewModel.pickupPointEnabled.collectAsState(initial = false)
    
    PickupPointContent(
        storeName = storeName ?: "",
        pickupEnabled = pickupEnabled,
        isUpdateMode = isUpdateMode,
        onPickupToggle = { viewModel.setPickupPointEnabled(it) },
        onContinue = {
            // Mark configuration as completed before navigating
            viewModel.markConfigurationCompleted()
            onContinue()
        }
    )
}

@Composable
private fun PickupPointContent(
    storeName: String,
    pickupEnabled: Boolean,
    isUpdateMode: Boolean = false,
    onPickupToggle: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
    // Debounced continue to prevent rapid clicks
    val debouncedContinue = rememberDebouncedClick(onClick = onContinue)
    
    // Background - splash_background
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Content Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 80.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.your_selected_location),
                            fontFamily = Fonts.Poppins,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        if (isUpdateMode) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(id = R.string.updating_pickup_point),
                                fontFamily = Fonts.Poppins,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF808080),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selected store name
                    Text(
                        text = storeName,
                        fontFamily = Fonts.Poppins,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB50938),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Pick-up point toggle card (clickable)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = if (pickupEnabled) Color(0xFF4CAF50) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onPickupToggle(!pickupEnabled) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (pickupEnabled) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.pickup_point_option),
                                    fontFamily = Fonts.Poppins,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            }
                            
                            Spacer(modifier = Modifier.size(16.dp))
                            
                            // Toggle Switch (visual indicator)
                            Switch(
                                checked = pickupEnabled,
                                onCheckedChange = onPickupToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4CAF50),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFD9D9D9)
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Continue Button
                    Button(
                        onClick = debouncedContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB50938)
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.continue_button),
                            fontFamily = Fonts.Poppins,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun PickupPointScreenPreviewPhilips() {
    PickupPointContent(
        storeName = "Fashion & Friends Delta City",
        pickupEnabled = true,
        onPickupToggle = {},
        onContinue = {}
    )
}

