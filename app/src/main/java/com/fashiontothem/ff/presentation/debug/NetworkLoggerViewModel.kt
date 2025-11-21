package com.fashiontothem.ff.presentation.debug

import androidx.lifecycle.ViewModel
import com.fashiontothem.ff.util.NetworkLoggerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NetworkLoggerViewModel @Inject constructor(
    val networkLoggerManager: NetworkLoggerManager
) : ViewModel()

