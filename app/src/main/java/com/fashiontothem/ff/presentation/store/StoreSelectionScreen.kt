@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.fashiontothem.ff.presentation.store

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.fashiontothem.ff.domain.model.CountryStore
import com.fashiontothem.ff.presentation.common.FashionLoader
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.clickableDebounced
import humer.UvcCamera.BuildConfig
import humer.UvcCamera.R

/**
 * F&F Tothem - Store Selection Screen (Dialog Version)
 *
 * Dialog for selecting country and store.
 * Shows over splash_background.
 */
@Composable
fun StoreSelectionScreen(
    viewModel: StoreSelectionViewModel = hiltViewModel(),
    onStoreSelected: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Request notification permission in debug build (for Chucker)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (BuildConfig.DEBUG) {
            android.util.Log.d("StoreSelectionScreen", "Notification permission ${if (isGranted) "granted" else "denied"}")
        }
    }

    // Request notification permission on first launch (debug build only, Android 13+)
    LaunchedEffect(Unit) {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Navigate when store is selected
    LaunchedEffect(uiState.storeSelected) {
        if (uiState.storeSelected) {
            onStoreSelected()
        }
    }

    StoreSelectionContent(
        stores = uiState.stores,
        isLoading = uiState.isLoading,
        error = uiState.error,
        isSaving = uiState.isSaving,
        onStoreClick = { countryCode, storeCode ->
            viewModel.selectStore(countryCode, storeCode)
        },
        onRetry = { viewModel.loadStores() },
        onDismissError = { viewModel.dismissError() }
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun StoreSelectionContent(
    stores: List<CountryStore>,
    isLoading: Boolean,
    error: String?,
    isSaving: Boolean = false,
    onStoreClick: (String, String) -> Unit,
    onRetry: () -> Unit = {},
    onDismissError: () -> Unit = {},
) {
    // Background - splash_background
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dialog
        Dialog(
            onDismissRequest = { /* Cannot dismiss - must select */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Responsive dimensions based on screen size
                val screenWidth = maxWidth
                val screenHeight = maxHeight

                // Dialog width: responsive, max 600dp
                // Account for padding (16dp on each side = 32dp total)
                val availableWidth = screenWidth - 32.dp
                val dialogWidth = (availableWidth * 0.95f).coerceAtMost(600.dp)

                // Card padding: responsive
                val cardPadding = if (screenWidth < 400.dp) {
                    16.dp
                } else if (screenWidth < 600.dp) {
                    20.dp
                } else {
                    24.dp
                }

                // Title font size: responsive
                val titleFontSize = if (screenWidth < 400.dp) {
                    20.sp
                } else if (screenWidth < 600.dp) {
                    22.sp
                } else {
                    24.sp
                }

                // Title bottom padding: responsive
                val titleBottomPadding = if (screenHeight < 800.dp) {
                    16.dp
                } else {
                    24.dp
                }

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(200)) + scaleIn(
                        initialScale = 0.95f,
                        animationSpec = tween(200)
                    ),
                    exit = fadeOut(tween(150)) + scaleOut(
                        targetScale = 0.95f,
                        animationSpec = tween(150)
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .width(dialogWidth)
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(cardPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                fontFamily = Fonts.Poppins,
                                text = stringResource(id = R.string.select_country),
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = titleBottomPadding)
                            )

                            // Content
                            when {
                                isLoading -> LoadingContent(screenWidth = screenWidth)
                                error != null -> ErrorContent(
                                    error = error,
                                    onRetry = onRetry,
                                    onDismiss = onDismissError,
                                    screenWidth = screenWidth
                                )

                                else -> StoreList(
                                    stores = stores,
                                    isSaving = isSaving,
                                    onStoreClick = onStoreClick,
                                    screenWidth = screenWidth
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(screenWidth: androidx.compose.ui.unit.Dp) {
    val padding = if (screenWidth < 400.dp) {
        24.dp
    } else if (screenWidth < 600.dp) {
        32.dp
    } else {
        40.dp
    }

    val fontSize = if (screenWidth < 400.dp) {
        16.sp
    } else {
        18.sp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FashionLoader()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.loading_stores),
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp,
) {
    val padding = if (screenWidth < 400.dp) {
        20.dp
    } else if (screenWidth < 600.dp) {
        24.dp
    } else {
        32.dp
    }

    val emojiSize = if (screenWidth < 400.dp) {
        40.sp
    } else if (screenWidth < 600.dp) {
        50.sp
    } else {
        64.sp
    }

    val titleFontSize = if (screenWidth < 400.dp) {
        12.sp
    } else if (screenWidth < 600.dp) {
        18.sp
    } else {
        24.sp
    }

    val errorFontSize = if (screenWidth < 400.dp) {
        10.sp
    } else {
        14.sp
    }

    val buttonHeight = if (screenWidth < 400.dp) {
        40.dp
    } else {
        50.dp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "❌", fontSize = emojiSize)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.error_title),
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB50938)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = errorFontSize,
            color = Color.Black.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB50938)),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.retry_button),
                fontSize = if (screenWidth < 400.dp) 14.sp else 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StoreList(
    stores: List<CountryStore>,
    isSaving: Boolean,
    onStoreClick: (String, String) -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp,
) {
    val spacing = if (screenWidth < 400.dp) {
        8.dp
    } else {
        12.dp
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(stores) { countryStore ->
            CountryStoreCard(
                countryStore = countryStore,
                enabled = !isSaving,
                onClick = { storeCode ->
                    onStoreClick(countryStore.countryCode, storeCode)
                },
                screenWidth = screenWidth
            )
        }
    }
}

@Composable
private fun CountryStoreCard(
    countryStore: CountryStore,
    enabled: Boolean,
    onClick: (String) -> Unit,
    screenWidth: androidx.compose.ui.unit.Dp,
) {
    // Use flagcdn.com for high-quality, authentic flag images
    val flagUrl = "https://flagcdn.com/w160/${countryStore.countryCode.lowercase()}.png"

    // Responsive dimensions
    val cardPadding = if (screenWidth < 400.dp) {
        16.dp
    } else if (screenWidth < 600.dp) {
        18.dp
    } else {
        20.dp
    }

    val flagSize = if (screenWidth < 400.dp) {
        48.dp
    } else if (screenWidth < 600.dp) {
        54.dp
    } else {
        60.dp
    }

    val spacing = if (screenWidth < 400.dp) {
        12.dp
    } else {
        16.dp
    }

    val countryNameFontSize = if (screenWidth < 400.dp) {
        16.sp
    } else if (screenWidth < 600.dp) {
        18.sp
    } else {
        20.sp
    }

    val arrowFontSize = if (screenWidth < 400.dp) {
        28.sp
    } else if (screenWidth < 600.dp) {
        32.sp
    } else {
        35.sp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickableDebounced {
                if (enabled) {
                    // Click on first store (usually only one per country)
                    countryStore.stores.firstOrNull()?.let { store ->
                        onClick(store.code)
                    }
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Authentic flag image (circular)
            AsyncImage(
                model = flagUrl,
                contentDescription = countryStore.countryName,
                modifier = Modifier
                    .size(flagSize)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(spacing))

            // Country name
            Text(
                text = countryStore.countryName,
                fontSize = countryNameFontSize,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                fontFamily = Fonts.Poppins,
                modifier = Modifier.weight(1f)
            )

            // Arrow or loading indicator
            if (!enabled) {
                FashionLoader()
            } else {
                Text(
                    text = "→",
                    fontSize = arrowFontSize,
                    color = Color(0xFFB50938)
                )
            }
        }
    }
}

// Helper fora mock data
@Composable
private fun getMockStores(): List<CountryStore> {
    return listOf(
        CountryStore(
            countryCode = "RS",
            countryName = "Srbija",
            stores = listOf(
                com.fashiontothem.ff.domain.model.StoreConfig(
                    id = "1",
                    name = "Fashion & Friends Serbia",
                    code = "rs_SR",
                    websiteId = "1",
                    baseUrl = "https://www.fashionandfriends.com/rs/",
                    secureBaseUrl = "https://www.fashionandfriends.com/rs/",
                    baseMediaUrl = "https://fashion-assets.fashionandfriends.com/media/",
                    secureBaseMediaUrl = "https://fashion-assets.fashionandfriends.com/media/",
                    locale = "sr_Cyrl_RS",
                    baseCurrencyCode = "RSD",
                    defaultDisplayCurrencyCode = "RSD",
                    timezone = "Europe/Belgrade",
                    athenaSearchWebsiteUrl = "https://www.fashionandfriends.com/rs/",
                    athenaSearchWtoken = "mock_wtoken",
                    athenaSearchAccessToken = "mock_access_token"
                )
            )
        ),
        CountryStore(
            countryCode = "HR",
            countryName = "Hrvatska",
            stores = listOf(
                com.fashiontothem.ff.domain.model.StoreConfig(
                    id = "2",
                    name = "Fashion & Friends Croatia",
                    code = "hr_HR",
                    websiteId = "2",
                    baseUrl = "https://www.fashionandfriends.com/hr/",
                    secureBaseUrl = "https://www.fashionandfriends.com/hr/",
                    baseMediaUrl = "https://fashion-assets.fashionandfriends.com/media/",
                    secureBaseMediaUrl = "https://fashion-assets.fashionandfriends.com/media/",
                    locale = "hr_HR",
                    baseCurrencyCode = "EUR",
                    defaultDisplayCurrencyCode = "EUR",
                    timezone = "Europe/Zagreb",
                    athenaSearchWebsiteUrl = "https://www.fashionandfriends.com/hr/",
                    athenaSearchWtoken = "mock_wtoken_hr",
                    athenaSearchAccessToken = "mock_access_token_hr"
                )
            )
        )
    )
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun StoreSelectionScreenPreviewSmall() {
    StoreSelectionContent(
        stores = getMockStores(),
        isLoading = false,
        error = null,
        isSaving = false,
        onStoreClick = { _, _ -> },
        onRetry = {},
        onDismissError = {}
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun StoreSelectionScreenPreviewMedium() {
    StoreSelectionContent(
        stores = getMockStores(),
        isLoading = false,
        error = null,
        isSaving = false,
        onStoreClick = { _, _ -> },
        onRetry = {},
        onDismissError = {}
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun StoreSelectionScreenPreviewLarge() {
    StoreSelectionContent(
        stores = getMockStores(),
        isLoading = false,
        error = null,
        isSaving = false,
        onStoreClick = { _, _ -> },
        onRetry = {},
        onDismissError = {}
    )
}

@Preview(name = "Error State", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun StoreSelectionScreenPreviewError() {
    StoreSelectionContent(
        stores = emptyList(),
        isLoading = false,
        error = "Failed to load stores. Please check your internet connection.",
        isSaving = false,
        onStoreClick = { _, _ -> },
        onRetry = {},
        onDismissError = {}
    )
}

