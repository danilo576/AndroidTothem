package com.fashiontothem.ff.di

import com.fashiontothem.ff.data.remote.ApiService
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
import javax.inject.Singleton

/**
 * F&F Tothem - Network Module
 * 
 * Hilt module for providing network-related dependencies for Fashion & Friends API.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "https://www.fashionandfriends.com/rest/V1/mobile/"
    
    // TODO: Add these to gradle.properties or BuildConfig:
    // OAUTH_CONSUMER_KEY=your_consumer_key
    // OAUTH_CONSUMER_SECRET=your_consumer_secret
    // OAUTH_ACCESS_TOKEN=your_access_token
    // OAUTH_TOKEN_SECRET=your_token_secret
    
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = 
        HttpLoggingInterceptor().apply {
            // BODY logging for debug, NONE for production (performance)
            level = HttpLoggingInterceptor.Level.BODY
        }
    
    @Provides
    @Singleton
    fun provideOAuth1Interceptor(): OAuth1Interceptor {
        // TODO: Replace with actual credentials from BuildConfig or gradle.properties
        // For now using placeholder values - MUST be replaced!
        return OAuth1Interceptor(
            consumerKey = "YOUR_CONSUMER_KEY",  // TODO: Replace
            consumerSecret = "YOUR_CONSUMER_SECRET",  // TODO: Replace
            accessToken = "YOUR_ACCESS_TOKEN",  // TODO: Replace
            tokenSecret = "YOUR_TOKEN_SECRET"  // TODO: Replace
        )
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        oAuth1Interceptor: OAuth1Interceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(oAuth1Interceptor)  // OAuth1 first
        .addInterceptor(loggingInterceptor)  // Then logging
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .cache(null)
        .build()
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}

