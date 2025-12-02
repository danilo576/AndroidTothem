package com.fashiontothem.ff.presentation.pickup

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    isUpdateMode: Boolean = false,
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun PickupPointContent(
    storeName: String,
    pickupEnabled: Boolean,
    isUpdateMode: Boolean = false,
    onPickupToggle: (Boolean) -> Unit,
    onContinue: () -> Unit,
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
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Responsive dimensions based on screen size
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            // Responsive padding: smaller on small screens
            val horizontalPadding = if (screenWidth < 400.dp) {
                16.dp
            } else if (screenWidth < 600.dp) {
                24.dp
            } else {
                40.dp
            }

            val verticalPadding = if (screenHeight < 800.dp) {
                24.dp
            } else if (screenHeight < 1200.dp) {
                40.dp
            } else {
                80.dp
            }

            // Card width: 90% on small screens, max 600dp on large screens
            val cardWidth = (screenWidth - horizontalPadding * 2).coerceAtMost(600.dp)

            // Card padding: responsive
            val cardPadding = if (screenWidth < 400.dp) {
                20.dp
            } else if (screenWidth < 600.dp) {
                24.dp
            } else {
                32.dp
            }

            // Title font size: responsive
            val titleFontSize = if (screenWidth < 400.dp) {
                22.sp
            } else if (screenWidth < 600.dp) {
                25.sp
            } else {
                28.sp
            }

            // Update mode font size: responsive
            val updateModeFontSize = if (screenWidth < 400.dp) {
                12.sp
            } else {
                14.sp
            }

            // Store name font size: responsive
            val storeNameFontSize = if (screenWidth < 400.dp) {
                18.sp
            } else if (screenWidth < 600.dp) {
                21.sp
            } else {
                24.sp
            }

            // Spacing: responsive
            val titleBottomSpacing = if (screenHeight < 800.dp) {
                12.dp
            } else {
                16.dp
            }

            val storeNameBottomSpacing = if (screenHeight < 800.dp) {
                24.dp
            } else if (screenHeight < 1200.dp) {
                36.dp
            } else {
                48.dp
            }

            val toggleCardBottomSpacing = if (screenHeight < 800.dp) {
                24.dp
            } else if (screenHeight < 1200.dp) {
                36.dp
            } else {
                48.dp
            }

            // Toggle card padding: responsive
            val toggleCardPadding = if (screenWidth < 400.dp) {
                16.dp
            } else if (screenWidth < 600.dp) {
                18.dp
            } else {
                20.dp
            }

            // Toggle option font size: responsive
            val toggleOptionFontSize = if (screenWidth < 400.dp) {
                16.sp
            } else if (screenWidth < 600.dp) {
                18.sp
            } else {
                20.sp
            }

            // Button height: responsive
            val buttonHeight = if (screenWidth < 400.dp) {
                50.dp
            } else {
                56.dp
            }

            // Button font size: responsive
            val buttonFontSize = if (screenWidth < 400.dp) {
                16.sp
            } else {
                18.sp
            }

            Box(
                modifier = Modifier
                    .width(cardWidth)
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
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
                            .padding(cardPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.your_selected_location),
                                fontFamily = Fonts.Poppins,
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            if (isUpdateMode) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = R.string.updating_pickup_point),
                                    fontFamily = Fonts.Poppins,
                                    fontSize = updateModeFontSize,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF808080),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(titleBottomSpacing))

                        // Selected store name
                        Text(
                            text = storeName,
                            fontFamily = Fonts.Poppins,
                            fontSize = storeNameFontSize,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB50938),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(storeNameBottomSpacing))

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
                                containerColor = if (pickupEnabled) Color(0xFFE8F5E9) else Color(
                                    0xFFF5F5F5
                                )
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(toggleCardPadding),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.pickup_point_option),
                                        fontFamily = Fonts.Poppins,
                                        fontSize = toggleOptionFontSize,
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

                        Spacer(modifier = Modifier.height(toggleCardBottomSpacing))

                        // Continue Button
                        Button(
                            onClick = debouncedContinue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB50938)
                            )
                        ) {
                            Text(
                                text = stringResource(id = R.string.continue_button),
                                fontFamily = Fonts.Poppins,
                                fontSize = buttonFontSize,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun PickupPointScreenPreviewSmall() {
    PickupPointContent(
        storeName = "Fashion & Friends Delta City",
        pickupEnabled = true,
        isUpdateMode = false,
        onPickupToggle = {},
        onContinue = {}
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun PickupPointScreenPreviewMedium() {
    PickupPointContent(
        storeName = "Fashion & Friends Delta City",
        pickupEnabled = true,
        isUpdateMode = false,
        onPickupToggle = {},
        onContinue = {}
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun PickupPointScreenPreviewLarge() {
    PickupPointContent(
        storeName = "Fashion & Friends Delta City",
        pickupEnabled = true,
        isUpdateMode = false,
        onPickupToggle = {},
        onContinue = {}
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun PickupPointScreenPreviewPhilips() {
    PickupPointContent(
        storeName = "Fashion & Friends Delta City",
        pickupEnabled = true,
        isUpdateMode = false,
        onPickupToggle = {},
        onContinue = {}
    )
}

@Preview(name = "Pickup Disabled", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun PickupPointScreenPreviewDisabled() {
    PickupPointContent(
        storeName = "Fashion & Friends Delta City",
        pickupEnabled = false,
        isUpdateMode = false,
        onPickupToggle = {},
        onContinue = {}
    )
}

@Preview(name = "Update Mode", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun PickupPointScreenPreviewUpdateMode() {
    PickupPointContent(
        storeName = "Fashion & Friends Delta City",
        pickupEnabled = true,
        isUpdateMode = true,
        onPickupToggle = {},
        onContinue = {}
    )
}

@Preview(name = "Long Store Name", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun PickupPointScreenPreviewLongName() {
    PickupPointContent(
        storeName = "Fashion & Friends Delta City Shopping Center - Novi Beograd",
        pickupEnabled = true,
        isUpdateMode = false,
        onPickupToggle = {},
        onContinue = {}
    )
}

