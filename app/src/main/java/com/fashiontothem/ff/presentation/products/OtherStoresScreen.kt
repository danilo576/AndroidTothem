package com.fashiontothem.ff.presentation.products

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fashiontothem.ff.domain.model.OptionAttribute
import com.fashiontothem.ff.domain.model.OptionValue
import com.fashiontothem.ff.domain.model.ProductDetails
import com.fashiontothem.ff.domain.model.ProductDetailsImages
import com.fashiontothem.ff.domain.model.ProductDetailsOptions
import com.fashiontothem.ff.domain.model.ProductDetailsPrices
import com.fashiontothem.ff.domain.model.Store
import com.fashiontothem.ff.domain.model.StoreVariant
import com.fashiontothem.ff.domain.model.SuperAttribute
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.rememberDebouncedClick
import humer.UvcCamera.R
import java.util.Locale

/**
 * Screen for displaying other stores where the product is available
 * Groups stores by city and allows navigation between cities
 */
@Composable
fun OtherStoresScreen(
    uiState: ProductDetailsUiState,
    selectedStoreId: String?, // Currently selected store ID (to exclude from list)
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    val gradient = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF4F0418),
                Color(0xFFB50938)
            )
        )
    }

    // Debounced click handlers to prevent multiple rapid navigations
    val debouncedOnBack = rememberDebouncedClick(onClick = onBack)
    val debouncedOnClose = rememberDebouncedClick(onClick = onClose)

    // Resolve selection for availability check (same logic as ProductAvailabilityScreen)
    val selection = remember(uiState.selectedSize, uiState.selectedColor, uiState.productDetails) {
        resolveAvailabilitySelection(uiState)
    }

    // Group stores by city, excluding the currently selected store
    // Only include stores where product is available (matching selection and qty > 0)
    val storesByCity = remember(uiState.stores, selectedStoreId, selection) {
        val filteredStores = uiState.stores
            .filter { store ->
                // Exclude currently selected store
                !store.id.equals(selectedStoreId, ignoreCase = true)
            }
            .filter { store ->
                // Only include stores with available variants matching the selection
                store.variants.orEmpty().any { variant ->
                    variant.matchesSelection(selection) && variant.qty > 0
                }
            }

        filteredStores
            .groupBy { store -> store.city ?: "Unknown" }
            .toSortedMap()
    }

    val cities = remember(storesByCity) {
        storesByCity.keys.toList()
    }

    var selectedCityIndex by remember(cities) {
        mutableStateOf(0.coerceAtMost(cities.size - 1))
    }

    val selectedCity = remember(cities, selectedCityIndex) {
        cities.getOrNull(selectedCityIndex) ?: ""
    }

    val storesInCity = remember(storesByCity, selectedCity) {
        storesByCity[selectedCity] ?: emptyList()
    }

    val hasPrevious = selectedCityIndex > 0
    val hasNext = selectedCityIndex < cities.size - 1

    // Debounced navigation for chevrons
    val debouncedPrevious = rememberDebouncedClick {
        if (hasPrevious) {
            selectedCityIndex--
        }
    }

    val debouncedNext = rememberDebouncedClick {
        if (hasNext) {
            selectedCityIndex++
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Box(modifier = Modifier.fillMaxSize()) {
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

            // Responsive logo top padding
            val logoTopPadding = when {
                screenHeight < 700.dp -> 10.dp
                screenHeight < 1200.dp -> 10.dp
                else -> 52.dp
            }

            // Responsive logo height
            val logoHeight = when {
                screenWidth < 400.dp -> 15.dp
                screenWidth < 600.dp -> 30.dp
                else -> 120.dp
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = logoTopPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fashion_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(logoHeight),
                    contentScale = ContentScale.Fit
                )
            }

            // Responsive dialog horizontal padding
            val dialogHorizontalPadding = when {
                screenWidth < 400.dp -> 12.dp
                screenWidth < 600.dp -> 20.dp
                else -> 32.dp
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dialogHorizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(initialScale = 0.95f)
                ) {
                    OtherStoresDialog(
                        selectedCity = selectedCity,
                        stores = storesInCity,
                        citiesCount = cities.size,
                        hasPrevious = hasPrevious,
                        hasNext = hasNext,
                        onPrevious = debouncedPrevious,
                        onNext = debouncedNext,
                        gradient = gradient,
                        onBack = debouncedOnBack,
                        onClose = debouncedOnClose,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }
            }
        }
    }
}

