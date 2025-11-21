package com.fashiontothem.ff.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fashiontothem.ff.data.local.preferences.CategoryPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * F&F Tothem - Category Settings ViewModel
 */
@HiltViewModel
class CategorySettingsViewModel @Inject constructor(
    private val categoryPreferences: CategoryPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategorySettingsUiState())
    val uiState: StateFlow<CategorySettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentValues()
    }
    
    private fun loadCurrentValues() {
        viewModelScope.launch {
            val actionsId = categoryPreferences.actionsCategoryId.first()
            val actionsLevel = categoryPreferences.actionsCategoryLevel.first()
            val newItemsId = categoryPreferences.newItemsCategoryId.first()
            val newItemsLevel = categoryPreferences.newItemsCategoryLevel.first()
            
            _uiState.value = _uiState.value.copy(
                actionsCategoryId = actionsId,
                actionsCategoryLevel = actionsLevel,
                newItemsCategoryId = newItemsId,
                newItemsCategoryLevel = newItemsLevel
            )
        }
    }
    
    fun updateActionsCategoryId(id: String) {
        _uiState.value = _uiState.value.copy(actionsCategoryId = id)
    }
    
    fun updateActionsCategoryLevel(level: String) {
        _uiState.value = _uiState.value.copy(actionsCategoryLevel = level)
    }
    
    fun updateNewItemsCategoryId(id: String) {
        _uiState.value = _uiState.value.copy(newItemsCategoryId = id)
    }
    
    fun updateNewItemsCategoryLevel(level: String) {
        _uiState.value = _uiState.value.copy(newItemsCategoryLevel = level)
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            categoryPreferences.saveActionsCategory(
                categoryId = state.actionsCategoryId,
                categoryLevel = state.actionsCategoryLevel
            )
            categoryPreferences.saveNewItemsCategory(
                categoryId = state.newItemsCategoryId,
                categoryLevel = state.newItemsCategoryLevel
            )
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
    
    fun resetToDefaults() {
        viewModelScope.launch {
            categoryPreferences.resetToDefaults()
            loadCurrentValues()
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
    
    fun clearSavedState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}

data class CategorySettingsUiState(
    val actionsCategoryId: String = "630",
    val actionsCategoryLevel: String = "2",
    val newItemsCategoryId: String = "223",
    val newItemsCategoryLevel: String = "3",
    val isSaved: Boolean = false
)

