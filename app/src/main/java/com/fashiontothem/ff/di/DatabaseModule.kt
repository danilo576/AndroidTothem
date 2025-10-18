package com.fashiontothem.ff.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * F&F Tothem - Database Module
 * 
 * Hilt module for providing Room database dependencies.
 * Database name: ff_tothem_database
 * 
 * TODO: Uncomment and implement when Room database is needed:
 * 
 * 1. Add Room dependencies to app/build.gradle
 * 2. Create AppDatabase class with entities
 * 3. Create DAOs (e.g., FashionImageDao)
 * 4. Uncomment provider methods below
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // Database providers will go here when implemented
}

