package com.mchehab94.kiwitask.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedCityFilter(
    val city: String,
    val countryCode: String,
) : Parcelable