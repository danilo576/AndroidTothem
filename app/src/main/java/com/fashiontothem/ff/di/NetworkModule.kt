package com.fashiontothem.ff.di

import com.fashiontothem.ff.data.remote.ApiService
import com.fashiontothem.ff.data.remote.adapters.ProductOptionsJsonAdapter
import com.fashiontothem.ff.data.remote.JsonPrettyPrintInterceptor
import com.fashiontothem.ff.data.remote.auth.AthenaAuthInterceptor
import com.fashiontothem.ff.data.remote.auth.OAuth1Interceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * F&F Tothem - Network Module
 * 
 * Hilt module for providing network-related dependencies for Fashion & Friends API.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // Base URLs imported from Constants
    private const val BASE_URL = com.fashiontothem.ff.util.Constants.FASHION_API_BASE_URL
    
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(ProductOptionsJsonAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()
    
    /**
     * Moshi for Athena API with custom null serialization
     * Skips null values to avoid sending empty strings
     */
    @Provides
    @Singleton
    @Named("AthenaMoshi")
    fun provideAthenaMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = 
        HttpLoggingInterceptor().apply {
            // HEADERS level - shows request/response headers but not body
            // Body will be pretty printed by JsonPrettyPrintInterceptor
            level = HttpLoggingInterceptor.Level.HEADERS
        }
    
    @Provides
    @Singleton
    fun provideJsonPrettyPrintInterceptor(): JsonPrettyPrintInterceptor = 
        JsonPrettyPrintInterceptor()
    
    @Provides
    @Singleton
    fun provideOAuth1Interceptor(): OAuth1Interceptor {
        return OAuth1Interceptor(
            consumerKey = humer.UvcCamera.BuildConfig.OAUTH_CONSUMER_KEY,
            consumerSecret = humer.UvcCamera.BuildConfig.OAUTH_CONSUMER_SECRET,
            accessToken = humer.UvcCamera.BuildConfig.OAUTH_ACCESS_TOKEN,
            tokenSecret = humer.UvcCamera.BuildConfig.OAUTH_TOKEN_SECRET
        )
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        jsonPrettyPrintInterceptor: JsonPrettyPrintInterceptor,
        oAuth1Interceptor: OAuth1Interceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(oAuth1Interceptor)  // OAuth1 first
        .addInterceptor(jsonPrettyPrintInterceptor)  // Pretty print JSON responses
        .addInterceptor(loggingInterceptor)  // Then logging
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .cache(null)
        .build()
    
    @Provides
    @Singleton
    @Named("FashionAndFriends")
    fun provideFashionRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    
    @Provides
    @Singleton
    fun provideApiService(
        @Named("FashionAndFriends") retrofit: Retrofit
    ): ApiService = retrofit.create(ApiService::class.java)
    
    @Provides
    @Singleton
    fun provideAthenaAuthInterceptor(
        athenaPreferences: com.fashiontothem.ff.data.local.preferences.AthenaPreferences,
        athenaTokenManager: com.fashiontothem.ff.data.manager.AthenaTokenManager
    ): AthenaAuthInterceptor = AthenaAuthInterceptor(athenaPreferences, athenaTokenManager)
    
    /**
     * Athena Search API - Dynamic client with Bearer token
     * Base URL is determined by athenaSearchWebsiteUrl from store config
     */
    @Provides
    @Singleton
    @Named("Athena")
    fun provideAthenaRetrofit(
        loggingInterceptor: HttpLoggingInterceptor,
        athenaAuthInterceptor: AthenaAuthInterceptor,
        @Named("AthenaMoshi") moshi: Moshi
    ): Retrofit {
        // Athena API client with Bearer token
        val athenaClient = OkHttpClient.Builder()
            .addInterceptor(athenaAuthInterceptor)  // Auto-add Bearer token
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
        
        // Configure Moshi to not serialize null values
        val moshiConverter = MoshiConverterFactory.create(moshi).asLenient()
        
        return Retrofit.Builder()
            .baseUrl(com.fashiontothem.ff.util.Constants.ATHENA_DEFAULT_BASE_URL) // Fallback URL
            .client(athenaClient)
            .addConverterFactory(moshiConverter)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAthenaApiService(
        @Named("Athena") retrofit: Retrofit
    ): com.fashiontothem.ff.data.remote.AthenaApiService =
        retrofit.create(com.fashiontothem.ff.data.remote.AthenaApiService::class.java)
    
    /**
     * Dynamic Athena API Service that uses athenaSearchWebsiteUrl from store config
     * This service is created at runtime with the correct base URL
     */
    @Provides
    @Singleton
    fun provideDynamicAthenaApiService(
        athenaPreferences: com.fashiontothem.ff.data.local.preferences.AthenaPreferences,
        loggingInterceptor: HttpLoggingInterceptor,
        jsonPrettyPrintInterceptor: JsonPrettyPrintInterceptor,
        athenaAuthInterceptor: AthenaAuthInterceptor,
        moshi: Moshi
    ): com.fashiontothem.ff.data.remote.DynamicAthenaApiService {
        return com.fashiontothem.ff.data.remote.DynamicAthenaApiService(
            athenaPreferences = athenaPreferences,
            loggingInterceptor = loggingInterceptor,
            jsonPrettyPrintInterceptor = jsonPrettyPrintInterceptor,
            athenaAuthInterceptor = athenaAuthInterceptor,
            moshi = moshi
        )
    }
}