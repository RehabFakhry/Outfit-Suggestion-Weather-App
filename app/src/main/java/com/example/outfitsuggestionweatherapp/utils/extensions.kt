package com.example.outfitsuggestionweatherapp.utils

import android.annotation.SuppressLint
import com.example.outfitsuggestionweatherapp.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityMainBinding

fun getCurrentDateAndDay() {
    binding.textViewCurrentDate.text =
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    binding.textViewCurrentDay.text =
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"))
}