package com.mchehab94.kiwitask.network

import com.mchehab94.kiwitask.model.FlightResponse
import com.mchehab94.kiwitask.model.LocationResponse
import com.mchehab94.kiwitask.utils.Constants
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface FlightApiService {

    @GET("/flights")
    fun getFlights(
        @Query("fly_from") flyFrom: String,
        @Query("fly_to") flyTo: String,
        @Query("partner") partner: String = Constants.PARTNER,
        @Query("limit") limit: Int = Constants.FLIGHTS_LIMIT,
        @Query("one_for_city") oneForCity: Int = Constants.ONE_FOR_CITY,
    ): Call<FlightResponse>

    @GET("/locations")
    suspend fun getLocations(
        @Query("term") term: String,
        @Query("limit") limit: Int,
        @Query("location_types") locationTypes: String = "airport"
    ): Response<LocationResponse>

    companion object {
        private var flightApiService: FlightApiService? = null
        fun getInstance(): FlightApiService {
            if (flightApiService == null) {
                flightApiService = Retrofit.Builder()
                    .baseUrl(URLs.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(FlightApiService::class.java)
            }
            return flightApiService!!
        }
    }

}