package com.example.ryangallagher

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CurrentConditions(
    val weather: List<WeatherCondition>,
    val main: Currents,
    val name: String,
) : Parcelable

