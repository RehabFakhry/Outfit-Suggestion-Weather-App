package com.example.outfitsuggestionweatherapp.data

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("name") val name: String,
    @SerializedName("main") val main: WeatherDetails
    )

//data class ClothesItem(
//    val resourceId: Int
//)
////    var isUsed: Boolean = false,
////    val id: Int
//

