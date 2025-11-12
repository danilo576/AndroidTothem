package com.fashiontothem.ff.presentation.products

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
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
import humer.UvcCamera.R
import java.util.Locale

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

    val selection = remember(uiState.selectedSize, uiState.selectedColor, uiState.productDetails) {
        resolveAvailabilitySelection(uiState)
    }

    val selectedStore = remember(uiState.selectedStoreCode, uiState.stores) {
        uiState.selectedStoreCode?.let { code ->
            uiState.stores.firstOrNull { store ->
                store.storeCode.equals(code, ignoreCase = true)
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

    LaunchedEffect(selection.type) {
        if (selection.type == AvailabilitySelectionType.None) {
            onClose()
        }
    }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.fashion_logo),
                contentDescription = null
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = selection.type != AvailabilitySelectionType.None,
                enter = fadeIn() + scaleIn(initialScale = 0.95f)
            ) {
                AvailabilityDialog(
                    selection = selection,
                    selectedStore = selectedStore,
                    isAvailableInSelectedStore = isAvailableInSelectedStore,
                    otherStoresCount = otherStoresWithAvailability,
                    gradient = gradient,
                    onBack = onBack,
                    onClose = onClose,
                    onDeliverToPickupPoint = onDeliverToPickupPoint,
                    onOrderOnline = onOrderOnline,
                    onViewMoreStores = onViewMoreStores,
                )
            }
        }
    }
}

@Composable
private fun AvailabilityDialog(
    selection: AvailabilitySelection,
    selectedStore: Store?,
    isAvailableInSelectedStore: Boolean,
    otherStoresCount: Int,
    gradient: Brush,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onDeliverToPickupPoint: () -> Unit,
    onOrderOnline: () -> Unit,
    onViewMoreStores: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 24.dp, shape = RoundedCornerShape(40.dp)),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
                .padding(top = 32.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
                    onClick = onBack
                )
                RoundIconButton(
                    iconRes = R.drawable.x_white_icon,
                    contentDescription = stringResource(id = R.string.product_details_close),
                    gradient = gradient,
                    onClick = onClose
                )
            }

            Image(
                painter = painterResource(id = R.drawable.item_available_icon),
                contentDescription = null
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.product_availability_title),
                    fontFamily = Fonts.Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 34.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                val selectionLabel = selection.label
                if (!selectionLabel.isNullOrEmpty()) {
                    val selectionText = when (selection.type) {
                        AvailabilitySelectionType.Size -> stringResource(
                            id = R.string.product_availability_selection_size,
                            selectionLabel
                        )

                        AvailabilitySelectionType.Color -> stringResource(
                            id = R.string.product_availability_selection_color,
                            selectionLabel
                        )

                        AvailabilitySelectionType.ColorShade -> stringResource(
                            id = R.string.product_availability_selection_shade,
                            selectionLabel
                        )

                        AvailabilitySelectionType.None -> null
                    }
                    selectionText?.let {
                        Text(
                            text = it,
                            fontFamily = Fonts.Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp,
                            color = Color(0xFF5E5E5E),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                selectedStore?.name?.takeIf { it.isNotBlank() }?.let { storeName ->
                    Text(
                        text = storeName,
                        fontFamily = Fonts.Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        color = Color(0xFF8C8C8C),
                        textAlign = TextAlign.Center
                    )
                }
            }

            PrimaryGradientButton(
                text = stringResource(id = R.string.product_availability_deliver_pickup),
                gradient = gradient,
                onClick = onDeliverToPickupPoint,
                enabled = isAvailableInSelectedStore
            )

            OutlineActionButton(
                text = stringResource(id = R.string.product_availability_order_online),
                iconRes = R.drawable.fashion_and_friends_loader,
                enabled = true,
                onClick = onOrderOnline
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val otherStoresText = if (otherStoresCount > 0) {
                    stringResource(
                        id = R.string.product_availability_other_stores_with_count,
                        otherStoresCount
                    )
                } else {
                    stringResource(id = R.string.product_availability_other_stores)
                }

                Text(
                    text = otherStoresText,
                    fontFamily = Fonts.Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    color = Color(0xFFB0B0B0),
                    textAlign = TextAlign.Center
                )

                OutlineActionButton(
                    text = stringResource(id = R.string.product_availability_view_more),
                    enabled = otherStoresCount > 0,
                    onClick = onViewMoreStores
                )
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
) {
    // Close button
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color(0xFFB50938), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(R.string.product_details_close),
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
) {
    val disabledGradient = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFB5B5B5),
                Color(0xFF9F9F9F)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(if (enabled) gradient else disabledGradient)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Fonts.Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
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
) {
    val shape = RoundedCornerShape(50.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(shape)
            .background(Color.White)
            .border(
                width = 2.dp,
                color = Color(0xFFE5E5E5),
                shape = shape
            )
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = text,
                fontFamily = Fonts.Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Color(0xFF2C2C2C)
            )
        }
    }
}

private enum class AvailabilitySelectionType {
    Size,
    Color,
    ColorShade,
    None
}

private data class AvailabilitySelection(
    val type: AvailabilitySelectionType,
    val label: String?,
)

private fun resolveAvailabilitySelection(uiState: ProductDetailsUiState): AvailabilitySelection {
    val productDetails =
        uiState.productDetails ?: return AvailabilitySelection(AvailabilitySelectionType.None, null)

    val sizeLabel = productDetails.options?.size?.labelForValue(uiState.selectedSize)
    if (!sizeLabel.isNullOrBlank()) {
        return AvailabilitySelection(AvailabilitySelectionType.Size, sizeLabel)
    }

    val colorShadeLabel = productDetails.options?.colorShade?.labelForValue(uiState.selectedColor)
    if (!colorShadeLabel.isNullOrBlank()) {
        return AvailabilitySelection(AvailabilitySelectionType.ColorShade, colorShadeLabel)
    }

    val colorLabel = productDetails.options?.color?.labelForValue(uiState.selectedColor)
    if (!colorLabel.isNullOrBlank()) {
        return AvailabilitySelection(AvailabilitySelectionType.Color, colorLabel)
    }

    return AvailabilitySelection(AvailabilitySelectionType.None, null)
}

private fun OptionAttribute.labelForValue(value: String?): String? {
    if (value == null) return null
    return options.firstOrNull { option -> option.value.equals(value, ignoreCase = true) }?.label
}

private fun StoreVariant.matchesSelection(selection: AvailabilitySelection): Boolean {
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

@Preview(name = "Product Availability", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
private fun ProductAvailabilityPreview() {
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

    ProductAvailabilityScreen(
        uiState = ProductDetailsUiState(
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
            selectedStoreCode = "rs_usce"
        ),
        onBack = {},
        onClose = {},
        onDeliverToPickupPoint = {},
        onOrderOnline = {},
        onViewMoreStores = {}
    )
}


