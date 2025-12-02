package com.fashiontothem.ff.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fashiontothem.ff.data.local.preferences.EnvironmentPreferences
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.rememberDebouncedClick
import humer.UvcCamera.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

/**
 * F&F Tothem - Settings Screen
 *
 * Allows user to choose which setting to update: Store Locations or Pickup Point
 */
@Composable
fun SettingsScreen(
    onUpdateStoreLocations: () -> Unit,
    onUpdatePickupPoint: () -> Unit,
    onOpenCategorySettings: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    SettingsScreenContent(
        onUpdateStoreLocations = onUpdateStoreLocations,
        onUpdatePickupPoint = onUpdatePickupPoint,
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
    onOpenCategorySettings: () -> Unit,
    onBack: () -> Unit,
    selectedEnvironmentFlow: StateFlow<String>,
    onEnvironmentChange: (String) -> Unit,
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
    val debouncedCategorySettings = rememberDebouncedClick(onClick = onOpenCategorySettings)
    val debouncedBack = rememberDebouncedClick(onClick = onBack)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

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

            // Responsive padding
            val horizontalPadding = when {
                screenWidth < 400.dp -> 12.dp
                screenWidth < 600.dp -> 18.dp
                else -> 40.dp
            }

            val verticalPadding = when {
                screenHeight < 700.dp -> 20.dp
                screenHeight < 1200.dp -> 44.dp
                else -> 80.dp
            }

            // Responsive spacing
            val titleBottomSpacing = when {
                screenHeight < 700.dp -> 20.dp
                screenHeight < 1200.dp -> 30.dp
                else -> 48.dp
            }

            val cardSpacing = when {
                screenHeight < 700.dp -> 12.dp
                screenHeight < 1200.dp -> 16.dp
                else -> 24.dp
            }

            val buttonTopSpacing = when {
                screenHeight < 700.dp -> 20.dp
                screenHeight < 1200.dp -> 32.dp
                else -> 48.dp
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title (clickable for hidden items activation)
                // Responsive title font size
                val titleFontSize = when {
                    screenWidth < 400.dp -> 12.sp
                    screenWidth < 600.dp -> 18.sp
                    else -> 32.sp
                }

                Text(
                    text = stringResource(id = R.string.settings_update_title),
                    fontFamily = Fonts.Poppins,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { onTitleClick() }
                )

                Spacer(modifier = Modifier.height(titleBottomSpacing))

                // Environment Selection Dropdown (hidden by default)
                if (showHiddenItems) {
                    EnvironmentDropdownCard(
                        selectedEnvironment = selectedEnvironment,
                        onEnvironmentSelected = { newEnvironment ->
                            onEnvironmentChange(newEnvironment)
                            showEnvironmentDropdown = false
                        },
                        showDropdown = showEnvironmentDropdown,
                        onDropdownToggle = { showEnvironmentDropdown = !showEnvironmentDropdown },
                        screenWidth = screenWidth
                    )

                    Spacer(modifier = Modifier.height(cardSpacing))
                }

                // Store Locations Option
                SettingsOptionCard(
                    title = stringResource(id = R.string.settings_store_location_title),
                    description = stringResource(id = R.string.settings_store_location_description),
                    onClick = debouncedStoreLocations,
                    screenWidth = screenWidth
                )

                Spacer(modifier = Modifier.height(cardSpacing))

                // Pickup Point Option
                SettingsOptionCard(
                    title = stringResource(id = R.string.settings_pickup_point_title),
                    description = stringResource(id = R.string.settings_pickup_point_description),
                    onClick = debouncedPickupPoint,
                    screenWidth = screenWidth
                )

                Spacer(modifier = Modifier.height(cardSpacing))

                // Category Settings Option (hidden by default)
                if (showHiddenItems) {
                    SettingsOptionCard(
                        title = stringResource(id = R.string.settings_category_settings_title),
                        description = stringResource(id = R.string.settings_category_settings_description),
                        onClick = debouncedCategorySettings,
                        screenWidth = screenWidth
                    )

                    Spacer(modifier = Modifier.height(cardSpacing))
                }

                Spacer(modifier = Modifier.height(buttonTopSpacing))

                // Close Button
                // Responsive button height
                val buttonHeight = when {
                    screenWidth < 400.dp -> 30.dp
                    screenWidth < 600.dp -> 34.dp
                    else -> 56.dp
                }

                // Responsive button corner radius
                val buttonCornerRadius = when {
                    screenWidth < 400.dp -> 16.dp
                    screenWidth < 600.dp -> 18.dp
                    else -> 20.dp
                }

                // Responsive button font size
                val buttonFontSize = when {
                    screenWidth < 400.dp -> 10.sp
                    screenWidth < 600.dp -> 14.sp
                    else -> 20.sp
                }

                Button(
                    onClick = debouncedBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
                    shape = RoundedCornerShape(buttonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB50938)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.close_settings_label),
                        fontFamily = Fonts.Poppins,
                        fontSize = buttonFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentDropdownCard(
    selectedEnvironment: String,
    onEnvironmentSelected: (String) -> Unit,
    showDropdown: Boolean,
    onDropdownToggle: () -> Unit,
    screenWidth: Dp,
) {
    val environmentOptions = listOf(
        EnvironmentPreferences.ENVIRONMENT_PRODUCTION to "Produkcija",
        EnvironmentPreferences.ENVIRONMENT_DEVELOPMENT to "Development"
    )

    val selectedLabel = environmentOptions.find { it.first == selectedEnvironment }?.second
        ?: selectedEnvironment

    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 16.dp
        screenWidth < 600.dp -> 18.dp
        else -> 20.dp
    }

    // Responsive elevation
    val elevation = when {
        screenWidth < 400.dp -> 4.dp
        screenWidth < 600.dp -> 6.dp
        else -> 8.dp
    }

    // Responsive padding
    val cardPadding = when {
        screenWidth < 400.dp -> 16.dp
        screenWidth < 600.dp -> 20.dp
        else -> 24.dp
    }

    // Responsive spacing
    val titleDescriptionSpacing = when {
        screenWidth < 400.dp -> 4.dp
        screenWidth < 600.dp -> 6.dp
        else -> 8.dp
    }

    val columnArrowSpacing = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 12.dp
        else -> 16.dp
    }

    // Responsive title font size
    val titleFontSize = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 16.sp
        else -> 22.sp
    }

    // Responsive description font size
    val descriptionFontSize = when {
        screenWidth < 400.dp -> 10.sp
        screenWidth < 600.dp -> 12.sp
        else -> 16.sp
    }

    // Responsive arrow font size
    val arrowFontSize = when {
        screenWidth < 400.dp -> 14.sp
        screenWidth < 600.dp -> 18.sp
        else -> 24.sp
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDropdownToggle() },
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.environment_label),
                        fontFamily = Fonts.Poppins,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(titleDescriptionSpacing))
                    Text(
                        text = "Trenutno: $selectedLabel",
                        fontFamily = Fonts.Poppins,
                        fontSize = descriptionFontSize,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF808080)
                    )
                }

                Spacer(modifier = Modifier.size(columnArrowSpacing))

                // Dropdown arrow
                Text(
                    text = if (showDropdown) "▲" else "▼",
                    fontSize = arrowFontSize,
                    color = Color(0xFFB50938),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Responsive dropdown menu width
        val dropdownWidth = when {
            screenWidth < 400.dp -> 0.95f
            screenWidth < 600.dp -> 0.92f
            else -> 0.9f
        }

        // Responsive dropdown menu item font size
        val menuItemFontSize = when {
            screenWidth < 400.dp -> 10.sp
            screenWidth < 600.dp -> 14.sp
            else -> 18.sp
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { onDropdownToggle() },
            modifier = Modifier
                .fillMaxWidth(dropdownWidth)
                .background(Color.White)
        ) {
            environmentOptions.forEach { (value, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            fontFamily = Fonts.Poppins,
                            fontSize = menuItemFontSize,
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
    onClick: () -> Unit,
    screenWidth: Dp,
) {
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 16.dp
        screenWidth < 600.dp -> 18.dp
        else -> 20.dp
    }

    // Responsive elevation
    val elevation = when {
        screenWidth < 400.dp -> 4.dp
        screenWidth < 600.dp -> 6.dp
        else -> 8.dp
    }

    // Responsive padding
    val cardPadding = when {
        screenWidth < 400.dp -> 16.dp
        screenWidth < 600.dp -> 20.dp
        else -> 24.dp
    }

    // Responsive spacing
    val titleDescriptionSpacing = when {
        screenWidth < 400.dp -> 4.dp
        screenWidth < 600.dp -> 6.dp
        else -> 8.dp
    }

    val columnArrowSpacing = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 12.dp
        else -> 16.dp
    }

    // Responsive title font size
    val titleFontSize = when {
        screenWidth < 400.dp -> 10.sp
        screenWidth < 600.dp -> 14.sp
        else -> 22.sp
    }

    // Responsive description font size
    val descriptionFontSize = when {
        screenWidth < 400.dp -> 10.sp
        screenWidth < 600.dp -> 14.sp
        else -> 16.sp
    }

    // Responsive arrow font size
    val arrowFontSize = when {
        screenWidth < 400.dp -> 20.sp
        screenWidth < 600.dp -> 26.sp
        else -> 32.sp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontFamily = Fonts.Poppins,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(titleDescriptionSpacing))
                Text(
                    text = description,
                    fontFamily = Fonts.Poppins,
                    fontSize = descriptionFontSize,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF808080)
                )
            }

            Spacer(modifier = Modifier.size(columnArrowSpacing))

            // Arrow icon (using a simple text arrow for now)
            Text(
                text = "→",
                fontSize = arrowFontSize,
                color = Color(0xFFB50938),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Helper function to create preview StateFlow
@Composable
private fun createPreviewEnvironmentFlow(): StateFlow<String> {
    return remember {
        kotlinx.coroutines.flow.MutableStateFlow(EnvironmentPreferences.ENVIRONMENT_PRODUCTION) as StateFlow<String>
    }
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun SettingsScreenPreviewSmall() {
    val previewEnvironment =
        remember { mutableStateOf(EnvironmentPreferences.ENVIRONMENT_PRODUCTION) }
    SettingsScreenContent(
        onUpdateStoreLocations = {},
        onUpdatePickupPoint = {},
        onOpenCategorySettings = {},
        onBack = {},
        selectedEnvironmentFlow = createPreviewEnvironmentFlow(),
        onEnvironmentChange = { previewEnvironment.value = it }
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
private fun SettingsScreenPreviewMedium() {
    val previewEnvironment =
        remember { mutableStateOf(EnvironmentPreferences.ENVIRONMENT_PRODUCTION) }
    SettingsScreenContent(
        onUpdateStoreLocations = {},
        onUpdatePickupPoint = {},
        onOpenCategorySettings = {},
        onBack = {},
        selectedEnvironmentFlow = createPreviewEnvironmentFlow(),
        onEnvironmentChange = { previewEnvironment.value = it }
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
private fun SettingsScreenPreviewLarge() {
    val previewEnvironment =
        remember { mutableStateOf(EnvironmentPreferences.ENVIRONMENT_PRODUCTION) }
    SettingsScreenContent(
        onUpdateStoreLocations = {},
        onUpdatePickupPoint = {},
        onOpenCategorySettings = {},
        onBack = {},
        selectedEnvironmentFlow = createPreviewEnvironmentFlow(),
        onEnvironmentChange = { previewEnvironment.value = it }
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
private fun SettingsScreenPreviewPhilips() {
    val previewEnvironment =
        remember { mutableStateOf(EnvironmentPreferences.ENVIRONMENT_PRODUCTION) }
    SettingsScreenContent(
        onUpdateStoreLocations = {},
        onUpdatePickupPoint = {},
        onOpenCategorySettings = {},
        onBack = {},
        selectedEnvironmentFlow = createPreviewEnvironmentFlow(),
        onEnvironmentChange = { previewEnvironment.value = it }
    )
}

