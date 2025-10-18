package com.fashiontothem.ff.domain.usecase

import com.fashiontothem.ff.domain.model.CountryStore
import com.fashiontothem.ff.domain.repository.StoreRepository
import javax.inject.Inject

/**
 * F&F Tothem - Get Store Configurations Use Case
 */
class GetStoreConfigsUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(): Result<List<CountryStore>> {
        return repository.getStoreConfigs()
    }
}

