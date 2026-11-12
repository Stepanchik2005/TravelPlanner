package com.example.travelplanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TripsFragment : Fragment(R.layout.fragment_trips) {

    private val viewModel: TravelViewModel by activityViewModels {
        val repository = TravelRepository(
            TravelDatabaseHelper(requireContext().applicationContext),
            RetrofitClient.apiService
        )
        TravelViewModelFactory(repository)
    }

    private val addEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val action = result.data?.getStringExtra(AddEditTripActivity.RESULT_ACTION)
            @Suppress("DEPRECATION")
            val trip = result.data?.getSerializableExtra(AddEditTripActivity.RESULT_TRIP) as? Trip

            if (trip != null) {
                when (action) {
                    AddEditTripActivity.ACTION_ADD -> {
                        viewModel.addTrip(trip)
                        Toast.makeText(requireContext(), "Подорож додано", Toast.LENGTH_SHORT).show()
                    }
                    AddEditTripActivity.ACTION_UPDATE -> {
                        viewModel.updateTrip(trip)
                        Toast.makeText(requireContext(), "Зміни збережено", Toast.LENGTH_SHORT).show()
                    }
                    AddEditTripActivity.ACTION_DELETE -> {
                        viewModel.deleteTrip(trip.id)
                        Toast.makeText(requireContext(), "Подорож видалено", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSearch = view.findViewById<EditText>(R.id.etSearchTrips)
        val tvSummary = view.findViewById<TextView>(R.id.tvTripsSummary)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshTrips)
        val rvTrips = view.findViewById<RecyclerView>(R.id.rvTrips)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyTrips)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAddTrip)

        val adapter = TripAdapter(
            onItemClick = { trip ->
                val intent = Intent(requireContext(), AddEditTripActivity::class.java).apply {
                    putExtra(AddEditTripActivity.EXTRA_TRIP, trip)
                }
                addEditLauncher.launch(intent)
            },
            onDeleteClick = { trip ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Видалення подорожі")
                    .setMessage("Видалити «${trip.title}»?")
                    .setPositiveButton("Видалити") { _, _ ->
                        viewModel.deleteTrip(trip.id)
                        Toast.makeText(requireContext(), "Подорож видалено", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Скасувати", null)
                    .show()
            }
        )

        rvTrips.layoutManager = LinearLayoutManager(requireContext())
        rvTrips.adapter = adapter

        etSearch.addTextChangedListener { editable ->
            viewModel.loadTrips(editable?.toString() ?: "")
        }

        swipeRefresh.setOnRefreshListener {
            viewModel.loadTrips(etSearch.text.toString())
        }

        fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), AddEditTripActivity::class.java)
            addEditLauncher.launch(intent)
        }

        viewModel.trips.observe(viewLifecycleOwner) { trips ->
            adapter.submitList(trips)
            tvEmpty.visibility = if (trips.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.tripsSummary.observe(viewLifecycleOwner) { summary ->
            tvSummary.text = summary
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            swipeRefresh.isRefreshing = loading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }
}