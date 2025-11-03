package com.fashiontothem.ff.di

import com.fashiontothem.ff.data.repository.AnalyticsRepositoryImpl
import com.fashiontothem.ff.data.repository.LocationRepositoryImpl
import com.fashiontothem.ff.data.repository.ProductRepositoryImpl
import com.fashiontothem.ff.data.repository.StoreRepositoryImpl
import com.fashiontothem.ff.domain.repository.AnalyticsRepository
import com.fashiontothem.ff.domain.repository.LocationRepository
import com.fashiontothem.ff.domain.repository.ProductRepository
import com.fashiontothem.ff.domain.repository.StoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * F&F Tothem - Repository Module
 * 
 * Hilt module for binding repository interfaces to implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindStoreRepository(
        storeRepositoryImpl: StoreRepositoryImpl
    ): StoreRepository
    
    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository
    
    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository
    
    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        analyticsRepositoryImpl: AnalyticsRepositoryImpl
    ): AnalyticsRepository
}

