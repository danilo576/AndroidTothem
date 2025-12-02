@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.fashiontothem.ff.presentation.filter

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
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
import androidx.core.graphics.toColorInt
import coil.compose.rememberAsyncImagePainter
import com.fashiontothem.ff.presentation.common.FashionLoader
import com.fashiontothem.ff.presentation.products.FashionTopBar
import com.fashiontothem.ff.ui.theme.Fonts
import com.fashiontothem.ff.util.clickableDebounced
import humer.UvcCamera.R

/**
 * Filter tabs for product filtering
 */
enum class FilterTab {
    POL,
    KATEGORIJA,
    BREND,
    VELICINA,
    BOJA
}

/**
 * F&F Tothem - Product Filters Screen
 *
 * Comprehensive filter screen with 4 tabs: Category, Brand, Size, Color
 */
@Composable
fun ProductFiltersScreen(
    availableFilters: com.fashiontothem.ff.domain.model.FilterOptions? = null,
    activeFilters: Map<String, Set<String>> = emptyMap(),
    isLoadingFilters: Boolean = false,
    fromHome: Boolean = false,
    filterType: String? = null, // "brand" or "category"
    initialTab: FilterTab? = null, // Remember last active tab
    onApplyFilters: (
        selectedGenders: Set<String>,
        selectedCategories: Set<String>,
        selectedBrands: Set<String>,
        selectedSizes: Set<String>,
        selectedColors: Set<String>,
    ) -> Unit,
    onClose: () -> Unit,
    onNavigateToHome: () -> Unit = {}, // Navigate to home and clear stack
    onTabChanged: (FilterTab) -> Unit = {}, // Track tab changes for remembering
) {
    // Determine tab order based on navigation source
    val baseTabOrder = remember(fromHome, filterType) {
        when {
            fromHome || filterType == "visual" -> {
                // From Home or Visual Search: Pol → Kategorija → Brend → Veličina → Boja
                listOf(
                    FilterTab.POL,
                    FilterTab.KATEGORIJA,
                    FilterTab.BREND,
                    FilterTab.VELICINA,
                    FilterTab.BOJA
                )
            }

            filterType == "brand" -> {
                // From Brand Selection: Brend → Kategorija → Veličina → Boja
                listOf(FilterTab.BREND, FilterTab.KATEGORIJA, FilterTab.VELICINA, FilterTab.BOJA)
            }

            filterType == "category" -> {
                // From Category Selection: Kategorija → Brend → Veličina → Boja
                listOf(FilterTab.KATEGORIJA, FilterTab.BREND, FilterTab.VELICINA, FilterTab.BOJA)
            }

            else -> {
                // Default: Kategorija → Brend → Veličina → Boja (no Pol)
                listOf(FilterTab.KATEGORIJA, FilterTab.BREND, FilterTab.VELICINA, FilterTab.BOJA)
            }
        }
    }

    // Filter tabs to show only those with available data from API
    val tabOrder = remember(baseTabOrder, availableFilters) {
        baseTabOrder.filter { tab ->
            when (tab) {
                FilterTab.POL -> availableFilters?.genders?.isNotEmpty() == true
                FilterTab.KATEGORIJA -> availableFilters?.categories?.isNotEmpty() == true
                FilterTab.BREND -> availableFilters?.brands?.isNotEmpty() == true
                FilterTab.VELICINA -> availableFilters?.sizes?.isNotEmpty() == true
                FilterTab.BOJA -> availableFilters?.colors?.isNotEmpty() == true
            }
        }
    }

    // If arriving from Brand/Category and filters not yet available, show initial full-screen loader
    val isInitialFromBrandOrCategory = (filterType == "brand" || filterType == "category") && !fromHome
    if (availableFilters == null && isInitialFromBrandOrCategory) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            FashionLoader(assetName = "ff_black.json", speed = 3.5f)
        }
        return
    }

    // If no filters available after loading, close the screen
    if (tabOrder.isEmpty()) {
        LaunchedEffect(Unit) {
            onClose()
        }
        return
    }

    // Use initialTab if provided and it's in tabOrder, otherwise use first tab
    var currentTab by remember {
        mutableStateOf(
            if (initialTab != null && initialTab in tabOrder) initialTab
            else tabOrder.first()
        )
    }

    // Notify about tab changes
    LaunchedEffect(currentTab) {
        onTabChanged(currentTab)
    }

    // Update currentTab only if it's no longer in tabOrder (filter disappeared)
    LaunchedEffect(tabOrder) {
        if (currentTab !in tabOrder) {
            currentTab = tabOrder.first()
        }
    }

    // Initialize with active filters from API
    var selectedGenders by remember(activeFilters) {
        mutableStateOf(activeFilters["pol"] ?: setOf())
    }
    var selectedCategories by remember(activeFilters, filterType) {
        // Combine all category levels; when coming from Brand/Category also include 'kategorije'
        val categoryKeys = activeFilters.keys.filter {
            it.startsWith("category") || ((filterType == "brand" || filterType == "category") && it == "kategorije")
        }
        val allCategoryFilters = categoryKeys.flatMap { activeFilters[it] ?: emptySet() }.toSet()
        mutableStateOf(allCategoryFilters)
    }
    var selectedBrands by remember(activeFilters) {
        mutableStateOf(activeFilters["brend"] ?: setOf())
    }
    var selectedSizes by remember(activeFilters) {
        mutableStateOf(activeFilters["velicina"] ?: setOf())
    }
    var selectedColors by remember(activeFilters) {
        mutableStateOf(activeFilters["boja"] ?: setOf())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ProductFiltersContent(
            currentTab = currentTab,
            onTabChange = { newTab ->
                currentTab = newTab
            },
            tabOrder = tabOrder,
            availableFilters = availableFilters,
            selectedGenders = selectedGenders,
            selectedCategories = selectedCategories,
            selectedBrands = selectedBrands,
            selectedSizes = selectedSizes,
            selectedColors = selectedColors,
            onGenderToggle = { gender ->
                selectedGenders = if (selectedGenders.contains(gender)) {
                    selectedGenders - gender
                } else {
                    selectedGenders + gender
                }
                // Auto-apply filters after change
                onApplyFilters(
                    selectedGenders,
                    selectedCategories,
                    selectedBrands,
                    selectedSizes,
                    selectedColors
                )
            },
            onCategoryToggle = { category ->
                selectedCategories = if (selectedCategories.contains(category)) {
                    selectedCategories - category
                } else {
                    selectedCategories + category
                }
                // Auto-apply filters after change
                onApplyFilters(
                    selectedGenders,
                    selectedCategories,
                    selectedBrands,
                    selectedSizes,
                    selectedColors
                )
            },
            onBrandToggle = { brand ->
                selectedBrands = if (selectedBrands.contains(brand)) {
                    selectedBrands - brand
                } else {
                    selectedBrands + brand
                }
                // Auto-apply filters after change
                onApplyFilters(
                    selectedGenders,
                    selectedCategories,
                    selectedBrands,
                    selectedSizes,
                    selectedColors
                )
            },
            onSizeToggle = { size ->
                selectedSizes = if (selectedSizes.contains(size)) {
                    selectedSizes - size
                } else {
                    selectedSizes + size
                }
                // Auto-apply filters after change
                onApplyFilters(
                    selectedGenders,
                    selectedCategories,
                    selectedBrands,
                    selectedSizes,
                    selectedColors
                )
            },
            onColorToggle = { color ->
                selectedColors = if (selectedColors.contains(color)) {
                    selectedColors - color
                } else {
                    selectedColors + color
                }
                // Auto-apply filters after change
                onApplyFilters(
                    selectedGenders,
                    selectedCategories,
                    selectedBrands,
                    selectedSizes,
                    selectedColors
                )
            },
            onClearCurrentFilter = {
                when (currentTab) {
                    FilterTab.POL -> selectedGenders = setOf()
                    FilterTab.KATEGORIJA -> selectedCategories = setOf()
                    FilterTab.BREND -> selectedBrands = setOf()
                    FilterTab.VELICINA -> selectedSizes = setOf()
                    FilterTab.BOJA -> selectedColors = setOf()
                }
                // Auto-apply after clearing
                onApplyFilters(
                    selectedGenders,
                    selectedCategories,
                    selectedBrands,
                    selectedSizes,
                    selectedColors
                )
            },
            onClearAll = {
                // Check if all filters are already empty - don't trigger request if nothing changes
                val allEmpty = selectedGenders.isEmpty() &&
                        selectedCategories.isEmpty() &&
                        selectedBrands.isEmpty() &&
                        selectedSizes.isEmpty() &&
                        selectedColors.isEmpty()
                
                if (!allEmpty) {
                    // Only clear and apply if there are active filters
                    selectedGenders = setOf()
                    selectedCategories = setOf()
                    selectedBrands = setOf()
                    selectedSizes = setOf()
                    selectedColors = setOf()
                    // Auto-apply after clearing all
                    onApplyFilters(
                        selectedGenders,
                        selectedCategories,
                        selectedBrands,
                        selectedSizes,
                        selectedColors
                    )
                }
                // Always switch to first tab
                currentTab = tabOrder.first()
            },
            onApplyFilters = {
                onApplyFilters(
                    selectedGenders,
                    selectedCategories,
                    selectedBrands,
                    selectedSizes,
                    selectedColors
                )
            },
            onClose = onClose,
            onNavigateToHome = onNavigateToHome
        )

        // Loading overlay
        if (isLoadingFilters) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) { }, // Block interactions
                contentAlignment = Alignment.Center
            ) {
                FashionLoader(speed = 3.5f)
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductFiltersContent(
    currentTab: FilterTab,
    onTabChange: (FilterTab) -> Unit,
    tabOrder: List<FilterTab>,
    availableFilters: com.fashiontothem.ff.domain.model.FilterOptions?,
    selectedGenders: Set<String>,
    selectedCategories: Set<String>,
    selectedBrands: Set<String>,
    selectedSizes: Set<String>,
    selectedColors: Set<String>,
    onGenderToggle: (String) -> Unit,
    onCategoryToggle: (String) -> Unit,
    onBrandToggle: (String) -> Unit,
    onSizeToggle: (String) -> Unit,
    onColorToggle: (String) -> Unit,
    onClearCurrentFilter: () -> Unit,
    onClearAll: () -> Unit,
    onApplyFilters: () -> Unit,
    onClose: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with logo (clickable to go home)
            FashionTopBar { onNavigateToHome() }

            // Responsive spacing after top bar
            val topBarSpacing = when {
                screenHeight < 700.dp -> 4.dp
                screenHeight < 1200.dp -> 8.dp
                else -> 15.dp
            }
            Spacer(modifier = Modifier.height(topBarSpacing))

            // Responsive filter icon size
            val filterIconSize = when {
                screenWidth < 400.dp -> 30.dp
                screenWidth < 600.dp -> 40.dp
                else -> 120.dp
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.size(filterIconSize),
                    painter = painterResource(id = R.drawable.search_filter_icon),
                    contentDescription = "Search",
                    contentScale = ContentScale.Fit
                )
            }

            // Responsive spacing before card
            val iconCardSpacing = when {
                screenHeight < 700.dp -> 10.dp
                screenHeight < 1200.dp -> 20.dp
                else -> 30.dp
            }
            Spacer(modifier = Modifier.height(iconCardSpacing))

            // Responsive card padding
            val cardHorizontalPadding = when {
                screenWidth < 400.dp -> 10.dp
                screenWidth < 600.dp -> 20.dp
                else -> 50.dp
            }
            
            // Responsive corner radius
            val cardCornerRadius = when {
                screenWidth < 400.dp -> 20.dp
                screenWidth < 600.dp -> 30.dp
                else -> 40.dp
            }
            
            // Responsive content padding
            val contentPadding = when {
                screenWidth < 400.dp -> 12.dp
                screenWidth < 600.dp -> 18.dp
                else -> 40.dp
            }

            // Filter Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = cardHorizontalPadding),
                shape = RoundedCornerShape(cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                // Header with back/title/close and tabs
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Title row with back and close buttons
                    val currentIndex = tabOrder.indexOf(currentTab)
                    val showBackButton = currentIndex > 0 && tabOrder.size > 1

                    // Responsive button sizes
                    val headerButtonSize = when {
                        screenWidth < 400.dp -> 20.dp
                        screenWidth < 600.dp -> 30.dp
                        else -> 40.dp
                    }
                    
                    val headerSpacing = when {
                        screenWidth < 400.dp -> 8.dp
                        screenWidth < 600.dp -> 12.dp
                        else -> 16.dp
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Back button (conditional)
                        if (showBackButton) {
                            IconButton(
                                onClick = {
                                    val previousTab = tabOrder[currentIndex - 1]
                                    onTabChange(previousTab)
                                },
                                modifier = Modifier.size(headerButtonSize)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(headerButtonSize)
                                        .background(Color(0xFFB50938), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.back_white_icon),
                                        contentDescription = stringResource(R.string.cd_back),
                                        modifier = Modifier.size(headerButtonSize * 0.6f),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        } else {
                            // Spacer to keep title centered
                            Spacer(modifier = Modifier.size(headerButtonSize))
                        }

                        Spacer(modifier = Modifier.width(headerSpacing))

                        // Responsive title font size
                        val titleFontSize = when {
                            screenWidth < 400.dp -> 12.sp
                            screenWidth < 600.dp -> 18.sp
                            else -> 28.sp
                        }

                        // Title
                        Text(
                            text = stringResource(R.string.filter_title),
                            fontFamily = Fonts.Poppins,
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.width(headerSpacing))

                        // Close button (always visible)
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(headerButtonSize)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(headerButtonSize)
                                    .background(Color(0xFFB50938), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.x_white_icon),
                                    contentDescription = stringResource(R.string.cd_close),
                                    modifier = Modifier.size(headerButtonSize * 0.6f),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    // Responsive spacing after header
                    val headerTabSpacing = when {
                        screenHeight < 700.dp -> 20.dp
                        screenHeight < 1200.dp -> 30.dp
                        else -> 40.dp
                    }
                    Spacer(modifier = Modifier.height(headerTabSpacing))

                    // Responsive tab container
                    val tabContainerHeight = when {
                        screenHeight < 700.dp -> 60.dp
                        screenHeight < 1200.dp -> 70.dp
                        else -> 80.dp
                    }
                    
                    val tabContainerPadding = when {
                        screenWidth < 400.dp -> 8.dp
                        screenWidth < 600.dp -> 10.dp
                        else -> 60.dp
                    }
                    
                    // Tab circles with connecting lines - Dynamic based on tabOrder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tabContainerHeight)
                            .padding(horizontal = tabContainerPadding)
                    ) {
                        // Responsive circle size
                        val circleSize = when {
                            screenWidth < 400.dp -> 30.dp
                            screenWidth < 600.dp -> 36.dp
                            else -> 46.dp
                        }
                        
                        val innerCircleSize = when {
                            screenWidth < 400.dp -> 22.dp
                            screenWidth < 600.dp -> 26.dp
                            else -> 32.dp
                        }
                        
                        val lineOffset = circleSize / 2
                        val linePadding = circleSize / 2
                        
                        // Background layer: Horizontal line (through center of circles)
                        if (tabOrder.size > 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .align(Alignment.TopStart)
                                    .offset(y = lineOffset)
                                    .padding(horizontal = linePadding)
                                    .background(Color(0xFFEBEBEB))
                            )
                        }

                        // Foreground layer: Tab circles (over the line)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopStart),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            tabOrder.forEach { tab ->
                                FilterTabCircle(
                                    tab = tab,
                                    isSelected = currentTab == tab,
                                    onClick = { onTabChange(tab) },
                                    screenWidth = screenWidth
                                )
                            }
                        }
                    }
                }

                // Responsive spacing before content
                val tabContentSpacing = when {
                    screenHeight < 700.dp -> 12.dp
                    screenHeight < 1200.dp -> 22.dp
                    else -> 50.dp
                }
                Spacer(modifier = Modifier.height(tabContentSpacing))

                // Content area with scroll
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Responsive subtitle font size
                        val subtitleFontSize = when {
                            screenWidth < 400.dp -> 12.sp
                            screenWidth < 600.dp -> 18.sp
                            else -> 26.sp
                        }
                        
                        // Tab subtitle
                        Text(
                            text = stringResource(
                                id = when (currentTab) {
                                    FilterTab.POL -> R.string.filter_subtitle_gender
                                    FilterTab.KATEGORIJA -> R.string.filter_subtitle_category
                                    FilterTab.BREND -> R.string.filter_subtitle_brand
                                    FilterTab.VELICINA -> R.string.filter_subtitle_size
                                    FilterTab.BOJA -> R.string.filter_subtitle_color
                                }
                            ),
                            fontFamily = Fonts.Poppins,
                            fontSize = subtitleFontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF707070),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        // Responsive spacing after subtitle
                        val subtitleContentSpacing = when {
                            screenHeight < 700.dp -> 12.dp
                            screenHeight < 1200.dp -> 18.dp
                            else -> 40.dp
                        }
                        Spacer(modifier = Modifier.height(subtitleContentSpacing))

                        // Filter content based on tab
                        when (currentTab) {
                            FilterTab.POL -> {
                                GenderFilterContent(
                                    availableGenders = availableFilters?.genders ?: emptyList(),
                                    selectedGenders = selectedGenders,
                                    onGenderToggle = onGenderToggle,
                                    screenWidth = screenWidth
                                )
                            }

                            FilterTab.KATEGORIJA -> {
                                CategoryFilterContent(
                                    availableCategories = availableFilters?.categories
                                        ?: emptyList(),
                                    selectedCategories = selectedCategories,
                                    onCategoryToggle = onCategoryToggle,
                                    screenWidth = screenWidth
                                )
                            }

                            FilterTab.BREND -> {
                                BrandFilterContent(
                                    availableBrands = availableFilters?.brands ?: emptyList(),
                                    selectedBrands = selectedBrands,
                                    onBrandToggle = onBrandToggle,
                                    screenWidth = screenWidth
                                )
                            }

                            FilterTab.VELICINA -> {
                                SizeFilterContent(
                                    availableSizes = availableFilters?.sizes ?: emptyList(),
                                    selectedSizes = selectedSizes,
                                    onSizeToggle = onSizeToggle,
                                    screenWidth = screenWidth
                                )
                            }

                            FilterTab.BOJA -> {
                                ColorFilterContent(
                                    availableColors = availableFilters?.colors ?: emptyList(),
                                    selectedColors = selectedColors,
                                    onColorToggle = onColorToggle,
                                    screenWidth = screenWidth
                                )
                            }
                        }

                        // Responsive spacing after content
                        val contentBottomSpacing = when {
                            screenHeight < 700.dp -> 12.dp
                            screenHeight < 1200.dp -> 20.dp
                            else -> 40.dp
                        }
                        Spacer(modifier = Modifier.height(contentBottomSpacing))
                    }
                }

                // Selected count and clear button
                val currentSelectionCount = when (currentTab) {
                    FilterTab.POL -> selectedGenders.size
                    FilterTab.KATEGORIJA -> selectedCategories.size
                    FilterTab.BREND -> selectedBrands.size
                    FilterTab.VELICINA -> selectedSizes.size
                    FilterTab.BOJA -> selectedColors.size
                }

                // Responsive clear icon size
                val clearIconSize = when {
                    screenWidth < 400.dp -> 10.dp
                    screenWidth < 600.dp -> 12.dp
                    else -> 18.dp
                }
                
                // Responsive clear text font size
                val clearTextFontSize = when {
                    screenWidth < 400.dp -> 10.sp
                    screenWidth < 600.dp -> 12.sp
                    else -> 20.sp
                }
                
                if (currentSelectionCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableDebounced { onClearCurrentFilter() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.silver_close),
                            contentDescription = stringResource(R.string.cd_clear),
                            modifier = Modifier.size(clearIconSize),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                R.string.filter_selected_count,
                                currentSelectionCount
                            ),
                            fontFamily = Fonts.Poppins,
                            fontSize = clearTextFontSize,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF707070)
                        )
                    }
                }

                // Responsive spacing before button
                val clearButtonSpacing = when {
                    screenHeight < 700.dp -> 8.dp
                    screenHeight < 1200.dp -> 12.dp
                    else -> 20.dp
                }
                Spacer(modifier = Modifier.height(clearButtonSpacing))

                // Responsive button height
                val buttonHeight = when {
                    screenWidth < 400.dp -> 34.dp
                    screenWidth < 600.dp -> 52.dp
                    else -> 70.dp
                }
                
                // Responsive button corner radius
                val buttonCornerRadius = when {
                    screenWidth < 400.dp -> 24.dp
                    screenWidth < 600.dp -> 32.dp
                    else -> 50.dp
                }
                
                // Responsive button font size
                val buttonFontSize = when {
                    screenWidth < 400.dp -> 10.sp
                    screenWidth < 600.dp -> 14.sp
                    else -> 22.sp
                }
                
                // Pretraži button - closes filter screen (like X button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight)
                        .clip(RoundedCornerShape(buttonCornerRadius))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4F0418),
                                    Color(0xFFB50938)
                                )
                            )
                        )
                        .clickableDebounced { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.filter_search_button),
                        fontFamily = Fonts.Poppins,
                        fontSize = buttonFontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Responsive spacing between buttons
                val buttonSpacing = when {
                    screenHeight < 700.dp -> 12.dp
                    screenHeight < 1200.dp -> 14.dp
                    else -> 16.dp
                }
                Spacer(modifier = Modifier.height(buttonSpacing))

                // Bottom buttons row
                val currentIndex = tabOrder.indexOf(currentTab)
                val isLastTab = currentIndex == tabOrder.lastIndex

                // Responsive bottom button spacing
                val bottomButtonSpacing = when {
                    screenWidth < 400.dp -> 12.dp
                    screenWidth < 600.dp -> 14.dp
                    else -> 16.dp
                }
                
                // Responsive bottom button icon size
                val bottomButtonIconSize = when {
                    screenWidth < 400.dp -> 10.dp
                    screenWidth < 600.dp -> 12.dp
                    else -> 20.dp
                }
                
                // Responsive bottom button text spacing
                val bottomButtonTextSpacing = when {
                    screenWidth < 400.dp -> 8.dp
                    screenWidth < 600.dp -> 10.dp
                    else -> 12.dp
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(bottomButtonSpacing)
                ) {
                    // Očisti sve button - full width on last tab, half width otherwise
                    Button(
                        onClick = onClearAll,
                        modifier = Modifier
                            .then(
                                if (isLastTab) Modifier.fillMaxWidth()
                                else Modifier.weight(1f)
                            )
                            .height(buttonHeight),
                        shape = RoundedCornerShape(buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE5E5E5))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.silver_close),
                                contentDescription = stringResource(R.string.cd_clear_all),
                                modifier = Modifier.size(bottomButtonIconSize),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(bottomButtonTextSpacing))
                            Text(
                                text = stringResource(R.string.filter_clear_all_button),
                                fontFamily = Fonts.Poppins,
                                fontSize = buttonFontSize,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF707070)
                            )
                        }
                    }

                    // Nastavi button - only show if not on last tab
                    if (!isLastTab) {
                        Button(
                            onClick = {
                                // Go to next tab
                                val nextTab = tabOrder[currentIndex + 1]
                                onTabChange(nextTab)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight),
                            shape = RoundedCornerShape(buttonCornerRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Color(0xFFE5E5E5)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.filter_continue_button),
                                fontFamily = Fonts.Poppins,
                                fontSize = buttonFontSize,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Responsive bottom spacing
        val bottomSpacing = when {
            screenHeight < 700.dp -> 24.dp
            screenHeight < 1200.dp -> 36.dp
            else -> 50.dp
        }
        Spacer(modifier = Modifier.height(bottomSpacing))
    }
    }
}

