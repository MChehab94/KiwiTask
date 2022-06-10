package com.mchehab94.kiwitask.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mchehab94.kiwitask.database.dao.AirportDao
import com.mchehab94.kiwitask.database.dao.CityDao
import com.mchehab94.kiwitask.database.dao.FlightDao
import com.mchehab94.kiwitask.database.entities.Flight
import com.mchehab94.kiwitask.model.FlightResponse
import com.mchehab94.kiwitask.model.LocationResponse
import com.mchehab94.kiwitask.model.SelectedCityFilter
import com.mchehab94.kiwitask.network.FlightApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class FlightViewModel @Inject constructor(
    private val flightApiService: FlightApiService,
    private val airportDao: AirportDao,
    private val flightDao: FlightDao,
    private val cityDao: CityDao
) : ViewModel() {

    val isNetworkRunning = mutableStateOf(false)
    val didRefresh = MutableLiveData<Boolean>()

    private val errorMessageFlow = MutableStateFlow<ResponseStatus?>(null)
    val errorMessage = errorMessageFlow.asLiveData()

    private val flightResponse = MutableLiveData<FlightResponse>()
    fun getKiwiResponse() = flightResponse

    private val locationResponse = MutableLiveData<LocationResponse>()
    fun getLocation() = locationResponse

    /**
     * Called whenever an API request is performed. [emit] is called once the [remainingTime] reaches 0, indicating
     * a timeout.
     */
    private val timeoutCoroutine = flow {
        val startTime = 15
        var remainingTime = startTime
        while (remainingTime > 0) {
            remainingTime--
            delay(1000)
        }
        emit(remainingTime)
    }

    init {
        getRetrofitFlights()
    }

    fun refresh() = viewModelScope.launch {
        if (!isNetworkRunning.value) {
            getRetrofitFlights()
            didRefresh.value = true
        }
    }

    /**
     * Sends an API request with the selected [cities] as destination. [cities] is mapped into a [List<List<String>>],
     * each element, [List<String>], contains all the airports (iata codes) in the city. The result is flattened and
     * converted into a comma separated String.
     *
     * @param cities
     */
    fun getRetrofitFlights(cities: ArrayList<SelectedCityFilter>) = viewModelScope.launch {
        isNetworkRunning.value = true
        val destinations = cities.map { airportDao.getAirportsIataInCity(it.city, it.countryCode) }
            .flatten().joinToString(",")
        getRetrofitFlights(destinations)
    }

    /**
     *Sends an API request to retrieve flights at the specified [destination].
     *Random (unvisited) cities are selected if the [destination] is empty, then all airport iata codes are represented
     * in a comma separated String.
     */
    fun getRetrofitFlights(destination: String = "") = viewModelScope.launch {
        isNetworkRunning.value = true
        val flyTo = destination.ifEmpty {
            val cities = cityDao.getRandomNewCities(50)
//            this can occur in two scenarios: 1- first run, database is empty. 2- all cities have been retrieved at least once
            if (cities.isEmpty()) {
//                returns 5 random locations using 'spain' as the term
                getRetrofitLocations()
                return@launch
            }

            cities.map { airportDao.getAirportsIataInCity(it.cityName, it.countryCode) }
                .flatten().joinToString(",")
        }

        val call = flightApiService.getFlights(flyFrom = "antalya_tr", flyTo = "airport:$flyTo")
        call.enqueue(object : Callback<FlightResponse> {
            override fun onResponse(call: Call<FlightResponse>, response: Response<FlightResponse>) {
                isNetworkRunning.value = false
                if (call.isCanceled) {
                    return
                }
                viewModelScope.launch {
                    if (response.body() == null || response.body()!!.data.isEmpty()) {
                        errorMessageFlow.emit(ResponseStatus.NoFlightsFound)
                        return@launch
                    }
                    val responseBody = response.body()!!
                    val citiesVisited = mutableListOf<String>()
                    responseBody.data.forEachIndexed { index, flight ->
                        responseBody.data[index].imageId = getLocation(flight.cityTo)!!
                        responseBody.data[index].currency = responseBody.currency
                        citiesVisited.add(flight.cityTo)
                    }
                    flightResponse.value = responseBody
                    for (i in flightResponse.value!!.data.indices) {
                        val flight = flightResponse.value!!.data[i]
                        if (flightDao.doesFlightExist(flight.flightId)) {
                            continue
                        }
                        flightDao.insert(flight)
                    }
                    cityDao.updateCitiesToVisited(citiesVisited)
                }
            }

            override fun onFailure(call: Call<FlightResponse>, t: Throwable) {
                viewModelScope.launch {
                    errorMessageFlow.emit(ResponseStatus.GenericError)
                    isNetworkRunning.value = false
                }
            }
        })
        timeoutCoroutine.collect {
            if (it <= 0 && isNetworkRunning.value) {
                call.cancel()
                isNetworkRunning.value = false
                errorMessageFlow.emit(ResponseStatus.NetworkTimeout)
            }
        }
    }

    /**
     * Sends an API request to retrieve 5 random destinations.
     */
    suspend fun getRetrofitLocations(): Job = viewModelScope.launch {
        val response = flightApiService.getLocations(term = "spain", limit = 5)
        if (response.isSuccessful) {
            response.body()?.let {
                val locations = it.locations
                val codes = locations.joinToString(",") { location -> location.id }
                getRetrofitFlights(codes)
            }
        }
    }

    /**
     * Helper method that retrieves the destination's image identifier.
     *@param term that represents the destination
     */
    suspend fun getLocation(term: String) = coroutineScope {
        val response = flightApiService.getLocations(term = term, limit = 1, locationTypes = "city")
        if (response.isSuccessful) {
            response.body()?.let {
                it.locations[0].id
            }
        } else {
            ""
        }
    }

    /**
     *Updates [flight] when the favorite icon is clicked.
     */
    fun onFavoriteClick(flight: Flight) {
        var isFavorite = false
        val updatedItems = flightResponse.value!!.data.map { f ->
            if (flight.flightId == f.flightId) {
                isFavorite = !f.isFavorite
                val updatedFlight = f.copy(isFavorite = !f.isFavorite)
                updatedFlight.imageId = flight.imageId
                updatedFlight
            } else {
                f
            }
        }
//        trigger observe in MainActivity
        flightResponse.value = flightResponse.value!!.copy(data = updatedItems)
//        reflect change in database
        viewModelScope.launch {
            if (isFavorite) {
                flightDao.addToFavorites(flight.flightId)
            } else {
                flightDao.removeFromFavorite(flight.flightId)
            }
        }
    }

    /**
     * Enum representing different possible statuses.
     * @property GenericError indicates an unknown error has occurred
     * @property NetworkTimeout indicates the request is taking too long to finish
     * @property NoFlightsFound indicates no data to display
     * @property NoError indicates a successful response
     */
    enum class ResponseStatus {
        GenericError, NetworkTimeout, NoFlightsFound, NoError
    }
}