package com.fashiontothem.ff.presentation.home

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fashiontothem.ff.core.scanner.BarcodeScannerEvents
import com.fashiontothem.ff.domain.model.FilterOptions
import com.fashiontothem.ff.domain.repository.ProductFilters
import com.fashiontothem.ff.domain.repository.ProductPageResult
import com.fashiontothem.ff.presentation.common.DownloadAppDialog
import com.fashiontothem.ff.presentation.common.FindItemDialog
import com.fashiontothem.ff.presentation.common.LoyaltyDialog
import com.fashiontothem.ff.presentation.common.ScanAndFindDialog
import com.fashiontothem.ff.ui.theme.Fonts
import humer.UvcCamera.R
import kotlinx.coroutines.delay

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HomeScreen(
    onStartCamera: () -> Unit = {},
    onNavigateToProducts: (categoryId: String, categoryLevel: String) -> Unit = { _, _ -> },
    onNavigateToFilter: () -> Unit = {},
    onNavigateToProductDetails: (barcode: String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    // Preload brand images when HomeScreen is displayed
    LaunchedEffect(Unit) {
        viewModel.preloadBrandImages()
    }

    // Get category IDs and levels from preferences
    val newItemsCategoryId by viewModel.newItemsCategoryId.collectAsState(initial = "223")
    val newItemsCategoryLevel by viewModel.newItemsCategoryLevel.collectAsState(initial = "3")
    val actionsCategoryId by viewModel.actionsCategoryId.collectAsState(initial = "630")
    val actionsCategoryLevel by viewModel.actionsCategoryLevel.collectAsState(initial = "2")

    // Poppins font family (regular, medium, semibold, bold) from res/font
    val poppins = Fonts.Poppins
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showLoyaltyDialog by remember { mutableStateOf(false) }
    var showFindItemDialog by remember { mutableStateOf(false) }
    var showScanAndFindDialog by remember { mutableStateOf(false) }
    var scanHandlingInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        BarcodeScannerEvents.scans.collect { barcode ->
            if (scanHandlingInProgress) return@collect
            scanHandlingInProgress = true

            try {
                showDownloadDialog = false
                showLoyaltyDialog = false
                showFindItemDialog = false
                showScanAndFindDialog = false

                // Check if barcode is "FASHION SETTINGS"
                if (barcode.trim().equals("FASHION SETTINGS", ignoreCase = true)) {
                    onNavigateToSettings()
                } else {
                    onNavigateToProductDetails(barcode)
                }
            } finally {
                delay(600)
                scanHandlingInProgress = false
            }
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenH = maxHeight
        val screenW = maxWidth

        // Responsive side padding
        val sidePadding = when {
            screenW < 400.dp -> 16.dp
            screenW < 600.dp -> 24.dp
            else -> 40.dp
        }

        // Responsive logo height
        val logoHeight = when {
            screenH < 700.dp -> (screenH * 0.05f).coerceAtLeast(36.dp).coerceAtMost(50.dp)
            screenH < 1200.dp -> (screenH * 0.06f).coerceAtLeast(44.dp).coerceAtMost(70.dp)
            else -> (screenH * 0.06f).coerceAtLeast(44.dp)
        }

        // Responsive search card height
        val searchHeight = when {
            screenH < 700.dp -> (screenH * 0.12f).coerceAtLeast(70.dp).coerceAtMost(120.dp)
            screenH < 1200.dp -> (screenH * 0.10f).coerceAtLeast(88.dp).coerceAtMost(150.dp)
            else -> (screenH * 0.10f).coerceAtLeast(88.dp)
        }

        // Responsive feature card height
        val cardHeight = when {
            screenH < 700.dp -> (screenH * 0.12f).coerceAtLeast(80.dp).coerceAtMost(120.dp)
            screenH < 1200.dp -> (screenH * 0.10f).coerceAtLeast(108.dp).coerceAtMost(160.dp)
            else -> (screenH * 0.10f).coerceAtLeast(108.dp)
        }

        // Responsive vertical gaps
        val verticalGap = when {
            screenH < 700.dp -> 12.dp
            screenH < 1200.dp -> 16.dp
            else -> 20.dp
        }

        // Responsive corner radius
        val corner = when {
            screenW < 400.dp -> 30.dp
            screenW < 600.dp -> 40.dp
            else -> 50.dp
        }

        val borderStroke = 2.dp
        val glassAlpha = 0.28f

        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sidePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Responsive top spacing
            val topSpacing = when {
                screenH < 700.dp -> 8.dp
                screenH < 1200.dp -> 12.dp
                else -> 15.dp
            }

            Spacer(Modifier.height(topSpacing))

            Image(
                modifier = Modifier
                    .height(logoHeight)
                    .padding(start = if (screenW < 400.dp) 4.dp else 10.dp),
                painter = painterResource(id = R.drawable.fashion_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )

            // Responsive spacing after logo
            val logoBottomSpacing = when {
                screenH < 700.dp -> 12.dp
                screenH < 1200.dp -> 16.dp
                else -> 20.dp
            }
            Spacer(Modifier.height(logoBottomSpacing))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlassCard(corner, borderStroke, glassAlpha, screenW) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(searchHeight)
                                .clickable { showFindItemDialog = true },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Responsive icon height
                            val iconHeight = when {
                                screenW < 400.dp -> (searchHeight * 0.50f).coerceAtLeast(36.dp)
                                screenW < 600.dp -> (searchHeight * 0.52f).coerceAtLeast(40.dp)
                                else -> (searchHeight * 0.55f).coerceAtLeast(44.dp)
                            }

                            Image(
                                painterResource(id = R.drawable.find_item),
                                null,
                                Modifier.height(iconHeight),
                                contentScale = ContentScale.Fit
                            )

                            // Responsive spacing
                            val iconTextSpacing = when {
                                screenH < 700.dp -> 2.dp
                                else -> 4.dp
                            }
                            Spacer(Modifier.height(iconTextSpacing))

                            // Responsive font size
                            val searchTextSize = when {
                                screenW < 400.dp -> 12.sp
                                screenW < 600.dp -> 18.sp
                                else -> 30.sp
                            }

                            Text(
                                stringResource(R.string.find_item_home_label),
                                color = Color.White,
                                fontSize = searchTextSize,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = poppins
                            )
                        }
                    }

                    Spacer(Modifier.height(verticalGap))

                    // Responsive horizontal spacing between columns
                    val columnSpacing = when {
                        screenW < 400.dp -> 12.dp
                        screenW < 600.dp -> 14.dp
                        else -> 16.dp
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(columnSpacing)
                    ) {
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(verticalGap)
                        ) {
                            FeatureCard(
                                R.drawable.new_icon,
                                stringResource(id = R.string.new_items),
                                cardHeight,
                                corner,
                                borderStroke,
                                glassAlpha,
                                poppins,
                                screenW,
                                screenH,
                                onClick = {
                                    onNavigateToProducts(
                                        newItemsCategoryId,
                                        newItemsCategoryLevel
                                    )
                                }
                            )
                            FeatureCard(
                                R.drawable.download_app,
                                stringResource(id = R.string.download_app),
                                cardHeight,
                                corner,
                                borderStroke,
                                glassAlpha,
                                poppins,
                                screenW,
                                screenH,
                                onClick = { showDownloadDialog = true }
                            )
                        }
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(verticalGap)
                        ) {
                            FeatureCard(
                                R.drawable.actions,
                                stringResource(id = R.string.actions),
                                cardHeight,
                                corner,
                                borderStroke,
                                glassAlpha,
                                poppins,
                                screenW,
                                screenH,
                                onClick = {
                                    onNavigateToProducts(
                                        actionsCategoryId,
                                        actionsCategoryLevel
                                    )
                                }
                            )
                            FeatureCard(
                                R.drawable.loaylty,
                                stringResource(id = R.string.loyalty_program),
                                cardHeight,
                                corner,
                                borderStroke,
                                glassAlpha,
                                poppins,
                                screenW,
                                screenH,
                                onClick = { showLoyaltyDialog = true }
                            )
                        }
                    }
                }
            }

            // Responsive diesel logo height
            val dieselLogoHeight = when {
                screenH < 700.dp -> (screenH * 0.04f).coerceAtLeast(36.dp).coerceAtMost(50.dp)
                screenH < 1200.dp -> (screenH * 0.05f).coerceAtLeast(48.dp).coerceAtMost(70.dp)
                else -> (screenH * 0.05f).coerceAtLeast(48.dp)
            }

            Image(
                painter = painterResource(id = R.drawable.diesel_logo),
                contentDescription = null,
                modifier = Modifier.height(dieselLogoHeight),
                contentScale = ContentScale.Fit
            )

            // Responsive bottom spacing
            val bottomSpacing = when {
                screenH < 700.dp -> 16.dp
                screenH < 1200.dp -> 24.dp
                else -> 30.dp
            }
            Spacer(Modifier.height(bottomSpacing))
        }
    }

    if (showDownloadDialog) {
        DownloadAppDialog(
            onDismiss = { showDownloadDialog = false }
        )
    }

    if (showLoyaltyDialog) {
        LoyaltyDialog(
            onDismiss = { showLoyaltyDialog = false }
        )
    }

    if (showFindItemDialog) {
        FindItemDialog(
            onDismiss = { showFindItemDialog = false },
            onScanAndFind = {
                showFindItemDialog = false
                showScanAndFindDialog = true
            },
            onFilterAndFind = {
                showFindItemDialog = false
                onNavigateToFilter()
            },
            onVisualSearch = {
                showFindItemDialog = false
                onStartCamera()
            }
        )
    }

    if (showScanAndFindDialog) {
        ScanAndFindDialog(
            onDismiss = { showScanAndFindDialog = false }
        )
    }

}

