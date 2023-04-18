package com.example.outfitsuggestionweatherapp.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.outfitsuggestionweatherapp.R
import com.example.outfitsuggestionweatherapp.data.source.ImageOutfits
import com.example.outfitsuggestionweatherapp.data.source.LottieAnimations
import com.example.outfitsuggestionweatherapp.data.weatherModel.WeatherDetails
import com.example.outfitsuggestionweatherapp.data.weatherModel.WeatherResponse
import com.example.outfitsuggestionweatherapp.databinding.ActivityMainBinding
import com.example.outfitsuggestionweatherapp.utils.Constants
import com.example.outfitsuggestionweatherapp.utils.ImagePreferenceManager
import com.google.android.gms.location.*
import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var imagePreferenceManager: ImagePreferenceManager
    private val locationRequest = LocationRequest().apply {
        interval = 10000
        fastestInterval = 5000
        LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onStart()
        getCurrentDateAndDay()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            @SuppressLint("SetTextI18n")
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation ?: return
                getWeatherData(
                    location.latitude, location.longitude
                ) { weatherResponse, exception ->
                    runOnUiThread {
                        updateUIWithWeatherDetails(weatherResponse, exception)
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                    } } } } }

    @SuppressLint("SetTextI18n")
    private fun updateUIWithWeatherDetails(
        weatherResponse: WeatherResponse?, exception: Exception?
    ) {
        if (exception != null) {
            binding.textViewTemperature.text = getString(R.string.failed_to_get_weather)
        } else {
            val cityName = weatherResponse?.name ?: "N/A"
            val temp = weatherResponse?.main?.temp ?: 0.0f
            val humidity = weatherResponse?.main?.humidity ?: 0
            val pressure = weatherResponse?.main?.pressure?.toInt()
            val feelsLike = weatherResponse?.main?.feelsLike ?: 0.0f
            clothesRecommendation(temp.toInt(), this@MainActivity)
            with(binding) {
                textViewTemperature.text = "$temp°C"
                textViewCityName.text = "$cityName"
                textViewFeelsLike.text = "$feelsLike\nFeels like"
                textViewPressure.text = "$pressure\n Pressure"
                textViewHumidity.text = "$humidity%\n Humidity"
            } } }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()
        checkLocationPermission()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkLocationPermission() {
        val permission = ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(permission), Constants.LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            ) } }

    private fun getWeatherData(
        latitude: Double, longitude: Double, callback: (WeatherResponse?, Exception?) -> Unit
    ) {
        val url =
            "${Constants.BASE_URL}$latitude&lon=$longitude" + "&appid=${Constants.API_KEY}&units=metric"
        val client = OkHttpClient.Builder().addInterceptor(
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            ).build()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e) }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody: ResponseBody? = response.body
                    if (responseBody != null) {
                        val json = responseBody.string()
                        val dataResult = Gson().fromJson(json, WeatherResponse::class.java)
                        val weatherResponse = parseWeatherResponse(dataResult)
                        callback(weatherResponse, null)
                    } else {
                        callback(null, Exception("Response body is null"))
                    }
                } catch (e: Exception) {
                    callback(null, e)
                } } }) }

    fun parseWeatherResponse(jsonData: WeatherResponse): WeatherResponse? {
        return try {
            val cityName = jsonData.name
            val temp = jsonData.main.temp
            val feelsLike = jsonData.main.feelsLike
            val pressure = jsonData.main.pressure
            val humidity = jsonData.main.humidity
            WeatherResponse(WeatherDetails(temp, feelsLike, pressure, humidity), cityName)
        } catch (e: Exception) {
            null  } }

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
