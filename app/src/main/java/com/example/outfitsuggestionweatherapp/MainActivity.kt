package com.example.outfitsuggestionweatherapp

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import com.example.outfitsuggestionweatherapp.databinding.ActivityMainBinding
import okhttp3.*
import java.io.IOException
import java.net.URL
import java.security.Policy

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonFetch.setOnClickListener {
            makeRequestUsingOKHTTP()
            //makeRequest()

        }
    }

    private fun makeRequestUsingOKHTTP() {
        Log.i("String","Make Request")
        val request = Request.Builder()
            .url("https://api.openweathermap.org/data/2.5/weather?" +
                    "lat=30.033333&lon=31.233334&appid=545c025aec18c326d80f41ac17e459b0")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("String" , "fail: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                //Log.i("String", response.body?.string().toString())
                runOnUiThread{
                    binding.textView.text = response.body?.string()
                }
            }

        })
    }
}