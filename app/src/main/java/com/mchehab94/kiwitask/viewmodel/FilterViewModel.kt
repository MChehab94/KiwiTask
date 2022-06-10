package com.mchehab94.kiwitask.viewmodel

import androidx.lifecycle.*
import com.mchehab94.kiwitask.database.dao.AirportDao
import com.mchehab94.kiwitask.database.dao.CountryDao
import com.mchehab94.kiwitask.database.dao.CityDao
import com.mchehab94.kiwitask.database.entities.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FilterViewModel @Inject constructor(
    private val cityDao: CityDao,
) : ViewModel() {

    private val citiesFlow = cityDao.getAll()
    val cities = citiesFlow.asLiveData()

    val searchQuery =  MutableLiveData<String>()

    private val filteredCitiesFlow = searchQuery.asFlow().flatMapLatest { query ->
        cityDao.filter(query)
    }
    val filteredCities = filteredCitiesFlow.asLiveData()
}