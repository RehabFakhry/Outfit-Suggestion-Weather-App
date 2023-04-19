package com.example.outfitsuggestionweatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.outfitsuggestionweatherapp.data.source.WeatherDataResponse
import com.example.outfitsuggestionweatherapp.utils.Constants
import com.google.android.gms.location.*

class LocationManager (private val context: Context, private val weatherDataResponse: WeatherDataResponse) {

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
                weatherDataResponse.getWeatherData(
                    location.latitude, location.longitude,
                    onResponse = { weatherResponse ->
                        (context as MainActivity).runOnUiThread {
                            (context as MainActivity).updateUIWithWeatherDetails(
                                weatherResponse,
                                null
                            )
                            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                        }
                    },
                    onFailure = { exception ->
                        (context as MainActivity).runOnUiThread {
                            (context as MainActivity)
                                .updateUIWithWeatherDetails(null, exception)
                            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                        }
                    }
                )
            }
        }
        checkPermission()
    }

    private fun checkPermission() {
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


// make alert dialog to handel if permissions didn't granted
//            AlertDialog.Builder(context)
//                .setTitle("Enable location")
//                .setMessage("Please enable location access in order to use this Application.")
//                .setPositiveButton("Settings") { _, _ ->
//                    // Open the app's location settings
//                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                    intent.data = Uri.fromParts("package", context.packageName, null)
//                    context.startActivity(intent)
//                }
//                .setNegativeButton("Cancel", null)
//                .show()


