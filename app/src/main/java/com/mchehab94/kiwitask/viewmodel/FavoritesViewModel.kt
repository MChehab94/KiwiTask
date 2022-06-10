package com.mchehab94.kiwitask.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.mchehab94.kiwitask.database.dao.FlightDao
import com.mchehab94.kiwitask.database.entities.Flight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val flightDao: FlightDao
) : ViewModel() {

    val favoritesCount = flightDao.getFavoritesCount().asLiveData()
    val searchQuery =  MutableLiveData("")

    private val filteredFlights = searchQuery.asFlow().flatMapLatest { query ->
        flightDao.filterFavorites(searchQuery.value!!)
    }

    /**
     * Maps [filtered] data, grouping the data based on [countryName], sorted by key. Each key, [countryNam]
     * has a [List<Flight>], representing destinations
     */
    val filteredGroupedByCountry = filteredFlights.map { flights ->
        flights.groupBy { it.countryTo.name }.toSortedMap()
    }.asLiveData()
}
