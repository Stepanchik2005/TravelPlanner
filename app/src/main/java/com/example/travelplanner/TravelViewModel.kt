package com.example.travelplanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Locale

class TravelViewModel(private val repository: TravelRepository) : ViewModel() {

    private var currentTripQuery: String = ""
    private var allDestinationsCache: List<CountryDto> = emptyList()

    private val _trips = MutableLiveData<List<Trip>>(emptyList())
    val trips: LiveData<List<Trip>> = _trips

    // Реактивная статистика через трансформацию LiveData.map
    val tripsSummary: LiveData<String> = _trips.map { list ->
        val totalBudget = list.sumOf { it.budget }
        String.format(
            Locale.US,
            "Заплановано подорожей: %d | Бюджет: $%.2f",
            list.size,
            totalBudget
        )
    }

    private val _destinations = MutableLiveData<List<CountryDto>>(emptyList())
    val destinations: LiveData<List<CountryDto>> = _destinations

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadTrips()
        loadDestinations()
    }

    // --- CRUD-операции с локальной БД (SQLite) ---

    fun loadTrips(query: String = currentTripQuery) {
        currentTripQuery = query
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _trips.value = repository.getTrips(query)
            } catch (e: Exception) {
                _errorMessage.value = "Помилка читання БД: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                repository.addTrip(trip)
                loadTrips()
            } catch (e: Exception) {
                _errorMessage.value = "Помилка додавання: ${e.message}"
            }
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                repository.updateTrip(trip)
                loadTrips()
            } catch (e: Exception) {
                _errorMessage.value = "Помилка оновлення: ${e.message}"
            }
        }
    }

    fun deleteTrip(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTrip(id)
                loadTrips()
            } catch (e: Exception) {
                _errorMessage.value = "Помилка видалення: ${e.message}"
            }
        }
    }

    // --- Загрузка направлений из сети (Retrofit) ---

    fun loadDestinations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getPopularDestinations()
                val body = response.body()
                if (response.isSuccessful && !body.isNullOrEmpty()) {
                    val sorted = body.sortedBy { it.name.common }
                    allDestinationsCache = sorted
                    _destinations.value = sorted
                } else {
                    val fallback = getFallbackDestinations()
                    allDestinationsCache = fallback
                    _destinations.value = fallback
                }
            } catch (e: Exception) {
                val fallback = getFallbackDestinations()
                allDestinationsCache = fallback
                _destinations.value = fallback
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getFallbackDestinations(): List<CountryDto> = listOf(
        CountryDto(CountryName("Austria"), listOf("Vienna"), "Europe", 9104772),
        CountryDto(CountryName("Croatia"), listOf("Zagreb"), "Europe", 3855641),
        CountryDto(CountryName("Czechia"), listOf("Prague"), "Europe", 10827529),
        CountryDto(CountryName("France"), listOf("Paris"), "Europe", 68042591),
        CountryDto(CountryName("Germany"), listOf("Berlin"), "Europe", 84482267),
        CountryDto(CountryName("Greece"), listOf("Athens"), "Europe", 10413982),
        CountryDto(CountryName("Italy"), listOf("Rome"), "Europe", 58870762),
        CountryDto(CountryName("Netherlands"), listOf("Amsterdam"), "Europe", 17811291),
        CountryDto(CountryName("Norway"), listOf("Oslo"), "Europe", 5514042),
        CountryDto(CountryName("Poland"), listOf("Warsaw"), "Europe", 36753736),
        CountryDto(CountryName("Portugal"), listOf("Lisbon"), "Europe", 10467366),
        CountryDto(CountryName("Spain"), listOf("Madrid"), "Europe", 48345223),
        CountryDto(CountryName("Sweden"), listOf("Stockholm"), "Europe", 10549347),
        CountryDto(CountryName("Switzerland"), listOf("Bern"), "Europe", 8849852),
        CountryDto(CountryName("Ukraine"), listOf("Kyiv"), "Europe", 37000000)
    )

    fun filterDestinations(query: String) {
        if (query.isBlank()) {
            _destinations.value = allDestinationsCache
        } else {
            _destinations.value = allDestinationsCache.filter { country ->
                country.name.common.contains(query, ignoreCase = true) ||
                        (country.capital?.any { it.contains(query, ignoreCase = true) } == true)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

class TravelViewModelFactory(
    private val repository: TravelRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TravelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TravelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}