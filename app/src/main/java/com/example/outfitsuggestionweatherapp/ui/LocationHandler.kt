package com.example.outfitsuggestionweatherapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.outfitsuggestionweatherapp.utils.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class LocationHandler (private val context: Context, private val locationCallback: LocationCallback) {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationRequest: LocationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(10000)
        .setFastestInterval(5000)

    @RequiresApi(Build.VERSION_CODES.S)
    fun onStart() {
        checkLocationPermission()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkLocationPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as MainActivity, arrayOf(permission), Constants.LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        }
    }
}
