package com.example.travelplanner

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class TravelViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: TravelRepository = mockk()
    private lateinit var viewModel: TravelViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getTrips(any()) } returns emptyList()
        coEvery { repository.getPopularDestinations() } returns Response.success(
            listOf(
                CountryDto(CountryName("France"), listOf("Paris"), "Europe", 68042591),
                CountryDto(CountryName("Germany"), listOf("Berlin"), "Europe", 84482267),
                CountryDto(CountryName("Ukraine"), listOf("Kyiv"), "Europe", 37000000)
            )
        )
        viewModel = TravelViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun tripsSummary_calculatesTripCountAndTotalBudget() {
        val sampleTrips = listOf(
            Trip(1L, "Тур 1", "Львів", "20.06.2026", 250.0, ""),
            Trip(2L, "Тур 2", "Карпати", "12.07.2026", 420.5, "")
        )
        coEvery { repository.getTrips("") } returns sampleTrips
        viewModel.tripsSummary.observeForever {}

        viewModel.loadTrips("")

        assertEquals("Заплановано подорожей: 2 | Бюджет: $670.50", viewModel.tripsSummary.value)
    }

    @Test
    fun addTrip_callsRepositoryAndRefreshesTripsList() {
        val newTrip = Trip(0L, "Поїздка в Рим", "Рим, Італія", "10.08.2026", 800.0, "Колізей")
        coEvery { repository.addTrip(newTrip) } returns 3L
        coEvery { repository.getTrips("") } returns listOf(newTrip.copy(id = 3L))

        viewModel.addTrip(newTrip)

        coVerify(exactly = 1) { repository.addTrip(newTrip) }
        assertEquals(1, viewModel.trips.value?.size)
        assertEquals("Поїздка в Рим", viewModel.trips.value?.get(0)?.title)
    }

    @Test
    fun deleteTrip_callsRepositoryDelete() {
        coEvery { repository.deleteTrip(1L) } returns 1

        viewModel.deleteTrip(1L)

        coVerify(exactly = 1) { repository.deleteTrip(1L) }
    }

    @Test
    fun filterDestinations_filtersListByCountryOrCapital() {
        viewModel.filterDestinations("Paris")

        val filtered = viewModel.destinations.value
        assertEquals(1, filtered?.size)
        assertEquals("France", filtered?.get(0)?.name?.common)
    }
}