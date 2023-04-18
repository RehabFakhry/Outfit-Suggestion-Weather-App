package com.example.outfitsuggestionweatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.outfitsuggestionweatherapp.data.source.WeatherDataFetcher
import com.example.outfitsuggestionweatherapp.utils.Constants
import com.google.android.gms.location.*

class LocationManager (private val context: Context, private val weatherDataFetcher: WeatherDataFetcher) {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation ?: return
                weatherDataFetcher.getWeatherData(
                    location.latitude, location.longitude
                ) { weatherResponse, exception ->
                    (context as MainActivity).runOnUiThread {
                        (context as MainActivity).updateUIWithWeatherDetails(weatherResponse, exception)
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                    }
                }
            }
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }
}