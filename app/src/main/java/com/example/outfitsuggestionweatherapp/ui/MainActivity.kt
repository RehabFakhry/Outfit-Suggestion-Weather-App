package com.example.outfitsuggestionweatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.outfitsuggestionweatherapp.R
import com.example.outfitsuggestionweatherapp.data.WeatherDetails
import com.example.outfitsuggestionweatherapp.data.WeatherResponse
import com.example.outfitsuggestionweatherapp.databinding.ActivityMainBinding
import com.example.outfitsuggestionweatherapp.utils.ImagePreferenceManager
import com.google.android.gms.location.*
import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1234
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

        imagePreferenceManager = ImagePreferenceManager(this)

        val images = listOf(
            R.drawable.image_1,
            R.drawable.image_2,
            R.drawable.image_4,
            R.drawable.image_8
        )
        imagePreferenceManager.saveImages(images)
        val randomImage = imagePreferenceManager.getRandomImage()

        if (randomImage != null)
            binding.imageViewOutfit.setImageResource(randomImage)
        else
            binding.imageViewOutfit.setImageResource(R.drawable.img_1)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            @SuppressLint("SetTextI18n")
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation ?: return
                getWeatherData(
                    location.latitude,
                    location.longitude
                ) { weatherResponse, exception ->
                    runOnUiThread {
                        if (exception != null) {
                            // Handle error
                            binding.textView.text = getString(R.string.failed_to_get_weather)
                        } else {
                            // Update UI with weather details
                            val cityName = weatherResponse?.name ?: "N/A"
                            val temp = weatherResponse?.main?.temp ?: 0.0f
                            binding.textView.text = "$cityName\n $temp°C"
                        }
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                    } } } } }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()
        checkLocationPermission()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkLocationPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it at runtime
            ActivityCompat.requestPermissions(
                this, arrayOf(permission), LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted, start requesting location updates
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun getWeatherData(
        latitude: Double, longitude: Double, callback: (WeatherResponse?, Exception?) -> Unit
    ) {
        val url ="$Baseurl$latitude&lon=$longitude&appid=$apiKey&units=metric"
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            // Optional: for logging API requests
            .build()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody: ResponseBody? = response.body
                if (responseBody != null) {
                    val json = responseBody.string()
                    val dataResult = Gson().fromJson(json, WeatherResponse::class.java)
                    val weatherResponse = parseWeatherResponse(dataResult)
                    callback(weatherResponse, null)
                } } }) }

    fun parseWeatherResponse(jsonData: WeatherResponse): WeatherResponse? {
        return try {
            val cityName = jsonData.name
            val temp = jsonData.main.temp
            WeatherResponse(cityName, WeatherDetails(temp))
        } catch (e: Exception) {
            null
        }
    }

    companion object{
        val apiKey = "545c025aec18c326d80f41ac17e459b0"
        val Baseurl =
            "https://api.openweathermap.org/data/2.5/weather?lat="
    }
}