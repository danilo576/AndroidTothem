package com.fashiontothem.ff.domain.usecase

import com.fashiontothem.ff.domain.repository.StoreRepository
import javax.inject.Inject

/**
 * F&F Tothem - Save Selected Store Use Case
 */
class SaveSelectedStoreUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(
        storeCode: String, 
        countryCode: String, 
        locale: String? = null,
        secureBaseMediaUrl: String? = null
    ) {
        repository.saveSelectedStore(storeCode, countryCode, locale, secureBaseMediaUrl)
    }
}

