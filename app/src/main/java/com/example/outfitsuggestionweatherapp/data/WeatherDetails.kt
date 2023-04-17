package com.example.outfitsuggestionweatherapp.data

import com.google.gson.annotations.SerializedName

data class WeatherDetails(
    @SerializedName("temp") val temp: Float,
//    val weather: List<Weather>?
    )