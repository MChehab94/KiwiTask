package com.mchehab94.kiwitask.di

import com.mchehab94.kiwitask.database.AppDatabase

import android.app.Application
import androidx.room.Room
import com.mchehab94.kiwitask.network.FlightApiService
import com.mchehab94.kiwitask.network.URLs
import com.mchehab94.kiwitask.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application, callback: AppDatabase.Callback) =
        Room.databaseBuilder(app, AppDatabase::class.java, Constants.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides
    fun provideCountriesDao(database: AppDatabase) = database.countryDao()

    @Provides
    fun provideFlatCityDao(database: AppDatabase) = database.flatCityDao()

    @Provides
    fun provideAirportDao(database: AppDatabase) = database.airportDao()

    @Provides
    fun provideFlightDao(database: AppDatabase) = database.flightDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Singleton
    @Provides
    fun providesOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(URLs.BASE_URL)
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): FlightApiService = retrofit.create(FlightApiService::class.java)
}

//this qualifier is visible through reflection, the qualifer annotation doesn't work without retention runtime
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
