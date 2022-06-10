package com.mchehab94.kiwitask.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Country(
    @PrimaryKey(autoGenerate = true) val countryId: Int = 0,
    @ColumnInfo(name = "countryName") @SerializedName("name") val name: String,
    @ColumnInfo(name = "countryCode") @SerializedName("code") val code: String
) {
    override fun toString(): String {
        return name
    }
}