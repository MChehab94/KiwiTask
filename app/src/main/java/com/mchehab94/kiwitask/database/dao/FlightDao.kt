package com.mchehab94.kiwitask.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.mchehab94.kiwitask.database.entities.Country
import com.mchehab94.kiwitask.database.entities.Flight
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {

    @Insert
    suspend fun insert(flight: Flight)

    @Transaction
    @Insert
    suspend fun insertAll(flights: List<Flight>)

    @Query("SELECT * FROM Flight")
    fun getAll(): Flow<List<Flight>>

    @Query("SELECT * FROM Flight WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<Flight>>

    @Query("SELECT COUNT(*) FROM Flight WHERE isFavorite = 1")
    fun getFavoritesCount(): Flow<Int>

    @Query("UPDATE Flight SET isFavorite = 1 WHERE flightId = :flightId")
    suspend fun addToFavorites(flightId: String)

    @Query("UPDATE Flight SET isFavorite = 0 WHERE flightId = :flightId")
    suspend fun removeFromFavorite(flightId: String)

    @Query("SELECT * FROM Flight WHERE isFavorite = 1 AND (UPPER(tocountryName) LIKE '%' || :query || '%' OR UPPER(cityTo) LIKE '%' || :query || '%' OR UPPER(tocountryCode) LIKE '%' || :query || '%')")
    fun filterFavorites(query: String): Flow<List<Flight>>

    @Query("SELECT EXISTS(SELECT * FROM Flight WHERE flightId = :flightId)")
    suspend fun doesFlightExist(flightId: String): Boolean
}