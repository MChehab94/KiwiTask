package com.mchehab94.kiwitask.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mchehab94.kiwitask.utils.capitalized

@Entity
data class Airport(
    @PrimaryKey(autoGenerate = true) val airportId: Int = 0,
    @SerializedName("name") val name: String,
    @SerializedName("iata_code") val iataCode: String,
    @SerializedName("country_code") val countryCode: String,
    @SerializedName("city") val city: String,
) {
    override fun toString(): String {
        return "$name, ${city.capitalized()}"
    }
}
