package com.fashiontothem.ff.presentation.filter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

    // If no filters available, close the screen
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
    var selectedCategories by remember(activeFilters) {
        // Combine all category levels (category1, category2, category3, ...)
        val allCategoryFilters = activeFilters.keys
            .filter { it.startsWith("category") }
            .flatMap { activeFilters[it] ?: emptySet() }
            .toSet()
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
                selectedGenders = setOf()
                selectedCategories = setOf()
                selectedBrands = setOf()
                selectedSizes = setOf()
                selectedColors = setOf()
                currentTab = tabOrder.first()
                // Auto-apply after clearing all
                onApplyFilters(
                    selectedGenders,
                    selectedCategories,
                    selectedBrands,
                    selectedSizes,
                    selectedColors
                )
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
                FashionLoader()
            }
        }
    }
}

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFFAFAFA)
            )
    ) {
        // Top Bar with logo (clickable to go home)
        FashionTopBar { onNavigateToHome() }

        Spacer(modifier = Modifier.height(15.dp))

        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(id = R.drawable.search_filter_icon),
            contentDescription = "Search",
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Filter Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 50.dp),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp)
            ) {
                // Header with back/title/close and tabs
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Title row with back and close buttons
                    val currentIndex = tabOrder.indexOf(currentTab)
                    val showBackButton = currentIndex > 0 && tabOrder.size > 1

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
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFB50938), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.back_white_icon),
                                        contentDescription = stringResource(R.string.cd_back),
                                    )
                                }
                            }
                        } else {
                            // Spacer to keep title centered
                            Spacer(modifier = Modifier.size(40.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Title
                        Text(
                            text = stringResource(R.string.filter_title),
                            fontFamily = Fonts.Poppins,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Close button (always visible)
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFB50938), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.x_white_icon),
                                    contentDescription = stringResource(R.string.cd_close),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Tab circles with connecting lines - Dynamic based on tabOrder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp) // ✅ Increased height for larger circles and text
                            .padding(horizontal = 60.dp)
                    ) {
                        // Background layer: Horizontal line (through center of circles - at 23dp from top since circles are 46dp)
                        if (tabOrder.size > 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .align(Alignment.TopStart)
                                    .offset(y = 23.dp) // Center of 46dp circle = 23dp from top
                                    .padding(horizontal = 23.dp) // ✅ Padding od pola kruga (46dp/2) da linija ne prelazi sa strane prvog/poslednjeg kruga
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
                                    onClick = { onTabChange(tab) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))

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
                            fontSize = 26.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF707070),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Filter content based on tab
                        when (currentTab) {
                            FilterTab.POL -> {
                                GenderFilterContent(
                                    availableGenders = availableFilters?.genders ?: emptyList(),
                                    selectedGenders = selectedGenders,
                                    onGenderToggle = onGenderToggle
                                )
                            }

                            FilterTab.KATEGORIJA -> {
                                CategoryFilterContent(
                                    availableCategories = availableFilters?.categories
                                        ?: emptyList(),
                                    selectedCategories = selectedCategories,
                                    onCategoryToggle = onCategoryToggle
                                )
                            }

                            FilterTab.BREND -> {
                                BrandFilterContent(
                                    availableBrands = availableFilters?.brands ?: emptyList(),
                                    selectedBrands = selectedBrands,
                                    onBrandToggle = onBrandToggle
                                )
                            }

                            FilterTab.VELICINA -> {
                                SizeFilterContent(
                                    availableSizes = availableFilters?.sizes ?: emptyList(),
                                    selectedSizes = selectedSizes,
                                    onSizeToggle = onSizeToggle
                                )
                            }

                            FilterTab.BOJA -> {
                                ColorFilterContent(
                                    availableColors = availableFilters?.colors ?: emptyList(),
                                    selectedColors = selectedColors,
                                    onColorToggle = onColorToggle
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                R.string.filter_selected_count,
                                currentSelectionCount
                            ),
                            fontFamily = Fonts.Poppins,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF707070)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Pretraži button - closes filter screen (like X button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4F0418),
                                    Color(0xFFB50938)
                                )
                            )
                        )
                        .clickableDebounced { onClose() }, // ✅ Close filter screen
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.filter_search_button),
                        fontFamily = Fonts.Poppins,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom buttons row
                val currentIndex = tabOrder.indexOf(currentTab)
                val isLastTab = currentIndex == tabOrder.lastIndex

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Očisti sve button - full width on last tab, half width otherwise
                    Button(
                        onClick = onClearAll,
                        modifier = Modifier
                            .then(
                                if (isLastTab) Modifier.fillMaxWidth()
                                else Modifier.weight(1f)
                            )
                            .height(70.dp),
                        shape = RoundedCornerShape(50.dp),
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
                                contentDescription = stringResource(R.string.cd_clear_all)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.filter_clear_all_button),
                                fontFamily = Fonts.Poppins,
                                fontSize = 22.sp,
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
                                .height(70.dp),
                            shape = RoundedCornerShape(50.dp),
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
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
private fun FilterTabCircle(
    tab: FilterTab,
    isSelected: Boolean,
    onClick: () -> Unit,
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickableDebounced { onClick() }
    ) {
        // Circle indicator - border ring + inner circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(46.dp)
        ) {
            // Outer ring - white background to hide line + border
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color.White, CircleShape) // ✅ Bela pozadina da prekrije liniju
                    .border(
                        width = 1.dp,
                        color = Color(0xFFEBEBEB),
                        shape = CircleShape
                    )
            )
            // Inner circle - crveni za aktivni, malo tamniji sivi za neaktivni
            Box(
                modifier = Modifier
                    .size(32.dp) // ✅ Smanjeno sa 36dp na 32dp
                    .background(
                        color = if (isSelected) Color(0xFFB50938) else Color(0xFFC8C8C8),
                        shape = CircleShape
                    )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = label,
            fontFamily = Fonts.Poppins,
            fontSize = 18.sp,
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
) {
    // Use API genders only
    val genders = availableGenders

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        maxItemsInEachRow = 2
    ) {
        genders.forEach { gender ->
            CategoryButton(
                text = gender.label,
                isSelected = selectedGenders.contains(gender.key),
                onClick = { onGenderToggle(gender.key) },
                modifier = Modifier.weight(1f)
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
) {
    // Use API categories only
    val categories = availableCategories

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        maxItemsInEachRow = 2
    ) {
        categories.forEach { category ->
            CategoryButton(
                text = category.label,
                isSelected = selectedCategories.contains(category.key),
                onClick = { onCategoryToggle(category.key) },
                modifier = Modifier.weight(1f)
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
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4F0418),
            Color(0xFFB50938)
        )
    )

    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(40.dp),
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
                    .background(gradient, shape = RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontFamily = Fonts.Poppins,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        } else {
            Text(
                text = text,
                fontFamily = Fonts.Poppins,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
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
) {
    // Use API brands (no fallback needed - brands should always come from API)
    val brands = availableBrands

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        brands.forEach { brand ->
            BrandItem(
                brandName = brand.label,
                imageUrl = brand.imageUrl,
                isSelected = selectedBrands.contains(brand.key),
                onClick = { onBrandToggle(brand.key) }
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
) {
    Box(
        modifier = Modifier
            .size(170.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
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
                    .size(150.dp)
                    .padding(16.dp),
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
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = brandName,
                    fontFamily = Fonts.Poppins,
                    fontSize = 20.sp,
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
) {
    // Use API sizes (no fallback needed - sizes should always come from API)
    val sizes = availableSizes

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        sizes.forEach { size ->
            SizeButton(
                text = size.label,
                isSelected = selectedSizes.contains(size.key),
                onClick = { onSizeToggle(size.key) }
            )
        }
    }
}

@Composable
private fun SizeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(80.dp)
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
            fontSize = 20.sp,
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
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        availableColors.forEach { filterOption ->
            val isSelected = selectedColors.contains(filterOption.key)
            val hexCode = filterOption.hexCode

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
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
                            .size(60.dp)
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
                            .size(60.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback - gray placeholder
                    Box(
                        modifier = Modifier
                            .size(60.dp)
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

