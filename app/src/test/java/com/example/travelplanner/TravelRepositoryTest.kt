package com.example.travelplanner

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class TravelRepositoryTest {

    private val dbHelper: TravelDatabaseHelper = mockk()
    private val apiService: TravelApiService = mockk()
    private lateinit var repository: TravelRepository

    @Before
    fun setUp() {
        repository = TravelRepository(dbHelper, apiService)
    }

    @Test
    fun addTrip_insertsTripIntoDatabaseAndReturnsId() = runTest {
        val trip = Trip(
            title = "Вікенд у Кракові",
            destination = "Краків, Польща",
            date = "15.09.2026",
            budget = 350.0,
            notes = "Вавель, Ринкова площа"
        )
        every { dbHelper.insertTrip(trip) } returns 1L

        val insertedId = repository.addTrip(trip)

        assertEquals(1L, insertedId)
        verify(exactly = 1) { dbHelper.insertTrip(trip) }
    }

    @Test
    fun getTrips_withSearchQuery_returnsFilteredTrips() = runTest {
        val expectedTrips = listOf(
            Trip(1L, "Літній трекінг", "Яремче, Карпати", "12.07.2026", 420.0, "Говерла")
        )
        every { dbHelper.searchTrips("Карпати") } returns expectedTrips

        val result = repository.getTrips("Карпати")

        assertEquals(1, result.size)
        assertEquals("Літній трекінг", result[0].title)
        verify(exactly = 1) { dbHelper.searchTrips("Карпати") }
    }

    @Test
    fun updateTrip_updatesExistingRecordInDatabase() = runTest {
        val updatedTrip = Trip(1L, "Оновлений тур", "Львів", "25.06.2026", 300.0, "Новий маршрут")
        every { dbHelper.updateTrip(updatedTrip) } returns 1

        val rowsAffected = repository.updateTrip(updatedTrip)

        assertEquals(1, rowsAffected)
        verify(exactly = 1) { dbHelper.updateTrip(updatedTrip) }
    }

    @Test
    fun deleteTrip_removesRecordById() = runTest {
        every { dbHelper.deleteTrip(1L) } returns 1

        val rowsDeleted = repository.deleteTrip(1L)

        assertEquals(1, rowsDeleted)
        verify(exactly = 1) { dbHelper.deleteTrip(1L) }
    }

    @Test
    fun getPopularDestinations_fetchesCountriesFromApi() = runTest {
        val mockCountries = listOf(
            CountryDto(CountryName("France"), listOf("Paris"), "Europe", 68042591),
            CountryDto(CountryName("Italy"), listOf("Rome"), "Europe", 58870762)
        )
        coEvery { apiService.getEuropeanDestinations() } returns Response.success(mockCountries)

        val response = repository.getPopularDestinations()

        assertTrue(response.isSuccessful)
        assertEquals(2, response.body()?.size)
        assertEquals("France", response.body()?.get(0)?.name?.common)
        coVerify(exactly = 1) { apiService.getEuropeanDestinations() }
    }
}