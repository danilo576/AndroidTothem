package com.fashiontothem.ff.presentation.filter

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.clickableDebounced
import humer.UvcCamera.R

/**
 * Filter type for brand or category selection
 */
enum class FilterType {
    BRAND,
    CATEGORY
}

/**
 * F&F Tothem - Brand or Category Selection Screen
 *
 * Second step in filter flow - select whether to filter by Brand or Category
 */
@Composable
fun BrandOrCategorySelectionScreen(
    genderId: String,
    onFilterTypeSelected: (filterType: FilterType) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    var selectedFilterType by remember { mutableStateOf<FilterType?>(null) }

    BrandOrCategoryContent(
        selectedFilterType = selectedFilterType,
        onFilterTypeSelect = { filterType ->
            selectedFilterType = filterType
            onFilterTypeSelected(filterType)
        },
        onBack = onBack,
        onClose = onClose
    )
}

@Composable
private fun BrandOrCategoryContent(
    selectedFilterType: FilterType?,
    onFilterTypeSelect: (FilterType) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2B2B2B))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fashion&Friends logo
            Image(
                painter = painterResource(id = R.drawable.fashion_logo),
                contentDescription = "Fashion & Friends",
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Search/Filter Icon
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.search_filter_icon),
                contentDescription = "Filter"
            )
        }

        // Filter Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(50.dp),
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Header with back and close buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clickableDebounced { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.back_white_icon),
                                contentDescription = "Back",
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Title
                        Text(
                            text = stringResource(id = R.string.filter_and_find),
                            fontFamily = Fonts.Poppins,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        // Close button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clickableDebounced { onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.x_white_icon),
                                contentDescription = "Close",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(220.dp))

                    // Select Brand or Category Text
                    Text(
                        text = stringResource(id = R.string.select_brand_or_category),
                        fontFamily = Fonts.Poppins,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF707070),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(50.dp))

                    // Filter Type Selection Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(60.dp)
                    ) {
                        // Brand Button
                        FilterTypeButton(
                            text = stringResource(id = R.string.brand),
                            isSelected = selectedFilterType == FilterType.BRAND,
                            onClick = { onFilterTypeSelect(FilterType.BRAND) },
                            modifier = Modifier.weight(1f)
                        )

                        // Category Button
                        FilterTypeButton(
                            text = stringResource(id = R.string.category),
                            isSelected = selectedFilterType == FilterType.CATEGORY,
                            onClick = { onFilterTypeSelect(FilterType.CATEGORY) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4F0418),
            Color(0xFFB50938)
        )
    )

    Box(
        modifier = modifier
            .height(60.dp)
            .background(
                brush = if (isSelected) gradient else Brush.linearGradient(
                    listOf(
                        Color.White,
                        Color.White
                    )
                ),
                shape = RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
            .then(
                if (!isSelected) {
                    Modifier.background(
                        color = Color.White,
                        shape = RoundedCornerShape(50.dp)
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Border for unselected
        if (!isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = Color(0xFFEBEBEB),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(2.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(50.dp)
                    )
            )
        }

        Text(
            text = text,
            fontFamily = Fonts.Poppins,
            fontSize = 22.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun BrandOrCategorySelectionScreenPreview() {
    BrandOrCategoryContent(
        selectedFilterType = FilterType.CATEGORY,
        onFilterTypeSelect = {},
        onBack = {},
        onClose = {}
    )
}

