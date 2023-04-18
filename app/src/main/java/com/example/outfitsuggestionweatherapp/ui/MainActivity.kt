package com.example.outfitsuggestionweatherapp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.outfitsuggestionweatherapp.R
import com.example.outfitsuggestionweatherapp.data.source.ImageOutfits
import com.example.outfitsuggestionweatherapp.data.source.LottieAnimations
import com.example.outfitsuggestionweatherapp.data.source.WeatherDataFetcher
import com.example.outfitsuggestionweatherapp.data.weatherModel.WeatherResponse
import com.example.outfitsuggestionweatherapp.databinding.ActivityMainBinding
import com.example.outfitsuggestionweatherapp.utils.ImagePreferenceManager
import com.google.android.gms.location.*
import okhttp3.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imagePreferenceManager: ImagePreferenceManager
    private lateinit var weatherDataFetcher: WeatherDataFetcher
    private lateinit var locationManager: LocationManager

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onStart()
        getCurrentDateAndDay()
        weatherDataFetcher = WeatherDataFetcher()
        locationManager = LocationManager(this, WeatherDataFetcher())
    }

    override fun onStart() {
        super.onStart()
        locationManager.startLocationUpdates()
    }

    @SuppressLint("SetTextI18n")
    fun updateUIWithWeatherDetails(
        weatherResponse: WeatherResponse?, exception: Exception?
    ) {
        if (exception != null) {
            binding.textViewTemperature.text = getString(R.string.failed_to_get_weather)
        } else {
            val cityName = weatherResponse?.name ?: "N/A"
            val temp = weatherResponse?.main?.temp ?: 0.0f
            val humidity = weatherResponse?.main?.humidity ?: 0
            val pressure = weatherResponse?.main?.pressure?.times(1.0)
            val feelsLike = weatherResponse?.main?.feelsLike ?: 0.0f
            clothesRecommendation(temp.toInt(), this@MainActivity)
            with(binding) {
                textViewTemperature.text = "$temp°C"
                textViewCityName.text = "$cityName"
                textViewFeelsLike.text = "$feelsLike\nFeels like"
                textViewPressure.text = "${pressure} mmHg\n Pressure"
                textViewHumidity.text = "$humidity%\n Humidity"
            }
        }
    }

    private fun clothesRecommendation(temperature: Int, context: Context) {
        val outfitList = when {
            (temperature < 0 || temperature in 0..20) -> {
                binding.textViewDescription.text = getString(R.string.cold_weather)
                LottieAnimations.winterAnimation
                ImageOutfits.winterOutfits
            }
            temperature in 20..30 -> {
                binding.textViewDescription.text = getString(R.string.autumn_weather)
                LottieAnimations.autumnAnimation
                ImageOutfits.autumnOutfits
            }
            temperature in 30..35 -> {
                binding.textViewDescription.text = getString(R.string.sunny_weather)
                LottieAnimations.springAnimation
                ImageOutfits.springOutfits
            }
            temperature in 35..40 -> {
                binding.textViewDescription.text = getString(R.string.sunny_weather)
                LottieAnimations.summerAnimation
                ImageOutfits.summerOutfits
            }
            else -> {
                Toast.makeText(context, "Out of options", Toast.LENGTH_LONG).show()
            }
        }
        imagePreferenceManager = ImagePreferenceManager(this)
        imagePreferenceManager.saveImages(outfitList as List<Int>)
        val randomImage = imagePreferenceManager.getRandomImage()
        if (randomImage != null) binding.imageViewOutfit.setImageResource(randomImage)
        else binding.imageViewOutfit.setImageResource(R.drawable.img_1)
    }

    private fun getCurrentDateAndDay() {
        binding.textViewCurrentDate.text =
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        binding.textViewCurrentDay.text =
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"))
    }
}