@Composable
private fun FeatureCard(
    icon: Int,
    label: String,
    height: Dp,
    corner: Dp,
    borderStroke: Dp,
    glassAlpha: Float,
    fontFamily: FontFamily,
    screenWidth: Dp,
    screenHeight: Dp,
    onClick: (() -> Unit)? = null,
) {
    GlassCard(corner, borderStroke, glassAlpha, screenWidth) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clickable { onClick?.invoke() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Responsive icon height
            val iconHeight = when {
                screenWidth < 400.dp -> (height * 0.35f).coerceAtLeast(30.dp)
                screenWidth < 600.dp -> (height * 0.36f).coerceAtLeast(32.dp)
                else -> (height * 0.38f).coerceAtLeast(44.dp)
            }

            Image(
                painterResource(id = icon),
                null,
                Modifier.height(iconHeight),
                contentScale = ContentScale.Fit
            )

            // Responsive spacing
            val iconTextSpacing = when {
                screenHeight < 700.dp -> 4.dp
                screenHeight < 1200.dp -> 6.dp
                else -> 8.dp
            }
            Spacer(Modifier.height(iconTextSpacing))

            // Responsive font size
            val cardTextSize = when {
                screenWidth < 400.dp -> 10.sp
                screenWidth < 600.dp -> 14.sp
                else -> 30.sp
            }

            Text(
                text = label,
                color = Color.White,
                fontSize = cardTextSize,
                fontWeight = FontWeight.Medium,
                fontFamily = fontFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GlassCard(
    corner: Dp,
    borderWidth: Dp,
    alphaOverlay: Float,
    screenWidth: Dp,
    content: @Composable () -> Unit,
) {
    // Responsive padding inside card
    val cardPadding = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 14.dp
        else -> 16.dp
    }

    val shape = RoundedCornerShape(corner)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = Color.Transparent,
        shadowElevation = 0.dp,
        border = BorderStroke(borderWidth, Color(0xFFB50938))
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(Color.Black.copy(alpha = alphaOverlay))
                .padding(cardPadding)
        ) { content() }
    }
}

