package com.example.outfitsuggestionweatherapp.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceManager(context: Context) {
    companion object {
        private const val PREF_NAME = "image_pref"
        private const val KEY_IMAGES = "images"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )

    fun saveImages(images: List<Int>) {
        val imageIds = images.joinToString(",") { it.toString() }
        sharedPreferences.edit().putString(KEY_IMAGES, imageIds).apply()
    }

    private fun getSavedImages(): List<Int> {
        val savedImagesString = sharedPreferences.getString(KEY_IMAGES, "")
        return savedImagesString?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }

    fun getRandomImage(): Int? {
        val images = getSavedImages()
        return if (images.isNotEmpty()) {
            images.random()
        } else null
    }
}
