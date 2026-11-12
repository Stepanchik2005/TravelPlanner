package com.example.travelplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class TripAdapter(
    private val onItemClick: (Trip) -> Unit,
    private val onDeleteClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    private var trips: List<Trip> = emptyList()

    fun submitList(newTrips: List<Trip>) {
        trips = newTrips
        notifyDataSetChanged()
    }

    class TripViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTripTitle)
        val tvDestination: TextView = view.findViewById(R.id.tvTripDestination)
        val tvDate: TextView = view.findViewById(R.id.tvTripDate)
        val tvBudget: TextView = view.findViewById(R.id.tvTripBudget)
        val tvNotes: TextView = view.findViewById(R.id.tvTripNotes)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteTrip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        holder.tvTitle.text = trip.title
        holder.tvDestination.text = trip.destination
        holder.tvDate.text = "Дата: ${trip.date}"
        holder.tvBudget.text = String.format(Locale.US, "$%.2f", trip.budget)
        holder.tvNotes.text = trip.notes.ifBlank { "Без додаткових нотаток" }

        holder.itemView.setOnClickListener { onItemClick(trip) }
        holder.btnDelete.setOnClickListener { onDeleteClick(trip) }
    }

    override fun getItemCount(): Int = trips.size
}