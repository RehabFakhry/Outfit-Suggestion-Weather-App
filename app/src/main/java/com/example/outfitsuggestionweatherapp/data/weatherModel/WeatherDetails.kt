package com.example.outfitsuggestionweatherapp.data.weatherModel

import com.google.gson.annotations.SerializedName

data class WeatherDetails(
    @SerializedName("temp") val temp: Float,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int
    )