@Composable
private fun OtherStoresDialog(
    selectedCity: String,
    stores: List<Store>,
    citiesCount: Int,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    gradient: Brush,
    onBack: () -> Unit,
    onClose: () -> Unit,
    screenWidth: Dp,
    screenHeight: Dp,
) {
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 32.dp
        else -> 40.dp
    }

    // Responsive shadow elevation
    val shadowElevation = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 18.dp
        else -> 24.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = shadowElevation, shape = RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        // Responsive padding
        val horizontalPadding = when {
            screenWidth < 400.dp -> 20.dp
            screenWidth < 600.dp -> 28.dp
            else -> 36.dp
        }

        val topPadding = when {
            screenHeight < 700.dp -> 20.dp
            screenHeight < 1200.dp -> 26.dp
            else -> 32.dp
        }

        val bottomPadding = when {
            screenHeight < 700.dp -> 24.dp
            screenHeight < 1200.dp -> 32.dp
            else -> 40.dp
        }

        // Responsive spacing
        val contentSpacing = when {
            screenHeight < 700.dp -> 12.dp
            screenHeight < 1200.dp -> 16.dp
            else -> 20.dp
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(top = topPadding, bottom = bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            // Header with back and close buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(
                    iconRes = R.drawable.back_white_icon,
                    contentDescription = "Back",
                    gradient = gradient,
                    onClick = onBack,
                    screenWidth = screenWidth
                )
                RoundIconButton(
                    iconRes = R.drawable.x_white_icon,
                    contentDescription = "Close",
                    gradient = gradient,
                    onClick = onClose,
                    screenWidth = screenWidth
                )
            }

            // Central icon (magnifying glass + red star with checkmark)
            // Responsive icon size
            val iconSize = when {
                screenWidth < 400.dp -> 40.dp
                screenWidth < 600.dp -> 60.dp
                else -> 120.dp
            }

            Image(
                painter = painterResource(id = R.drawable.item_available_icon),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Fit
            )

            // Title - different text for single city vs multiple cities
            // Responsive title font size
            val titleFontSize = when {
                screenWidth < 400.dp -> 14.sp
                screenWidth < 600.dp -> 16.sp
                else -> 24.sp
            }

            Text(
                text = if (citiesCount > 1) {
                    stringResource(id = R.string.product_availability_other_stores_title)
                } else {
                    stringResource(id = R.string.product_availability_other_stores_single_city_title)
                },
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = titleFontSize,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // City selector with chevrons (light gray background, rounded ends)
            // Only show chevrons if there are multiple cities
            // Responsive border width
            val borderWidth = when {
                screenWidth < 400.dp -> 0.75.dp
                screenWidth < 600.dp -> 0.875.dp
                else -> 1.dp
            }

            // Responsive city selector padding
            val citySelectorHorizontalPadding = when {
                screenWidth < 400.dp -> 12.dp
                screenWidth < 600.dp -> 14.dp
                else -> 16.dp
            }

            val citySelectorVerticalPadding = when {
                screenHeight < 700.dp -> 8.dp
                screenHeight < 1200.dp -> 10.dp
                else -> 12.dp
            }

            // Responsive corner radius for city selector
            val citySelectorCornerRadius = when {
                screenWidth < 400.dp -> 24.dp
                screenWidth < 600.dp -> 35.dp
                else -> 50.dp
            }

            Box(
                modifier = Modifier
                    .border(
                        border = BorderStroke(borderWidth, Color(0xFFE5E5E5)),
                        shape = CircleShape
                    )
                    .fillMaxWidth()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(citySelectorCornerRadius)
                    )
                    .padding(
                        horizontal = citySelectorHorizontalPadding,
                        vertical = citySelectorVerticalPadding
                    )
            ) {
                // Responsive chevron button size
                val chevronButtonSize = when {
                    screenWidth < 400.dp -> 20.dp
                    screenWidth < 600.dp -> 30.dp
                    else -> 40.dp
                }

                // Responsive chevron icon size
                val chevronIconSize = when {
                    screenWidth < 400.dp -> 15.dp
                    screenWidth < 600.dp -> 20.dp
                    else -> 30.dp
                }

                // Responsive city name font size
                val cityNameFontSize = when {
                    screenWidth < 400.dp -> 14.sp
                    screenWidth < 600.dp -> 16.sp
                    else -> 24.sp
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left chevron - only show if multiple cities
                    if (citiesCount > 1) {
                        IconButton(
                            onClick = onPrevious,
                            enabled = hasPrevious,
                            modifier = Modifier.size(chevronButtonSize)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous city",
                                tint = if (hasPrevious) Color(0xFFB0B0B0) else Color(0xFFE0E0E0),
                                modifier = Modifier.size(chevronIconSize)
                            )
                        }
                    } else {
                        // Spacer to center city name when no chevrons
                        Spacer(modifier = Modifier.size(chevronButtonSize))
                    }

                    // City name
                    Text(
                        text = selectedCity,
                        fontSize = cityNameFontSize,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Fonts.Poppins,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    // Right chevron - only show if multiple cities
                    if (citiesCount > 1) {
                        IconButton(
                            onClick = onNext,
                            enabled = hasNext,
                            modifier = Modifier.size(chevronButtonSize)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next city",
                                tint = if (hasNext) Color(0xFFB0B0B0) else Color(0xFFE0E0E0),
                                modifier = Modifier.size(chevronIconSize)
                            )
                        }
                    } else {
                        // Spacer to center city name when no chevrons
                        Spacer(modifier = Modifier.size(chevronButtonSize))
                    }
                }
            }

            // Stores list
            if (stores.isEmpty()) {
                // Responsive empty state padding
                val emptyStatePadding = when {
                    screenHeight < 700.dp -> 30.dp
                    screenHeight < 1200.dp -> 45.dp
                    else -> 60.dp
                }

                // Responsive empty state font size
                val emptyStateFontSize = when {
                    screenWidth < 400.dp -> 14.sp
                    screenWidth < 600.dp -> 15.sp
                    else -> 16.sp
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(emptyStatePadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_stores_in_city),
                        fontSize = emptyStateFontSize,
                        fontFamily = Fonts.Poppins,
                        color = Color.Black.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Responsive max height for LazyColumn
                val maxListHeight = when {
                    screenHeight < 700.dp -> 350.dp
                    screenHeight < 1200.dp -> 400.dp
                    else -> 600.dp
                }

                // Responsive spacing between store cards
                val storeCardSpacing = when {
                    screenHeight < 700.dp -> 8.dp
                    screenHeight < 1200.dp -> 10.dp
                    else -> 12.dp
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxListHeight), // Limit max height for scrolling
                    verticalArrangement = Arrangement.spacedBy(storeCardSpacing)
                ) {
                    items(stores) { store ->
                        StoreCard(
                            storeName = store.name,
                            storeAddress = store.streetAddress ?: "",
                            screenWidth = screenWidth
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreCard(
    storeName: String,
    storeAddress: String,
    screenWidth: Dp,
) {
    // Responsive corner radius
    val cardCornerRadius = when {
        screenWidth < 400.dp -> 16.dp
        screenWidth < 600.dp -> 22.dp
        else -> 30.dp
    }

    // Responsive border width
    val borderWidth = when {
        screenWidth < 400.dp -> 0.75.dp
        screenWidth < 600.dp -> 0.875.dp
        else -> 1.dp
    }

    // Responsive padding
    val cardPadding = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 12.dp
        else -> 20.dp
    }

    // Responsive spacing between name and address
    val nameAddressSpacing = when {
        screenWidth < 400.dp -> 2.dp
        screenWidth < 600.dp -> 3.dp
        else -> 4.dp
    }

    // Responsive store name font size
    val storeNameFontSize = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 16.sp
        else -> 22.sp
    }

    // Responsive store name line height
    val storeNameLineHeight = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 16.sp
        else -> 24.sp
    }

    // Responsive address font size
    val addressFontSize = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 14.sp
        else -> 18.sp
    }

    // Responsive address line height
    val addressLineHeight = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 16.sp
        else -> 20.sp
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(borderWidth, Color(0xFFE5E5E5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Store Name (bold, black)
            Text(
                text = storeName,
                fontSize = storeNameFontSize,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Fonts.Poppins,
                color = Color.Black,
                lineHeight = storeNameLineHeight
            )

            Spacer(modifier = Modifier.height(nameAddressSpacing))

            // Address (regular, gray)
            Text(
                text = storeAddress,
                fontSize = addressFontSize,
                fontWeight = FontWeight.Normal,
                fontFamily = Fonts.Poppins,
                color = Color(0xFF808080),
                lineHeight = addressLineHeight
            )
        }
    }
}

