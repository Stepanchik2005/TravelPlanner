package com.example.travelplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class CountryAdapter(
    private val onAddToPlansClick: (CountryDto) -> Unit
) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {

    private var countries: List<CountryDto> = emptyList()

    fun submitList(newCountries: List<CountryDto>) {
        countries = newCountries
        notifyDataSetChanged()
    }

    class CountryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCountryName)
        val tvCapital: TextView = view.findViewById(R.id.tvCountryCapital)
        val tvPopulation: TextView = view.findViewById(R.id.tvCountryPopulation)
        val btnAdd: Button = view.findViewById(R.id.btnAddCountryToTrips)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = countries[position]
        val capitalName = country.capital?.firstOrNull() ?: "Не вказано"

        holder.tvName.text = country.name.common
        holder.tvCapital.text = "Столиця: $capitalName"
        holder.tvPopulation.text = String.format(
            Locale.US,
            "Регіон: %s | Населення: %,d",
            country.region,
            country.population
        )

        holder.btnAdd.setOnClickListener { onAddToPlansClick(country) }
    }

    override fun getItemCount(): Int = countries.size
}