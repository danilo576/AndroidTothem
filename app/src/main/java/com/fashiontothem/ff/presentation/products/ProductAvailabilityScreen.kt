package com.fashiontothem.ff.presentation.products

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProductAvailabilityScreen(
    uiState: ProductDetailsUiState,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onDeliverToPickupPoint: () -> Unit,
    onOrderOnline: () -> Unit,
    onViewMoreStores: () -> Unit,
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

    // State for showing option no longer available dialog
    var showOptionNoLongerAvailableDialog by remember { mutableStateOf(false) }
    
    val selection = remember(uiState.selectedSize, uiState.selectedColor, uiState.productDetails) {
        resolveAvailabilitySelection(uiState)
    }
    
    // Create a stable key for current selection
    val currentSelectionKey = remember(uiState.selectedSize, uiState.selectedColor) {
        "${uiState.selectedSize}_${uiState.selectedColor}"
    }
    
    // Track if dialog has been shown for current selection - reset when selection changes
    var hasShownDialogForSelection by remember(currentSelectionKey) {
        mutableStateOf(false)
    }

    // Use selectedStoreId from LocationPreferences (saved in StoreLocationsScreen)
    // This is the store selected for pickup point delivery
    val selectedStore = remember(uiState.selectedStoreId, uiState.stores) {
        uiState.selectedStoreId?.let { storeId ->
            uiState.stores.firstOrNull { store ->
                store.id.equals(storeId, ignoreCase = true)
            }
        }
    }

    val isAvailableInSelectedStore = remember(selection, selectedStore) {
        selectedStore?.variants.orEmpty().any { variant ->
            variant.matchesSelection(selection) && variant.qty > 0
        }
    }

    val otherStoresWithAvailability =
        remember(selection, uiState.stores, selectedStore?.storeCode) {
            uiState.stores
                .filter { store -> store.storeCode != selectedStore?.storeCode }
                .count { store ->
                    store.variants.orEmpty().any { variant ->
                        variant.matchesSelection(selection) && variant.qty > 0
                    }
                }
        }

    // Show pickup availability only if:
    // 1. Pickup point is enabled
    // 2. Selected store (from LocationPreferences) is found in stores list
    // 3. Product is available in that store
    val showPickupAvailability = remember(
        uiState.isPickupPointEnabled,
        selectedStore,
        isAvailableInSelectedStore
    ) {
        uiState.isPickupPointEnabled &&
                selectedStore != null &&
                isAvailableInSelectedStore
    }

    // Check if product is retail-only OR if selected variant is retail-only
    val isRetailOnly =
        remember(uiState.productDetails, uiState.selectedSize, uiState.selectedColor) {
            val productRetailOnly = uiState.productDetails?.isRetailOnly == true
            if (productRetailOnly) return@remember true

            // Check if selected variant is available only in retail stores (not online)
            // A variant is retail-only if it's only available in stores and not in the original product options
            val productDetails = uiState.productDetails

            // Check if selected size is in original options
            val selectedSize = uiState.selectedSize
            if (selectedSize != null && productDetails?.options?.size != null) {
                val isInMainOptions = productDetails.options.size.options.any { option ->
                    option.value.equals(selectedSize, ignoreCase = true) ||
                            option.label.equals(selectedSize, ignoreCase = true)
                }
                // If selected size is NOT in main options, it's retail-only (added from stores variants)
                if (!isInMainOptions) {
                    return@remember true
                }
            }

            // Check if selected color/shade is in original options
            val selectedColor = uiState.selectedColor
            if (selectedColor != null) {
                val isInMainOptions = productDetails?.options?.colorShade?.options?.any { option ->
                    option.value.equals(selectedColor, ignoreCase = true) ||
                            option.label.equals(selectedColor, ignoreCase = true)
                } == true ||
                        productDetails?.options?.color?.options?.any { option ->
                            option.value.equals(selectedColor, ignoreCase = true) ||
                                    option.label.equals(selectedColor, ignoreCase = true)
                        } == true

                // If selected color/shade is NOT in main options, it's retail-only
                if (!isInMainOptions) {
                    return@remember true
                }
            }

            false
        }

    LaunchedEffect(selection.type, selection.requiresSelection) {
        if (selection.requiresSelection && selection.type == AvailabilitySelectionType.None) {
            onClose()
        }
    }

    // Show dialog when option is available in selected store but pickup point is disabled
    // Only show once per selection - use currentSelectionKey as key so it executes only once per selection
    LaunchedEffect(currentSelectionKey) {
        // Wait a bit for all computed values to be ready
        kotlinx.coroutines.delay(50)
        // Only show if conditions are met AND dialog hasn't been shown for this selection yet
        if (isAvailableInSelectedStore && 
            selectedStore != null && 
            !showPickupAvailability && 
            !hasShownDialogForSelection) {
            hasShownDialogForSelection = true
            showOptionNoLongerAvailableDialog = true
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
                    visible = !selection.requiresSelection || selection.type != AvailabilitySelectionType.None,
                    enter = fadeIn() + scaleIn(initialScale = 0.95f)
                ) {
                    AvailabilityDialog(
                        selection = selection,
                        selectedStore = selectedStore,
                        isAvailableInSelectedStore = isAvailableInSelectedStore,
                        otherStoresCount = otherStoresWithAvailability,
                        showPickupAvailability = showPickupAvailability,
                        isRetailOnly = isRetailOnly,
                        gradient = gradient,
                        onBack = debouncedOnBack,
                        onClose = debouncedOnClose,
                        onDeliverToPickupPoint = onDeliverToPickupPoint,
                        onOrderOnline = onOrderOnline,
                        onViewMoreStores = onViewMoreStores,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight
                    )
                }
            }
        }
    }

    // Show option no longer available dialog
    if (showOptionNoLongerAvailableDialog) {
        OptionNoLongerAvailableDialog(
            gradient = gradient,
            onDismiss = { 
                showOptionNoLongerAvailableDialog = false
                // Dialog has been dismissed, don't show it again for this selection
            }
        )
    }
}

