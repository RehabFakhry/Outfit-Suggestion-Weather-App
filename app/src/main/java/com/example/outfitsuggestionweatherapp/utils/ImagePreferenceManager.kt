package com.example.outfitsuggestionweatherapp.utils

import android.content.Context
import android.content.SharedPreferences

class ImagePreferenceManager(context: Context) {
    companion object {
        private const val PREF_NAME = "image_pref"
        private const val KEY_IMAGES = "images"
//        private const val KEY_DATE = "date"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )

    fun saveImages(images: List<Int>) {
        val imageIds = images.joinToString(",") { it.toString() }
        sharedPreferences.edit().putString(KEY_IMAGES, imageIds).apply()
//        sharedPreferences.edit().putLong(KEY_DATE , System.currentTimeMillis()).apply()
    }

    fun getRandomImage(): Int? {
        val images = getSavedImages()
        return if (images.isNotEmpty()) {
            images.random()
        } else null
    }

    private fun getSavedImages(): List<Int> {
        val savedImagesString = sharedPreferences.getString(KEY_IMAGES, "")
        return savedImagesString?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }
}

//    fun getRandomImage(): Int? {
//        val images = getSavedImages()
//        val usedImagesIds = sharedPreferences.getString(KEY_IMAGES, "")?.
//        split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
//        val currentDate = System.currentTimeMillis()
//
//        // If today's date is different from the saved date, reset used images
//        if (!isSameDay(currentDate, sharedPreferences.getLong(KEY_DATE, 0))) {
//            sharedPreferences.edit().putString(KEY_IMAGES, "").apply()
//        }
//
//        // Filter out used images
//        val availableImages = images.filterNot { usedImagesIds.contains(it)}
//
//        return if (availableImages.isNotEmpty()) {
//            availableImages.random()
//        }
//        else null
//    }
//
//    // Helper method to check if two timestamps represent the same day
//    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
//        val calender1 = Calendar.getInstance()
//        val calender2 = Calendar.getInstance()
//        calender1.timeInMillis = timestamp1
//        calender2.timeInMillis = timestamp2
//        return calender1.get(Calendar.YEAR) == calender2.get(Calendar.YEAR) &&
//                calender1.get(Calendar.DAY_OF_YEAR) == calender2.get(Calendar.DAY_OF_YEAR)
//    }
