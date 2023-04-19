package com.example.outfitsuggestionweatherapp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.outfitsuggestionweatherapp.R
import com.example.outfitsuggestionweatherapp.data.source.DataManager
import com.example.outfitsuggestionweatherapp.data.source.WeatherDataResponse
import com.example.outfitsuggestionweatherapp.data.weatherModel.WeatherResponse
import com.example.outfitsuggestionweatherapp.databinding.ActivityMainBinding
import com.example.outfitsuggestionweatherapp.utils.SharedPreferenceManager
import com.google.android.gms.location.*
import okhttp3.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private lateinit var weatherDataFetcher: WeatherDataResponse
    private lateinit var locationManager: LocationManager

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onStart()
        getCurrentDateAndDay()
        weatherDataFetcher = WeatherDataResponse()
        locationManager = LocationManager(this, WeatherDataResponse())
    }

    override fun onStart() {
        super.onStart()
        locationManager.startLocationUpdates()
    }

    @SuppressLint("SetTextI18n")
    fun updateUIWithWeatherDetails(
        weatherResponse: WeatherResponse?,
        exception: Exception?
    ) {
        if (exception != null) {
            binding.textViewTemperature.text = getString(R.string.failed_to_get_weather)
        } else {
            val cityName = weatherResponse?.name ?: "N/A"
            val temp = weatherResponse?.main?.temp ?: 0.0f
            val humidity = weatherResponse?.main?.humidity ?: 0
            val pressure = weatherResponse?.main?.pressure?.times(1.0)?: 0.0
            val feelsLike = weatherResponse?.main?.feelsLike ?: 0.0f
            clothesRecommendation(temp.toInt(), this@MainActivity)
            with(binding) {
                textViewTemperature.text = "$temp°C"
                textViewCityName.text = "$cityName"
                textViewFeelsLike.text = "$feelsLike °C\nFeels like"
                textViewPressure.text = "$pressure Pca\n Pressure"
                textViewHumidity.text = "$humidity%\n Humidity"
            }
        }
    }

    private fun clothesRecommendation(temperature: Int, context: Context) {
        val outfitList = when {
            (temperature < zeroTemperature ||
                    temperature in zeroTemperature..smallTemperature) -> {
                binding.textViewDescription.text = getString(R.string.cold_weather)
                binding.lottieWeather.setAnimation(R.raw.winter)
                DataManager.winterOutfits
            }
            temperature in smallTemperature..mediumTemperature -> {
                binding.textViewDescription.text = getString(R.string.autumn_weather)
                binding.lottieWeather.setAnimation(R.raw.autumn)
                DataManager.autumnOutfits
            }
            temperature in mediumTemperature..highTemperature -> {
                binding.textViewDescription.text = getString(R.string.hot_weather)
                binding.lottieWeather.setAnimation(R.raw.spring)
                DataManager.springOutfits
            }
            temperature in highTemperature..veryHighTemperature -> {
                binding.textViewDescription.text = getString(R.string.hot_weather)
                binding.lottieWeather.setAnimation(R.raw.sunny)
                DataManager.summerOutfits
            }
            else -> Toast.makeText(context, "Out of options", Toast.LENGTH_LONG).show()
        }
        sharedPreferenceManager = SharedPreferenceManager(this)
        sharedPreferenceManager.saveImages(outfitList as List<Int>)
        val randomImage = sharedPreferenceManager.getRandomImage()
        if (randomImage != null) binding.imageViewOutfit.setImageResource(randomImage)
        else binding.imageViewOutfit.setImageResource(R.drawable.img_1)
    }

    private fun getCurrentDateAndDay() {
        binding.textViewCurrentDate.text =
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        binding.textViewCurrentDay.text =
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"))
    }

    companion object{
        const val zeroTemperature = 0
        const val smallTemperature = 20
        const val mediumTemperature = 30
        const val highTemperature = 35
        const val veryHighTemperature = 40
    }
}