@Composable
private fun FilterTabCircle(
    tab: FilterTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    screenWidth: Dp,
) {
    val label = stringResource(
        id = when (tab) {
            FilterTab.POL -> R.string.filter_tab_gender
            FilterTab.KATEGORIJA -> R.string.filter_tab_category
            FilterTab.BREND -> R.string.filter_tab_brand
            FilterTab.VELICINA -> R.string.filter_tab_size
            FilterTab.BOJA -> R.string.filter_tab_color
        }
    )
    
    // Responsive circle sizes
    val circleSize = when {
        screenWidth < 400.dp -> 32.dp
        screenWidth < 600.dp -> 38.dp
        else -> 46.dp
    }
    
    val innerCircleSize = when {
        screenWidth < 400.dp -> 22.dp
        screenWidth < 600.dp -> 26.dp
        else -> 32.dp
    }
    
    // Responsive text font size
    val labelFontSize = when {
        screenWidth < 400.dp -> 10.sp
        screenWidth < 600.dp -> 13.sp
        else -> 18.sp
    }
    
    // Responsive spacing
    val circleTextSpacing = when {
        screenWidth < 400.dp -> 8.dp
        screenWidth < 600.dp -> 10.dp
        else -> 12.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickableDebounced { onClick() }
    ) {
        // Circle indicator - border ring + inner circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(circleSize)
        ) {
            // Outer ring - white background to hide line + border
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .background(Color.White, CircleShape)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFEBEBEB),
                        shape = CircleShape
                    )
            )
            // Inner circle - crveni za aktivni, malo tamniji sivi za neaktivni
            Box(
                modifier = Modifier
                    .size(innerCircleSize)
                    .background(
                        color = if (isSelected) Color(0xFFB50938) else Color(0xFFC8C8C8),
                        shape = CircleShape
                    )
            )
        }

        Spacer(modifier = Modifier.height(circleTextSpacing))

        Text(
            text = label,
            fontFamily = Fonts.Poppins,
            fontSize = labelFontSize,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color(0xFF707070),
            textAlign = TextAlign.Center
        )
    }
}