@Composable
private fun AvailabilityDialog(
    selection: AvailabilitySelection,
    selectedStore: Store?,
    isAvailableInSelectedStore: Boolean,
    otherStoresCount: Int,
    showPickupAvailability: Boolean,
    isRetailOnly: Boolean,
    gradient: Brush,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onDeliverToPickupPoint: () -> Unit,
    onOrderOnline: () -> Unit,
    onViewMoreStores: () -> Unit,
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
        screenHeight < 1200.dp -> 18.dp
        else -> 24.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = shadowElevation, shape = RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(top = topPadding, bottom = bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(
                    iconRes = R.drawable.back_white_icon,
                    contentDescription = stringResource(id = R.string.product_details_back_button),
                    gradient = gradient,
                    onClick = onBack,
                    screenWidth = screenWidth
                )
                RoundIconButton(
                    iconRes = R.drawable.x_white_icon,
                    contentDescription = stringResource(id = R.string.product_details_close),
                    gradient = gradient,
                    onClick = onClose,
                    screenWidth = screenWidth
                )
            }

            // Responsive icon size
            val iconSize = when {
                screenWidth < 400.dp -> 50.dp
                screenWidth < 600.dp -> 70.dp
                else -> 120.dp
            }

            Image(
                painter = painterResource(id = R.drawable.item_available_icon),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Fit
            )

            // Responsive text spacing
            val textSpacing = when {
                screenHeight < 700.dp -> 6.dp
                screenHeight < 1200.dp -> 7.dp
                else -> 8.dp
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(textSpacing)
            ) {
                if (showPickupAvailability) {
                    // Responsive title font size
                    val titleFontSize = when {
                        screenWidth < 400.dp -> 16.sp
                        screenWidth < 600.dp -> 22.sp
                        else -> 34.sp
                    }

                    Text(
                        text = stringResource(id = R.string.product_availability_title),
                        fontFamily = Fonts.Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = titleFontSize,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                // Prikaži labelu samo ako proizvod zahteva selekciju (configurable sa size/shade)
                // Za simple proizvode ne prikazujemo labelu
                val selectionLabel = selection.label
                if (!selectionLabel.isNullOrEmpty() && selection.requiresSelection) {
                    val selectionText = when (selection.type) {
                        AvailabilitySelectionType.Size -> stringResource(
                            id = R.string.product_availability_selection_size,
                            selectionLabel
                        )

                        AvailabilitySelectionType.ColorShade -> stringResource(
                            id = R.string.product_availability_selection_shade,
                            selectionLabel
                        )

                        AvailabilitySelectionType.Color -> null // Color se ne prikazuje
                        AvailabilitySelectionType.None -> null
                    }
                    selectionText?.let {
                        // Responsive selection text font size
                        val selectionFontSize = when {
                            screenWidth < 400.dp -> 12.sp
                            screenWidth < 600.dp -> 16.sp
                            else -> 22.sp
                        }

                        Text(
                            text = it,
                            fontFamily = Fonts.Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = selectionFontSize,
                            color = Color(0xFF5E5E5E),
                            textAlign = TextAlign.Center
                        )
                    }
                }

            }

            if (showPickupAvailability) {
                PrimaryGradientButton(
                    text = stringResource(id = R.string.product_availability_deliver_pickup),
                    gradient = gradient,
                    onClick = onDeliverToPickupPoint,
                    enabled = isAvailableInSelectedStore,
                    screenWidth = screenWidth
                )
            }

            // Hide "Order Online" button for retail-only products
            if (!isRetailOnly) {
                OutlineActionButton(
                    text = stringResource(id = R.string.product_availability_order_online),
                    iconRes = R.drawable.fashion_and_friends_loader,
                    enabled = true,
                    onClick = onOrderOnline,
                    screenWidth = screenWidth
                )
            }

            // Prikaži sekciju "Dostupno u drugim radnjama" samo ako je proizvod dostupan u drugim prodavnicama
            if (otherStoresCount > 0) {
                // Responsive spacing
                val otherStoresSpacing = when {
                    screenHeight < 700.dp -> 12.dp
                    screenHeight < 1200.dp -> 14.dp
                    else -> 16.dp
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(otherStoresSpacing)
                ) {
                    val otherStoresText = stringResource(
                        id = R.string.product_availability_other_stores_with_count,
                        otherStoresCount
                    )

                    // Responsive other stores text font size
                    val otherStoresFontSize = when {
                        screenWidth < 400.dp -> 12.sp
                        screenWidth < 600.dp -> 16.sp
                        else -> 20.sp
                    }

                    Text(
                        text = otherStoresText,
                        fontFamily = Fonts.Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = otherStoresFontSize,
                        color = Color(0xFFB0B0B0),
                        textAlign = TextAlign.Center
                    )

                    OutlineActionButton(
                        text = stringResource(id = R.string.product_availability_view_more),
                        enabled = true,
                        onClick = onViewMoreStores,
                        screenWidth = screenWidth
                    )
                }
            }
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
        screenWidth < 400.dp -> 20.dp
        screenWidth < 600.dp -> 30.dp
        else -> 50.dp
    }

    // Close button
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .background(Color(0xFFB50938), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(R.string.product_details_close),
                modifier = Modifier.size(buttonSize * 0.6f),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun PrimaryGradientButton(
    text: String,
    gradient: Brush,
    enabled: Boolean,
    onClick: () -> Unit,
    screenWidth: Dp,
) {
    val disabledGradient = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFB5B5B5),
                Color(0xFF9F9F9F)
            )
        )
    }

    // Responsive button height
    val buttonHeight = when {
        screenWidth < 400.dp -> 40.dp
        screenWidth < 600.dp -> 50.dp
        else -> 70.dp
    }

    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 35.dp
        else -> 50.dp
    }

    // Responsive font size
    val buttonFontSize = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 16.sp
        else -> 22.sp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (enabled) gradient else disabledGradient)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Fonts.Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = buttonFontSize,
            color = if (enabled) Color.White else Color(0xFF5A5A5A)
        )
    }
}

