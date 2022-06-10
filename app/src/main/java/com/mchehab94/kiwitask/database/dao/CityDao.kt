package com.mchehab94.kiwitask.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.mchehab94.kiwitask.database.entities.City
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Insert
    fun insert(city: City)

    @Insert
    @Transaction
    fun insertAll(cities: List<City>)

    @Query("SELECT * FROM City")
    fun getAll(): Flow<List<City>>

    @Query("SELECT * FROM City where UPPER(cityName) LIKE '%' || UPPER(:text) || '%'ORDER BY countryCode ASC LIMIT 20")
    fun filter(text: String): Flow<List<City>>

    @Query("SELECT * FROM City WHERE didVisit = 0 ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomNewCities(count: Int): List<City>

    @Query("UPDATE City SET didVisit = 1 WHERE UPPER(cityName) = UPPER(:cityName)")
    suspend fun updateCityToVisited(cityName: String)

    @Transaction
    suspend fun updateCitiesToVisited(cityNames: List<String>) {
        for (name in cityNames) {
            updateCityToVisited(name)
        }
    }
}