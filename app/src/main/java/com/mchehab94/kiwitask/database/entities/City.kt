package com.mchehab94.kiwitask.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class City(
    @PrimaryKey(autoGenerate = true) val cityId:Int = 0,
    @SerializedName("name") val cityName: String,
    @SerializedName("country_code") val countryCode: String,
    var didVisit: Boolean = false
) {
    override fun toString(): String {
        return "${cityName}, (${countryCode})"
    }
}