@Composable
private fun OutlineActionButton(
    text: String,
    @DrawableRes iconRes: Int? = null,
    enabled: Boolean,
    onClick: () -> Unit,
    screenWidth: Dp,
) {
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 35.dp
        else -> 50.dp
    }

    val shape = RoundedCornerShape(cornerRadius)

    // Responsive button height
    val buttonHeight = when {
        screenWidth < 400.dp -> 40.dp
        screenWidth < 600.dp -> 50.dp
        else -> 66.dp
    }

    // Responsive border width
    val borderWidth = when {
        screenWidth < 400.dp -> 1.5.dp
        screenWidth < 600.dp -> 1.75.dp
        else -> 2.dp
    }

    // Responsive font size
    val buttonFontSize = when {
        screenWidth < 400.dp -> 10.sp
        screenWidth < 600.dp -> 16.sp
        else -> 22.sp
    }

    // Responsive icon size
    val iconSize = when {
        screenWidth < 400.dp -> 16.dp
        screenWidth < 600.dp -> 20.dp
        else -> 28.dp
    }

    // Responsive spacing
    val iconTextSpacing = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 10.dp
        else -> 12.dp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .clip(shape)
            .background(Color.White)
            .border(
                width = borderWidth,
                color = Color(0xFFE5E5E5),
                shape = shape
            )
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(iconTextSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = text,
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = buttonFontSize,
                color = Color(0xFF2C2C2C)
            )
        }
    }
}