// Helper function for creating mock ViewModel for previews
@Composable
private fun createMockViewModel(): HomeViewModel {
    val mockContext = androidx.compose.ui.platform.LocalContext.current.applicationContext

    return remember {
        HomeViewModel(
            productRepository = object : com.fashiontothem.ff.domain.repository.ProductRepository {
                suspend fun getProducts(
                    token: String,
                    categoryId: String,
                    categoryLevel: String,
                    page: Int,
                    filters: com.fashiontothem.ff.domain.repository.ProductFilters?,
                    filterOptions: com.fashiontothem.ff.domain.model.FilterOptions?,
                    activeFilters: Map<String, Set<String>>,
                ): Result<com.fashiontothem.ff.domain.repository.ProductPageResult> {
                    return Result.failure(Exception("Not implemented in Preview"))
                }

                override suspend fun getProductsByCategory(
                    token: String,
                    categoryId: String,
                    categoryLevel: String,
                    page: Int,
                    filters: ProductFilters?,
                    filterOptions: FilterOptions?,
                    activeFilters: Map<String, Set<String>>,
                    preferConsolidatedCategories: Boolean,
                ): Result<ProductPageResult> {
                    TODO("Not yet implemented")
                }

                override suspend fun getProductsByVisualSearch(
                    token: String,
                    image: String,
                    page: Int,
                    filters: com.fashiontothem.ff.domain.repository.ProductFilters?,
                    filterOptions: com.fashiontothem.ff.domain.model.FilterOptions?,
                    activeFilters: Map<String, Set<String>>,
                ): Result<com.fashiontothem.ff.domain.repository.ProductPageResult> {
                    return Result.failure(Exception("Not implemented in Preview"))
                }

                override suspend fun getBrandImages(): Result<List<com.fashiontothem.ff.domain.model.BrandImage>> {
                    return Result.success(emptyList())
                }

                override suspend fun getProductDetails(
                    barcodeOrSku: String,
                    isSku: Boolean,
                ): Result<com.fashiontothem.ff.domain.repository.ProductDetailsResult> {
                    return Result.failure(Exception("Not implemented in Preview"))
                }

                override suspend fun addToCart(
                    loyaltyScannedBarcode: String,
                    sku: String,
                    sizeAttributeId: String,
                    sizeOptionValue: String,
                    colorAttributeId: String,
                    colorOptionValue: String,
                ): Result<Boolean> {
                    return Result.success(true)
                }
            },
            categoryPreferences = com.fashiontothem.ff.data.local.preferences.CategoryPreferences(
                mockContext
            )
        )
    }
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun HomeScreenPreviewSmall() {
    HomeScreen(
        viewModel = createMockViewModel()
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun HomeScreenPreviewMedium() {
    HomeScreen(
        viewModel = createMockViewModel()
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun HomeScreenPreviewLarge() {
    HomeScreen(
        viewModel = createMockViewModel()
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun HomeScreenPreviewPhilips() {
    HomeScreen(
        viewModel = createMockViewModel()
    )
}