@Composable
private fun RoundIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    gradient: Brush,
    onClick: () -> Unit,
    screenWidth: Dp,
) {
    // Responsive button size
    val buttonSize = when {
        screenWidth < 400.dp -> 30.dp
        screenWidth < 600.dp -> 40.dp
        else -> 50.dp
    }

    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .background(Color(0xFFB50938), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(buttonSize * 0.6f),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// Helper functions for availability selection (using types from ProductAvailabilityScreen)
private fun resolveAvailabilitySelection(uiState: ProductDetailsUiState): AvailabilitySelection {
    val productDetails = uiState.productDetails
        ?: return AvailabilitySelection(
            AvailabilitySelectionType.None,
            null,
            requiresSelection = false
        )

    val requiresSelection = productDetails.requiresVariantSelection()

    val sizeLabel = productDetails.options?.size?.labelForValue(uiState.selectedSize)
    if (!sizeLabel.isNullOrBlank()) {
        return AvailabilitySelection(AvailabilitySelectionType.Size, sizeLabel, requiresSelection)
    }

    // First, try to find label in original options
    val colorShadeLabel = productDetails.options?.colorShade?.labelForValue(uiState.selectedColor)
    if (!colorShadeLabel.isNullOrBlank()) {
        // Check if this label exists in stores variants (to ensure we use the exact format from stores)
        val shadeFromStores = uiState.stores.flatMap { store ->
            store.variants.orEmpty().mapNotNull { variant ->
                variant.superAttribute?.colorShade ?: variant.shade
            }
        }.firstOrNull { shade ->
            shade.trim().equals(colorShadeLabel, ignoreCase = true)
        }

        if (shadeFromStores != null) {
            return AvailabilitySelection(
                AvailabilitySelectionType.ColorShade,
                shadeFromStores.trim(),
                requiresSelection
            )
        } else {
            // Use label from original options even if not found in stores (for backward compatibility)
            return AvailabilitySelection(
                AvailabilitySelectionType.ColorShade,
                colorShadeLabel,
                requiresSelection
            )
        }
    }

    // If not found in original options, check if selectedColor matches any shade from stores variants
    if (uiState.selectedColor != null) {
        val shadeFromStores = uiState.stores.flatMap { store ->
            store.variants.orEmpty().mapNotNull { variant ->
                variant.superAttribute?.colorShade ?: variant.shade
            }
        }.firstOrNull { shade ->
            shade.trim().equals(uiState.selectedColor, ignoreCase = true)
        }

        if (shadeFromStores != null) {
            return AvailabilitySelection(
                AvailabilitySelectionType.ColorShade,
                shadeFromStores.trim(),
                requiresSelection
            )
        }
    }

    val colorLabel = productDetails.options?.color?.labelForValue(uiState.selectedColor)
    if (!colorLabel.isNullOrBlank()) {
        return AvailabilitySelection(AvailabilitySelectionType.Color, colorLabel, requiresSelection)
    }

    return AvailabilitySelection(AvailabilitySelectionType.None, null, requiresSelection)
}

private fun OptionAttribute.labelForValue(value: String?): String? {
    if (value == null) return null
    return options.firstOrNull { option -> option.value.equals(value, ignoreCase = true) }?.label
}

private fun StoreVariant.matchesSelection(selection: AvailabilitySelection): Boolean {
    if (!selection.requiresSelection) {
        return true
    }

    val selectionLabel = selection.label ?: return false
    val normalizedSelection = selectionLabel.normalizeForCompare()
    return when (selection.type) {
        AvailabilitySelectionType.Size -> {
            val variantSize = (superAttribute?.size ?: size).normalizeForCompare()
            variantSize == normalizedSelection
        }

        AvailabilitySelectionType.Color -> {
            val variantColor = (superAttribute?.color ?: shade).normalizeForCompare()
            variantColor == normalizedSelection
        }

        AvailabilitySelectionType.ColorShade -> {
            val variantShade = (superAttribute?.colorShade ?: shade).normalizeForCompare()
            variantShade == normalizedSelection
        }

        AvailabilitySelectionType.None -> false
    }
}

private fun String?.normalizeForCompare(): String {
    return this?.trim()?.lowercase(Locale.getDefault()) ?: ""
}

// Helper function to create mock UI state for previews
private fun createMockOtherStoresUiState(): ProductDetailsUiState {
    val productDetails = ProductDetails(
        id = "1",
        sku = "SKU123",
        type = "configurable",
        name = "Test proizvod",
        shortDescription = "Opis proizvoda",
        brandName = "Miss Sixty",
        options = ProductDetailsOptions(
            size = OptionAttribute(
                label = "Veličina",
                attributeId = "242",
                options = listOf(
                    OptionValue("S", "5501"),
                    OptionValue("M", "5502"),
                    OptionValue("L", "5503"),
                    OptionValue("XL", "5504")
                )
            ),
            color = null,
            colorShade = null
        ),
        images = ProductDetailsImages(
            baseImg = null,
            imageList = emptyList()
        ),
        prices = ProductDetailsPrices(
            isAdditionalLoyaltyDiscountAllowed = true,
            parentId = null,
            fictional = "23.990,00",
            base = "16.793,00",
            special = "16.793,00",
            loyalty = null,
            id = "1121871"
        )
    )

    // Mock stores in different cities
    val stores = listOf(
        // Beograd stores
        Store(
            id = "2",
            name = "Fashion&Friends Rajićeva SC",
            image = null,
            email = null,
            phoneNumber1 = null,
            phoneNumber2 = null,
            lat = null,
            lng = null,
            fax = null,
            website = null,
            streetAddress = "Knez Mihailova 54",
            country = null,
            zipcode = null,
            description = null,
            tradingHours = null,
            status = null,
            storeCode = "rs_rajiceva",
            brandId = null,
            city = "Beograd",
            imageUrl = null,
            variants = listOf(
                StoreVariant(
                    itemNo = "SKU123-XL",
                    size = "XL",
                    qty = 5,
                    storeName = "Fashion&Friends Rajićeva SC",
                    storeCode = "rs_rajiceva",
                    shade = null,
                    superAttribute = SuperAttribute(
                        size = "XL",
                        color = null,
                        colorShade = null
                    )
                )
            )
        ),
        Store(
            id = "3",
            name = "Fashion&Friends TC Ušće",
            image = null,
            email = null,
            phoneNumber1 = null,
            phoneNumber2 = null,
            lat = null,
            lng = null,
            fax = null,
            website = null,
            streetAddress = "Bulevar Mihaila Pupina 4",
            country = null,
            zipcode = null,
            description = null,
            tradingHours = null,
            status = null,
            storeCode = "rs_usce",
            brandId = null,
            city = "Beograd",
            imageUrl = null,
            variants = listOf(
                StoreVariant(
                    itemNo = "SKU123-XL",
                    size = "XL",
                    qty = 3,
                    storeName = "Fashion&Friends TC Ušće",
                    storeCode = "rs_usce",
                    shade = null,
                    superAttribute = SuperAttribute(
                        size = "XL",
                        color = null,
                        colorShade = null
                    )
                )
            )
        ),
        Store(
            id = "4",
            name = "Fashion&Friends BIG FASHION",
            image = null,
            email = null,
            phoneNumber1 = null,
            phoneNumber2 = null,
            lat = null,
            lng = null,
            fax = null,
            website = null,
            streetAddress = "Višnjička 84",
            country = null,
            zipcode = null,
            description = null,
            tradingHours = null,
            status = null,
            storeCode = "rs_big",
            brandId = null,
            city = "Beograd",
            imageUrl = null,
            variants = listOf(
                StoreVariant(
                    itemNo = "SKU123-XL",
                    size = "XL",
                    qty = 2,
                    storeName = "Fashion&Friends BIG FASHION",
                    storeCode = "rs_big",
                    shade = null,
                    superAttribute = SuperAttribute(
                        size = "XL",
                        color = null,
                        colorShade = null
                    )
                )
            )
        ),
        Store(
            id = "5",
            name = "Fashion & Friends ADA MALL",
            image = null,
            email = null,
            phoneNumber1 = null,
            phoneNumber2 = null,
            lat = null,
            lng = null,
            fax = null,
            website = null,
            streetAddress = "Radnička 9",
            country = null,
            zipcode = null,
            description = null,
            tradingHours = null,
            status = null,
            storeCode = "rs_ada",
            brandId = null,
            city = "Beograd",
            imageUrl = null,
            variants = listOf(
                StoreVariant(
                    itemNo = "SKU123-XL",
                    size = "XL",
                    qty = 4,
                    storeName = "Fashion & Friends ADA MALL",
                    storeCode = "rs_ada",
                    shade = null,
                    superAttribute = SuperAttribute(
                        size = "XL",
                        color = null,
                        colorShade = null
                    )
                )
            )
        ),
        // Novi Sad stores
        Store(
            id = "6",
            name = "Fashion&Friends Promenada",
            image = null,
            email = null,
            phoneNumber1 = null,
            phoneNumber2 = null,
            lat = null,
            lng = null,
            fax = null,
            website = null,
            streetAddress = "Bulevar Oslobođenja 119",
            country = null,
            zipcode = null,
            description = null,
            tradingHours = null,
            status = null,
            storeCode = "rs_promenada",
            brandId = null,
            city = "Novi Sad",
            imageUrl = null,
            variants = listOf(
                StoreVariant(
                    itemNo = "SKU123-XL",
                    size = "XL",
                    qty = 6,
                    storeName = "Fashion&Friends Promenada",
                    storeCode = "rs_promenada",
                    shade = null,
                    superAttribute = SuperAttribute(
                        size = "XL",
                        color = null,
                        colorShade = null
                    )
                )
            )
        )
    )

    return ProductDetailsUiState(
        productDetails = productDetails,
        stores = stores,
        selectedSize = "5504", // XL selected
        selectedColor = null,
        selectedStoreCode = "rs_usce",
        selectedStoreId = "1", // Currently selected store (excluded from list)
        isPickupPointEnabled = true
    )
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun OtherStoresPreviewSmall() {
    OtherStoresScreen(
        uiState = createMockOtherStoresUiState(),
        selectedStoreId = "1", // Currently selected store ID
        onBack = {},
        onClose = {}
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
private fun OtherStoresPreviewMedium() {
    OtherStoresScreen(
        uiState = createMockOtherStoresUiState(),
        selectedStoreId = "1", // Currently selected store ID
        onBack = {},
        onClose = {}
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
private fun OtherStoresPreviewLarge() {
    OtherStoresScreen(
        uiState = createMockOtherStoresUiState(),
        selectedStoreId = "1", // Currently selected store ID
        onBack = {},
        onClose = {}
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
private fun OtherStoresPreviewPhilips() {
    OtherStoresScreen(
        uiState = createMockOtherStoresUiState(),
        selectedStoreId = "1", // Currently selected store ID
        onBack = {},
        onClose = {}
    )
}

@Preview(name = "Other Stores (Legacy)", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
private fun OtherStoresPreview() {
    OtherStoresScreen(
        uiState = createMockOtherStoresUiState(),
        selectedStoreId = "1", // Currently selected store ID
        onBack = {},
        onClose = {}
    )
}