enum class AvailabilitySelectionType {
    Size,
    Color,
    ColorShade,
    None
}

data class AvailabilitySelection(
    val type: AvailabilitySelectionType,
    val label: String?,
    val requiresSelection: Boolean,
)

private fun resolveAvailabilitySelection(uiState: ProductDetailsUiState): AvailabilitySelection {
    val productDetails = uiState.productDetails
        ?: return AvailabilitySelection(
            AvailabilitySelectionType.None,
            null,
            requiresSelection = false
        )

    val requiresSelection = productDetails.requiresVariantSelection()

    // Check size selection - first try to find in original options
    val sizeLabel = productDetails.options?.size?.labelForValue(uiState.selectedSize)
    if (!sizeLabel.isNullOrBlank()) {
        return AvailabilitySelection(AvailabilitySelectionType.Size, sizeLabel, requiresSelection)
    }

    // If not found in original options, check if selectedSize matches any size from stores variants
    // This handles retail-only sizes like "XS" that are added from stores variants
    if (uiState.selectedSize != null) {
        val sizeFromStores = uiState.stores.flatMap { store ->
            store.variants.orEmpty().mapNotNull { variant ->
                variant.superAttribute?.size ?: variant.size
            }
        }.firstOrNull { it.trim().equals(uiState.selectedSize, ignoreCase = true) }

        if (sizeFromStores != null) {
            return AvailabilitySelection(
                AvailabilitySelectionType.Size,
                sizeFromStores.trim(),
                requiresSelection
            )
        }
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
    // This handles retail-only shades that are added from stores variants
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

        // Also check for color (not shade)
        val colorFromStores = uiState.stores.flatMap { store ->
            store.variants.orEmpty().mapNotNull { variant ->
                variant.superAttribute?.color
            }
        }.firstOrNull { it.trim().equals(uiState.selectedColor, ignoreCase = true) }

        if (colorFromStores != null) {
            return AvailabilitySelection(
                AvailabilitySelectionType.Color,
                colorFromStores.trim(),
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

    val result = when (selection.type) {
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

    return result
}

private fun String?.normalizeForCompare(): String {
    return this?.trim()?.lowercase(Locale.getDefault()) ?: ""
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun OptionNoLongerAvailableDialog(
    gradient: Brush,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                // Responsive corner radius
                val cornerRadius = when {
                    screenWidth < 400.dp -> 24.dp
                    screenWidth < 600.dp -> 32.dp
                    else -> 40.dp
                }

                // Responsive shadow elevation
                val shadowElevation = when {
                    screenWidth < 400.dp -> 10.dp
                    screenWidth < 600.dp -> 16.dp
                    else -> 24.dp
                }

                // Responsive horizontal padding
                val cardHorizontalPadding = when {
                    screenWidth < 400.dp -> 16.dp
                    screenWidth < 600.dp -> 24.dp
                    else -> 32.dp
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = cardHorizontalPadding)
                        .shadow(
                            elevation = shadowElevation,
                            shape = RoundedCornerShape(cornerRadius)
                        )
                        .clickable { /* Prevent dialog close when clicking on card */ },
                    shape = RoundedCornerShape(cornerRadius),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    // Responsive content padding
                    val contentHorizontalPadding = when {
                        screenWidth < 400.dp -> 20.dp
                        screenWidth < 600.dp -> 28.dp
                        else -> 36.dp
                    }

                    val contentTopPadding = when {
                        screenHeight < 700.dp -> 20.dp
                        screenHeight < 1200.dp -> 26.dp
                        else -> 32.dp
                    }

                    val contentBottomPadding = when {
                        screenHeight < 700.dp -> 24.dp
                        screenHeight < 1200.dp -> 32.dp
                        else -> 40.dp
                    }

                    // Responsive spacing
                    val contentSpacing = when {
                        screenHeight < 700.dp -> 16.dp
                        screenHeight < 1200.dp -> 20.dp
                        else -> 24.dp
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentHorizontalPadding)
                            .padding(top = contentTopPadding, bottom = contentBottomPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(contentSpacing)
                    ) {
                        // Header with close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Responsive spacer size
                            val spacerSize = when {
                                screenWidth < 400.dp -> 36.dp
                                screenWidth < 600.dp -> 42.dp
                                else -> 50.dp
                            }

                            Spacer(modifier = Modifier.size(spacerSize)) // Spacer to balance the close button
                            RoundIconButton(
                                iconRes = R.drawable.x_white_icon,
                                contentDescription = stringResource(id = R.string.product_details_close),
                                gradient = gradient,
                                onClick = onDismiss,
                                screenWidth = screenWidth
                            )
                        }

                        // Title
                        // Responsive title font size
                        val titleFontSize = when {
                            screenWidth < 400.dp -> 10.sp
                            screenWidth < 600.dp -> 16.sp
                            else -> 28.sp
                        }

                        Text(
                            text = stringResource(id = R.string.product_availability_option_no_longer_available),
                            fontFamily = Fonts.Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = titleFontSize,
                            color = Color(0xFFB50938),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // OK button
                        // Responsive button height
                        val buttonHeight = when {
                            screenWidth < 400.dp -> 40.dp
                            screenWidth < 600.dp -> 50.dp
                            else -> 66.dp
                        }

                        // Responsive button corner radius
                        val buttonCornerRadius = when {
                            screenWidth < 400.dp -> 24.dp
                            screenWidth < 600.dp -> 35.dp
                            else -> 50.dp
                        }

                        // Responsive button font size
                        val buttonFontSize = when {
                            screenWidth < 400.dp -> 12.sp
                            screenWidth < 600.dp -> 16.sp
                            else -> 22.sp
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight)
                                .clip(RoundedCornerShape(buttonCornerRadius))
                                .background(gradient)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.invalid_loyalty_card_ok),
                                fontFamily = Fonts.Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = buttonFontSize,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function to create mock UI state for previews
private fun createMockUiState(): ProductDetailsUiState {
    val gradientOptions = ProductDetailsOptions(
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
    )

    val productDetails = ProductDetails(
        id = "1",
        sku = "SKU123",
        type = "configurable",
        name = "Test proizvod",
        shortDescription = "Opis proizvoda",
        brandName = "Miss Sixty",
        options = gradientOptions,
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

    val store = Store(
        id = "1",
        name = "Fashion & Friends Ušće",
        image = null,
        email = null,
        phoneNumber1 = null,
        phoneNumber2 = null,
        lat = null,
        lng = null,
        fax = null,
        website = null,
        streetAddress = null,
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
                itemNo = "SKU123-S",
                size = "S",
                qty = 5,
                storeName = "Fashion & Friends Ušće",
                storeCode = "rs_usce",
                shade = null,
                superAttribute = SuperAttribute(
                    size = "XL",
                    color = null,
                    colorShade = null
                )
            )
        )
    )

    return ProductDetailsUiState(
        productDetails = productDetails,
        stores = listOf(
            store,
            store.copy(
                id = "2",
                name = "Fashion & Friends Delta City",
                storeCode = "rs_delta",
                variants = listOf(
                    StoreVariant(
                        itemNo = "SKU123-M",
                        size = "M",
                        qty = 3,
                        storeName = "Fashion & Friends Delta City",
                        storeCode = "rs_delta",
                        shade = null,
                        superAttribute = SuperAttribute(
                            size = "XL",
                            color = null,
                            colorShade = null
                        )
                    )
                )
            )
        ),
        selectedSize = "5504",
        selectedColor = null,
        selectedStoreCode = "rs_usce",
        selectedStoreId = "1", // Store ID from LocationPreferences
        isPickupPointEnabled = true
    )
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun ProductAvailabilityPreviewSmall() {
    ProductAvailabilityScreen(
        uiState = createMockUiState(),
        onBack = {},
        onClose = {},
        onDeliverToPickupPoint = {},
        onOrderOnline = {},
        onViewMoreStores = {}
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
private fun ProductAvailabilityPreviewMedium() {
    ProductAvailabilityScreen(
        uiState = createMockUiState(),
        onBack = {},
        onClose = {},
        onDeliverToPickupPoint = {},
        onOrderOnline = {},
        onViewMoreStores = {}
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
private fun ProductAvailabilityPreviewLarge() {
    ProductAvailabilityScreen(
        uiState = createMockUiState(),
        onBack = {},
        onClose = {},
        onDeliverToPickupPoint = {},
        onOrderOnline = {},
        onViewMoreStores = {}
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
private fun ProductAvailabilityPreviewPhilips() {
    ProductAvailabilityScreen(
        uiState = createMockUiState(),
        onBack = {},
        onClose = {},
        onDeliverToPickupPoint = {},
        onOrderOnline = {},
        onViewMoreStores = {}
    )
}


