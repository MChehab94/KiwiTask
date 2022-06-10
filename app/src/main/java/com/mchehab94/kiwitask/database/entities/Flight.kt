package com.mchehab94.kiwitask.database.entities

import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mchehab94.kiwitask.database.entities.Country

@Entity
data class Flight(
    @PrimaryKey @SerializedName("id") val flightId: String,
    val cityCodeTo: String,
    val cityTo: String,
    @Embedded(prefix= "from") val countryFrom: Country,
    @Embedded(prefix = "to") val countryTo: Country,
    val price: Int,
    var currency: String = "EUR",
    val distance: Double,
    @SerializedName("dTime") val departureTime: Int,
    @SerializedName("dTimeUTC") val departureTimeUTC: Int,
    @SerializedName("aTime") val arrivalTime: Int,
    @SerializedName("aTimeUTC") val arrivalTimeUTC: Int,
    @SerializedName("fly_duration") val duration: String,
    @Expose var isFavorite: Boolean = false,
) {
    @Expose var imageId: String = ""
}