// Gender Filter Content (Multiselect buttons with gradient background - same as Category)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenderFilterContent(
    availableGenders: List<com.fashiontothem.ff.domain.model.FilterOption>,
    selectedGenders: Set<String>,
    onGenderToggle: (String) -> Unit,
    screenWidth: Dp,
) {
    // Use API genders only
    val genders = availableGenders
    
    // Responsive spacing
    val itemSpacing = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 16.dp
        else -> 20.dp
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        maxItemsInEachRow = 2
    ) {
        genders.forEach { gender ->
            CategoryButton(
                text = gender.label,
                isSelected = selectedGenders.contains(gender.key),
                onClick = { onGenderToggle(gender.key) },
                modifier = Modifier.weight(1f),
                screenWidth = screenWidth
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilterContent(
    availableCategories: List<com.fashiontothem.ff.domain.model.FilterOption>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    screenWidth: Dp,
) {
    // Use API categories only
    val categories = availableCategories
    
    // Responsive spacing
    val itemSpacing = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 16.dp
        else -> 20.dp
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        maxItemsInEachRow = 2
    ) {
        categories.forEach { category ->
            CategoryButton(
                text = category.label,
                isSelected = selectedCategories.contains(category.key),
                onClick = { onCategoryToggle(category.key) },
                modifier = Modifier.weight(1f),
                screenWidth = screenWidth
            )
        }
    }
}

@Composable
private fun CategoryButton(
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
        screenWidth < 400.dp -> 36.dp
        screenWidth < 600.dp -> 54.dp
        else -> 80.dp
    }
    
    // Responsive corner radius
    val cornerRadius = when {
        screenWidth < 400.dp -> 28.dp
        screenWidth < 600.dp -> 34.dp
        else -> 40.dp
    }
    
    // Responsive font size
    val fontSize = when {
        screenWidth < 400.dp -> 10.sp
        screenWidth < 600.dp -> 14.sp
        else -> 20.sp
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(buttonHeight),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.Transparent else Color.White
        ),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(
            2.dp,
            Color(0xFFEBEBEB)
        ) else null,
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient, shape = RoundedCornerShape(cornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontFamily = Fonts.Poppins,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        } else {
            Text(
                text = text,
                fontFamily = Fonts.Poppins,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

// BREND Content
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BrandFilterContent(
    availableBrands: List<com.fashiontothem.ff.domain.model.FilterOption>,
    selectedBrands: Set<String>,
    onBrandToggle: (String) -> Unit,
    screenWidth: Dp,
) {
    // Use API brands (no fallback needed - brands should always come from API)
    val brands = availableBrands
    
    // Responsive spacing
    val itemSpacing = when {
        screenWidth < 400.dp -> 16.dp
        screenWidth < 600.dp -> 22.dp
        else -> 30.dp
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        brands.forEach { brand ->
            BrandItem(
                brandName = brand.label,
                imageUrl = brand.imageUrl,
                isSelected = selectedBrands.contains(brand.key),
                onClick = { onBrandToggle(brand.key) },
                screenWidth = screenWidth
            )
        }
    }
}

@Composable
private fun BrandItem(
    brandName: String,
    imageUrl: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    screenWidth: Dp,
) {
    // Responsive brand item size
    val itemSize = when {
        screenWidth < 400.dp -> 100.dp
        screenWidth < 600.dp -> 130.dp
        else -> 170.dp
    }
    
    val imageSize = when {
        screenWidth < 400.dp -> 88.dp
        screenWidth < 600.dp -> 115.dp
        else -> 150.dp
    }
    
    val imagePadding = when {
        screenWidth < 400.dp -> 10.dp
        screenWidth < 600.dp -> 13.dp
        else -> 16.dp
    }
    
    // Responsive border width
    val borderWidth = when {
        screenWidth < 400.dp -> 1.5.dp
        screenWidth < 600.dp -> 2.dp
        else -> 2.dp
    }
    
    // Responsive font size
    val fontSize = when {
        screenWidth < 400.dp -> 12.sp
        screenWidth < 600.dp -> 16.sp
        else -> 20.sp
    }
    
    Box(
        modifier = Modifier
            .size(itemSize)
            .border(
                width = if (isSelected) borderWidth else 1.dp,
                color = if (isSelected) Color(0xFFB50938) else Color.Transparent,
                shape = CircleShape
            )
            .clickableDebounced { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            // Display brand logo image - no clipping to show full square images
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .padding(imagePadding),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = brandName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
            // Fallback to text if no image available
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .clip(CircleShape)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = brandName,
                    fontFamily = Fonts.Poppins,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

// VELIČINA Content
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SizeFilterContent(
    availableSizes: List<com.fashiontothem.ff.domain.model.FilterOption>,
    selectedSizes: Set<String>,
    onSizeToggle: (String) -> Unit,
    screenWidth: Dp,
) {
    // Use API sizes (no fallback needed - sizes should always come from API)
    val sizes = availableSizes
    
    // Responsive spacing
    val itemSpacing = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 16.dp
        else -> 20.dp
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        sizes.forEach { size ->
            SizeButton(
                text = size.label,
                isSelected = selectedSizes.contains(size.key),
                onClick = { onSizeToggle(size.key) },
                screenWidth = screenWidth
            )
        }
    }
}

@Composable
private fun SizeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    screenWidth: Dp,
) {
    // Responsive button size
    val buttonSize = when {
        screenWidth < 400.dp -> 40.dp
        screenWidth < 600.dp -> 60.dp
        else -> 80.dp
    }
    
    // Responsive font size
    val fontSize = when {
        screenWidth < 400.dp -> 10.sp
        screenWidth < 600.dp -> 14.sp
        else -> 20.sp
    }
    
    Box(
        modifier = Modifier
            .size(buttonSize)
            .then(
                if (isSelected) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4F0418),
                                Color(0xFFB50938)
                            )
                        ),
                        shape = CircleShape
                    )
                } else {
                    Modifier
                        .background(Color.White, CircleShape)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE5E5E5),
                            shape = CircleShape
                        )
                }
            )
            .clickableDebounced { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Fonts.Poppins,
            fontSize = fontSize,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

// BOJA Content
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorFilterContent(
    availableColors: List<com.fashiontothem.ff.domain.model.FilterOption>,
    selectedColors: Set<String>,
    onColorToggle: (String) -> Unit,
    screenWidth: Dp,
) {
    // Responsive spacing
    val itemSpacing = when {
        screenWidth < 400.dp -> 12.dp
        screenWidth < 600.dp -> 16.dp
        else -> 20.dp
    }
    
    // Responsive color circle size
    val circleSize = when {
        screenWidth < 400.dp -> 40.dp
        screenWidth < 600.dp -> 60.dp
        else -> 80.dp
    }
    
    val innerCircleSize = when {
        screenWidth < 400.dp -> 40.dp
        screenWidth < 600.dp -> 46.dp
        else -> 60.dp
    }
    
    // Responsive border width
    val borderWidth = when {
        screenWidth < 400.dp -> 1.5.dp
        screenWidth < 600.dp -> 2.dp
        else -> 2.dp
    }
    
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        availableColors.forEach { filterOption ->
            val isSelected = selectedColors.contains(filterOption.key)
            val hexCode = filterOption.hexCode

            Box(
                modifier = Modifier
                    .size(circleSize)
                    .border(
                        width = if (isSelected) borderWidth else 1.dp,
                        color = if (isSelected) Color(0xFFB50938) else Color(0xFFEBEBEB),
                        shape = CircleShape
                    )
                    .clickableDebounced { onColorToggle(filterOption.key) },
                contentAlignment = Alignment.Center
            ) {
                if (hexCode != null && hexCode.startsWith("#")) {
                    // Hex color code - display as colored circle
                    Box(
                        modifier = Modifier
                            .size(innerCircleSize)
                            .background(
                                Color(hexCode.toColorInt()),
                                shape = CircleShape
                            )
                    )
                } else if (hexCode != null && hexCode.isNotEmpty()) {
                    // Image URL - display as image
                    Image(
                        painter = rememberAsyncImagePainter(hexCode),
                        contentDescription = null,
                        modifier = Modifier
                            .size(innerCircleSize)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback - gray placeholder
                    Box(
                        modifier = Modifier
                            .size(innerCircleSize)
                            .background(Color(0xFFEBEBEB), shape = CircleShape)
                    )
                }
            }
        }
    }
}

// Preview helper function to create mock filter data
private fun getMockFilterOptions() = com.fashiontothem.ff.domain.model.FilterOptions(
    genders = listOf(
        com.fashiontothem.ff.domain.model.FilterOption("zene", "Žene", 120),
        com.fashiontothem.ff.domain.model.FilterOption("muskarci", "Muškarci", 95),
        com.fashiontothem.ff.domain.model.FilterOption("deca", "Deca", 45)
    ),
    categories = listOf(
        com.fashiontothem.ff.domain.model.FilterOption("majice", "Majice", 80),
        com.fashiontothem.ff.domain.model.FilterOption("farmerke", "Farmerke", 60),
        com.fashiontothem.ff.domain.model.FilterOption("jakne", "Jakne", 40)
    ),
    brands = listOf(
        com.fashiontothem.ff.domain.model.FilterOption("nike", "Nike", 50),
        com.fashiontothem.ff.domain.model.FilterOption("adidas", "Adidas", 45),
        com.fashiontothem.ff.domain.model.FilterOption("guess", "Guess", 35)
    ),
    sizes = listOf(
        com.fashiontothem.ff.domain.model.FilterOption("s", "S", 30),
        com.fashiontothem.ff.domain.model.FilterOption("m", "M", 40),
        com.fashiontothem.ff.domain.model.FilterOption("l", "L", 35),
        com.fashiontothem.ff.domain.model.FilterOption("xl", "XL", 25)
    ),
    colors = listOf(
        com.fashiontothem.ff.domain.model.FilterOption("crna", "Crna", 50),
        com.fashiontothem.ff.domain.model.FilterOption("bela", "Bela", 40),
        com.fashiontothem.ff.domain.model.FilterOption("plava", "Plava", 30)
    )
)

@Preview(name = "Small Phone - Pol Tab", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Pol_Small() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.POL,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Medium Phone - Pol Tab", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Pol_Medium() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.POL,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Large Phone - Pol Tab", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Pol_Large() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.POL,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Filters - Pol Tab", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Pol() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.POL,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Small Phone - Kategorija Tab", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Kategorija_Small() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.KATEGORIJA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Medium Phone - Kategorija Tab", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Kategorija_Medium() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.KATEGORIJA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Large Phone - Kategorija Tab", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Kategorija_Large() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.KATEGORIJA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Filters - Kategorija Tab", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Kategorija() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.KATEGORIJA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Small Phone - Brend Tab", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Brend_Small() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.BREND,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Medium Phone - Brend Tab", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Brend_Medium() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.BREND,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Large Phone - Brend Tab", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Brend_Large() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.BREND,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Filters - Brend Tab", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Brend() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.BREND,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Small Phone - Veličina Tab", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Velicina_Small() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.VELICINA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Medium Phone - Veličina Tab", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Velicina_Medium() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.VELICINA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Large Phone - Veličina Tab", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Velicina_Large() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.VELICINA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Filters - Veličina Tab", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Velicina() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.VELICINA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Small Phone - Boja Tab", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Boja_Small() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.BOJA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Medium Phone - Boja Tab", widthDp = 411, heightDp = 731, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Boja_Medium() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.BOJA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Large Phone - Boja Tab", widthDp = 480, heightDp = 854, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Boja_Large() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.BOJA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

@Preview(name = "Filters - Boja Tab", widthDp = 1080, heightDp = 1920, showBackground = true)
@Composable
fun ProductFiltersScreenPreview_Boja() {
    ProductFiltersScreen(
        availableFilters = getMockFilterOptions(),
        activeFilters = emptyMap(),
        isLoadingFilters = false,
        fromHome = true,
        filterType = null,
        initialTab = FilterTab.BOJA,
        onApplyFilters = { _, _, _, _, _ -> },
        onClose = {},
        onNavigateToHome = {},
        onTabChanged = {}
    )
}

