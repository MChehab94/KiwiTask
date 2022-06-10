package com.mchehab94.kiwitask.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mchehab94.kiwitask.database.entities.Flight

data class FlightResponse(
    @Expose @SerializedName("search_id") val searchId: String,
    @Expose @SerializedName("time") val time: Int,
    @Expose @SerializedName("currency") val currency: String,
    @Expose @SerializedName("fx_rate") val fxRate: String,
    @Expose @SerializedName("data") var data: List<Flight>
)
