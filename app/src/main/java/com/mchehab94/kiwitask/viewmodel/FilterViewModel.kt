package com.mchehab94.kiwitask.viewmodel

import androidx.lifecycle.*
import com.mchehab94.kiwitask.database.dao.AirportDao
import com.mchehab94.kiwitask.database.dao.CountryDao
import com.mchehab94.kiwitask.database.dao.CityDao
import com.mchehab94.kiwitask.database.entities.City
import com.mchehab94.kiwitask.database.entities.Country
import com.mchehab94.kiwitask.model.SelectedCityFilter
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

    private val _selectedCities = MutableLiveData<MutableList<City>>(mutableListOf())
    val selectedCities = _selectedCities
    var didAdd = false

    fun addCity(city: City) {
        if (!_selectedCities.value!!.contains(city)) {
            city.isVisible = true
            _selectedCities.value!!.add(city)
//            trigger observe
            _selectedCities.value = _selectedCities.value
            didAdd = true
        }
    }

    fun removeCity(city: City) {
        _selectedCities.value?.filter { it.cityId == city.cityId }?.forEach { _selectedCities.value?.remove(it) }
        _selectedCities.value = _selectedCities.value
        didAdd = false
    }

    fun canApplySearch(): Boolean {
        return _selectedCities.value!!.isNotEmpty()
    }

    fun getSelectedCities(): ArrayList<SelectedCityFilter> {
        return selectedCities.value!!.map { SelectedCityFilter(it.cityName, it.countryCode) } as ArrayList<SelectedCityFilter>
    }
}