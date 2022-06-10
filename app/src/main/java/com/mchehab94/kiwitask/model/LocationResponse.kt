package com.mchehab94.kiwitask.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @Expose @SerializedName("locations") val locations: List<Location>,
    var flightId: String = "",
    var index: Int = 0
)