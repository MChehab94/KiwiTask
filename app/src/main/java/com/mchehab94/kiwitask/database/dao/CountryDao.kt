package com.mchehab94.kiwitask.database.dao

import androidx.room.*
import com.mchehab94.kiwitask.database.entities.Country
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {
    @Insert
    fun insert(country: Country)

    @Transaction
    @Insert
    fun insertAll(countries: List<Country>)

    @Query("DELETE FROM Country")
    fun deleteAll()
}