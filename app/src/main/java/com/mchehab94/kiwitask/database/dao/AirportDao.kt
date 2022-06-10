package com.mchehab94.kiwitask.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.mchehab94.kiwitask.database.entities.Airport
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    @Insert
    fun insert(airport: Airport)

    @Insert
    @Transaction
    fun insertAll(airports: List<Airport>)

    @Query("SELECT * FROM Airport WHERE :iataCode == iataCode")
    fun getAirport(iataCode: String): Flow<Airport>

    @Query("SELECT iataCode FROM Airport WHERE UPPER(city) = UPPER(:cityName) AND UPPER(countryCode) = UPPER(:countryCode)")
    suspend fun getAirportsIataInCity(cityName: String, countryCode: String): List<String>
}