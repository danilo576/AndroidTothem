package com.fashiontothem.ff.presentation.filter

import android.annotation.SuppressLint
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fashiontothem.ff.data.config.ProductCategories
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.clickableDebounced
import humer.UvcCamera.R

/**
 * F&F Tothem - Gender Selection Screen
 *
 * First step in filter flow - select gender (Women/Men)
 */
@Composable
fun GenderSelectionScreen(
    initialGenderId: String? = null,
    onGenderSelected: (genderId: String) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    var selectedGender by remember { mutableStateOf(initialGenderId) }

    GenderSelectionContent(
        selectedGender = selectedGender,
        onGenderSelect = { gender ->
            selectedGender = gender
            onGenderSelected(gender)
        },
        onBack = onBack,
        onClose = onClose
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun GenderSelectionContent(
    selectedGender: String?,
    onGenderSelect: (String) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        // Responsive top bar padding
        val topBarHorizontalPadding = when {
            screenWidth < 400.dp -> 16.dp
            screenWidth < 600.dp -> 20.dp
            else -> 24.dp
        }
        
        val topBarVerticalPadding = when {
            screenHeight < 700.dp -> 12.dp
            screenHeight < 1200.dp -> 14.dp
            else -> 16.dp
        }
        
        // Responsive logo size
        val logoHeight = when {
            screenHeight < 700.dp -> 36.dp
            screenHeight < 1200.dp -> 44.dp
            else -> 50.dp
        }
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B2B2B))
                    .padding(horizontal = topBarHorizontalPadding, vertical = topBarVerticalPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fashion&Friends logo
                Image(
                    painter = painterResource(id = R.drawable.fashion_logo),
                    contentDescription = "Fashion & Friends",
                    modifier = Modifier.height(logoHeight),
                    contentScale = ContentScale.Fit
                )
            }

            // Responsive spacing after top bar
            val topBarSpacing = when {
                screenHeight < 700.dp -> 20.dp
                screenHeight < 1200.dp -> 30.dp
                else -> 40.dp
            }
            Spacer(modifier = Modifier.height(topBarSpacing))

            // Responsive filter icon size
            val filterIconSize = when {
                screenWidth < 400.dp -> 80.dp
                screenWidth < 600.dp -> 100.dp
                else -> 120.dp
            }

            // Search/Filter Icon
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.search_filter_icon),
                    contentDescription = "Filter",
                    modifier = Modifier.size(filterIconSize),
                    contentScale = ContentScale.Fit
                )
            }

            // Responsive card padding
            val cardPadding = when {
                screenWidth < 400.dp -> 16.dp
                screenWidth < 600.dp -> 30.dp
                else -> 50.dp
            }
            
            // Responsive corner radius
            val cardCornerRadius = when {
                screenWidth < 400.dp -> 24.dp
                screenWidth < 600.dp -> 32.dp
                else -> 40.dp
            }
            
            // Responsive content padding
            val contentPadding = when {
                screenWidth < 400.dp -> 16.dp
                screenWidth < 600.dp -> 20.dp
                else -> 24.dp
            }

            // Filter Card
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(cardPadding),
                    shape = RoundedCornerShape(cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(contentPadding)
                    ) {
                        // Responsive button sizes
                        val buttonSize = when {
                            screenWidth < 400.dp -> 30.dp
                            screenWidth < 600.dp -> 40.dp
                            else -> 64.dp
                        }
                        
                        val iconSize = when {
                            screenWidth < 400.dp -> 20.dp
                            screenWidth < 600.dp -> 24.dp
                            else -> 40.dp
                        }

                        // Header with back and close buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back button
                            Box(
                                modifier = Modifier
                                    .size(buttonSize)
                                    .clickableDebounced { onBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.back_white_icon),
                                    contentDescription = "Back",
                                    modifier = Modifier.size(iconSize),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            // Responsive title font size
                            val titleFontSize = when {
                                screenWidth < 400.dp -> 18.sp
                                screenWidth < 600.dp -> 22.sp
                                else -> 34.sp
                            }

                            // Title
                            Text(
                                text = stringResource(id = R.string.filter_and_find),
                                fontFamily = Fonts.Poppins,
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )

                            // Close button
                            Box(
                                modifier = Modifier
                                    .size(buttonSize)
                                    .clickableDebounced { onClose() },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.x_white_icon),
                                    contentDescription = "Close",
                                    modifier = Modifier.size(iconSize),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        // Responsive spacing after header
                        val headerSpacing = when {
                            screenHeight < 700.dp -> 80.dp
                            screenHeight < 1200.dp -> 150.dp
                            else -> 220.dp
                        }
                        Spacer(modifier = Modifier.height(headerSpacing))

                        // Responsive select text font size
                        val selectTextFontSize = when {
                            screenWidth < 400.dp -> 18.sp
                            screenWidth < 600.dp -> 20.sp
                            else -> 26.sp
                        }

                        // Select Gender Text
                        Text(
                            text = stringResource(id = R.string.select_gender),
                            fontFamily = Fonts.Poppins,
                            fontSize = selectTextFontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF707070),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        // Responsive spacing before buttons
                        val buttonTopSpacing = when {
                            screenHeight < 700.dp -> 30.dp
                            screenHeight < 1200.dp -> 40.dp
                            else -> 50.dp
                        }
                        Spacer(modifier = Modifier.height(buttonTopSpacing))

                        // Responsive button spacing
                        val buttonSpacing = when {
                            screenWidth < 400.dp -> 16.dp
                            screenWidth < 600.dp -> 30.dp
                            else -> 60.dp
                        }

                        // Gender Selection Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                        ) {
                            // Men Button
                            GenderButton(
                                text = stringResource(id = R.string.gender_male),
                                isSelected = selectedGender == ProductCategories.Gender.MEN.categoryId,
                                onClick = { onGenderSelect(ProductCategories.Gender.MEN.categoryId) },
                                modifier = Modifier.weight(1f),
                                screenWidth = screenWidth
                            )

                            // Women Button
                            GenderButton(
                                text = stringResource(id = R.string.gender_female),
                                isSelected = selectedGender == ProductCategories.Gender.WOMEN.categoryId,
                                onClick = { onGenderSelect(ProductCategories.Gender.WOMEN.categoryId) },
                                modifier = Modifier.weight(1f),
                                screenWidth = screenWidth
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenderButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    screenWidth: Dp,
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4F0418),
            Color(0xFFB50938)
        )
    )
    
    // Responsive button height
    val buttonHeight = when {
        screenWidth < 400.dp -> 40.dp
        screenWidth < 600.dp -> 50.dp
        else -> 60.dp
    }
    
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 24.dp
        screenWidth < 600.dp -> 32.dp
        else -> 50.dp
    }
    
    // Responsive font size
    val fontSize = when {
        screenWidth < 400.dp -> 14.sp
        screenWidth < 600.dp -> 16.sp
        else -> 22.sp
    }

    Box(
        modifier = modifier
            .height(buttonHeight)
            .background(
                brush = if (isSelected) gradient else Brush.linearGradient(
                    listOf(
                        Color.White,
                        Color.White
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable { onClick() }
            .then(
                if (!isSelected) {
                    Modifier.background(
                        color = Color.White,
                        shape = RoundedCornerShape(cornerRadius)
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
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .padding(2.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(cornerRadius)
                    )
            )
        }

        Text(
            text = text,
            fontFamily = Fonts.Poppins,
            fontSize = fontSize,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

@Preview(name = "Small Phone (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun GenderSelectionScreenPreviewSmall() {
    GenderSelectionContent(
        selectedGender = ProductCategories.Gender.WOMEN.categoryId,
        onGenderSelect = {},
        onBack = {},
        onClose = {}
    )
}

@Preview(name = "Medium Phone (411x731)", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun GenderSelectionScreenPreviewMedium() {
    GenderSelectionContent(
        selectedGender = ProductCategories.Gender.MEN.categoryId,
        onGenderSelect = {},
        onBack = {},
        onClose = {}
    )
}

@Preview(name = "Large Phone (480x854)", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun GenderSelectionScreenPreviewLarge() {
    GenderSelectionContent(
        selectedGender = ProductCategories.Gender.WOMEN.categoryId,
        onGenderSelect = {},
        onBack = {},
        onClose = {}
    )
}

@Preview(name = "Philips Portrait", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun GenderSelectionScreenPreview() {
    GenderSelectionContent(
        selectedGender = ProductCategories.Gender.WOMEN.categoryId,
        onGenderSelect = {},
        onBack = {},
        onClose = {}
    )
}

