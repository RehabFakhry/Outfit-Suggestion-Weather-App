package com.example.outfitsuggestionweatherapp.data.weatherModel

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("main") val main: WeatherDetails,
    @SerializedName("name") val name: String,
)
