package com.fashiontothem.ff.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fashiontothem.ff.ui.theme.Fonts
import humer.UvcCamera.R

/**
 * F&F Tothem - Category Settings Screen
 *
 * Allows configuration of category IDs and levels for Akcije and Novo categories.
 */
@Composable
fun CategorySettingsScreen(
    viewModel: CategorySettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    // Show success message when saved
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearSavedState()
        }
    }

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

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = stringResource(id = R.string.category_settings_title),
                fontFamily = Fonts.Poppins,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Success message
            if (uiState.isSaved) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.category_settings_saved),
                        fontFamily = Fonts.Poppins,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            // Akcije Category Card
            CategoryConfigCard(
                title = stringResource(id = R.string.category_settings_actions),
                categoryId = uiState.actionsCategoryId,
                categoryLevel = uiState.actionsCategoryLevel,
                onCategoryIdChange = { newId ->
                    // Only allow numeric input
                    if (newId.all { it.isDigit() } || newId.isEmpty()) {
                        viewModel.updateActionsCategoryId(newId)
                    }
                },
                onCategoryLevelChange = { newLevel ->
                    // Only allow numeric input
                    if (newLevel.all { it.isDigit() } || newLevel.isEmpty()) {
                        viewModel.updateActionsCategoryLevel(newLevel)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Novo Category Card
            CategoryConfigCard(
                title = stringResource(id = R.string.category_settings_new_items),
                categoryId = uiState.newItemsCategoryId,
                categoryLevel = uiState.newItemsCategoryLevel,
                onCategoryIdChange = { newId ->
                    // Only allow numeric input
                    if (newId.all { it.isDigit() } || newId.isEmpty()) {
                        viewModel.updateNewItemsCategoryId(newId)
                    }
                },
                onCategoryLevelChange = { newLevel ->
                    // Only allow numeric input
                    if (newLevel.all { it.isDigit() } || newLevel.isEmpty()) {
                        viewModel.updateNewItemsCategoryLevel(newLevel)
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB50938)
                )
            ) {
                Text(
                    text = stringResource(id = R.string.category_settings_save),
                    fontFamily = Fonts.Poppins,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset Button
            Button(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF808080)
                )
            ) {
                Text(
                    text = stringResource(id = R.string.category_settings_reset),
                    fontFamily = Fonts.Poppins,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back Button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB50938)
                )
            ) {
                Text(
                    text = stringResource(id = R.string.category_settings_back),
                    fontFamily = Fonts.Poppins,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CategoryConfigCard(
    title: String,
    categoryId: String,
    categoryLevel: String,
    onCategoryIdChange: (String) -> Unit,
    onCategoryLevelChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                fontFamily = Fonts.Poppins,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Category ID Input
            Column {
                Text(
                    text = stringResource(id = R.string.category_settings_category_id),
                    fontFamily = Fonts.Poppins,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextField(
                    value = categoryId,
                    onValueChange = onCategoryIdChange,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color(0xFFB50938),
                        unfocusedIndicatorColor = Color(0xFFCCCCCC),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Category Level Input
            Column {
                Text(
                    text = stringResource(id = R.string.category_settings_category_level),
                    fontFamily = Fonts.Poppins,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextField(
                    value = categoryLevel,
                    onValueChange = onCategoryLevelChange,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color(0xFFB50938),
                        unfocusedIndicatorColor = Color(0xFFCCCCCC),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }
    }
}

