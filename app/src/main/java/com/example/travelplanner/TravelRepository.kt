package com.example.travelplanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class TravelRepository(
    private val dbHelper: TravelDatabaseHelper,
    private val apiService: TravelApiService
) {

    // Локальные операции с SQLite (CRUD)
    suspend fun getTrips(query: String = ""): List<Trip> = withContext(Dispatchers.IO) {
        dbHelper.searchTrips(query)
    }

    suspend fun addTrip(trip: Trip): Long = withContext(Dispatchers.IO) {
        dbHelper.insertTrip(trip)
    }

    suspend fun updateTrip(trip: Trip): Int = withContext(Dispatchers.IO) {
        dbHelper.updateTrip(trip)
    }

    suspend fun deleteTrip(id: Long): Int = withContext(Dispatchers.IO) {
        dbHelper.deleteTrip(id)
    }

    // Сетевой запрос через Retrofit
    suspend fun getPopularDestinations(): Response<List<CountryDto>> = withContext(Dispatchers.IO) {
        apiService.getEuropeanDestinations()
    }
}