package com.example.travelplanner

data class CountryDto(
    val name: CountryName,
    val capital: List<String>? = null,
    val region: String? = "Europe",
    val population: Long = 0
)

data class CountryName(
    val common: String,
    val official: String = ""
)