package com.example.travelplanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private val viewModel: TravelViewModel by activityViewModels {
        val repository = TravelRepository(
            TravelDatabaseHelper(requireContext().applicationContext),
            RetrofitClient.apiService
        )
        TravelViewModelFactory(repository)
    }

    private val addTripLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            @Suppress("DEPRECATION")
            val trip = result.data?.getSerializableExtra(AddEditTripActivity.RESULT_TRIP) as? Trip
            if (trip != null) {
                viewModel.addTrip(trip)
                Toast.makeText(
                    requireContext(),
                    "Напрямок «${trip.destination}» додано до ваших планів!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSearch = view.findViewById<EditText>(R.id.etSearchCountries)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshExplore)
        val rvCountries = view.findViewById<RecyclerView>(R.id.rvCountries)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarExplore)

        val adapter = CountryAdapter { country ->
            val capital = country.capital?.firstOrNull()
            val destinationText = if (!capital.isNullOrBlank()) {
                "$capital, ${country.name.common}"
            } else {
                country.name.common
            }

            val intent = Intent(requireContext(), AddEditTripActivity::class.java).apply {
                putExtra(AddEditTripActivity.EXTRA_PREFILL_DESTINATION, destinationText)
            }
            addTripLauncher.launch(intent)
        }

        rvCountries.layoutManager = LinearLayoutManager(requireContext())
        rvCountries.adapter = adapter

        etSearch.addTextChangedListener { editable ->
            viewModel.filterDestinations(editable?.toString() ?: "")
        }

        swipeRefresh.setOnRefreshListener {
            etSearch.text.clear()
            viewModel.loadDestinations()
        }

        viewModel.destinations.observe(viewLifecycleOwner) { countries ->
            adapter.submitList(countries)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading && adapter.itemCount == 0) View.VISIBLE else View.GONE
            if (!loading) {
                swipeRefresh.isRefreshing = false
            }
        }
    }
}