package com.example.travelplanner

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface TravelApiService {

    @GET("region/europe")
    suspend fun getEuropeanDestinations(): Response<List<CountryDto>>
}

object RetrofitClient {
    private const val BASE_URL = "https://restcountries.com/v3.1/"

    val apiService: TravelApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TravelApiService::class.java)
    }
}