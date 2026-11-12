package com.example.travelplanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class AddEditTripActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRIP = "EXTRA_TRIP"
        const val EXTRA_PREFILL_DESTINATION = "EXTRA_PREFILL_DESTINATION"

        const val RESULT_ACTION = "RESULT_ACTION"
        const val RESULT_TRIP = "RESULT_TRIP"

        const val ACTION_ADD = "ADD"
        const val ACTION_UPDATE = "UPDATE"
        const val ACTION_DELETE = "DELETE"
    }

    private var existingTrip: Trip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_trip)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarAddEdit)
        val etTitle = findViewById<EditText>(R.id.etTripTitle)
        val etDestination = findViewById<EditText>(R.id.etTripDestination)
        val etDate = findViewById<EditText>(R.id.etTripDate)
        val etBudget = findViewById<EditText>(R.id.etTripBudget)
        val etNotes = findViewById<EditText>(R.id.etTripNotes)
        val btnSave = findViewById<Button>(R.id.btnSaveTrip)
        val btnDelete = findViewById<Button>(R.id.btnDeleteTripDetail)

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }

        @Suppress("DEPRECATION")
        existingTrip = intent.getSerializableExtra(EXTRA_TRIP) as? Trip
        val prefillDestination = intent.getStringExtra(EXTRA_PREFILL_DESTINATION)

        if (existingTrip != null) {
            toolbar.title = "Редагування подорожі"
            btnDelete.visibility = View.VISIBLE

            etTitle.setText(existingTrip!!.title)
            etDestination.setText(existingTrip!!.destination)
            etDate.setText(existingTrip!!.date)
            etBudget.setText(existingTrip!!.budget.toString())
            etNotes.setText(existingTrip!!.notes)
        } else {
            toolbar.title = "Нова подорож"
            if (!prefillDestination.isNullOrBlank()) {
                etDestination.setText(prefillDestination)
                etTitle.setText("Подорож: $prefillDestination")
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val destination = etDestination.text.toString().trim()
            val date = etDate.text.toString().trim()
            val budgetStr = etBudget.text.toString().trim()
            val notes = etNotes.text.toString().trim()

            if (title.isEmpty() || destination.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Заповніть назву, напрямок та дату", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = budgetStr.toDoubleOrNull()
            if (budget == null || budget < 0) {
                Toast.makeText(this, "Введіть коректну суму бюджету", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultTrip = Trip(
                id = existingTrip?.id ?: 0,
                title = title,
                destination = destination,
                date = date,
                budget = budget,
                notes = notes
            )
            val action = if (existingTrip != null) ACTION_UPDATE else ACTION_ADD

            val data = Intent().apply {
                putExtra(RESULT_ACTION, action)
                putExtra(RESULT_TRIP, resultTrip)
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }

        btnDelete.setOnClickListener {
            existingTrip?.let { trip ->
                val data = Intent().apply {
                    putExtra(RESULT_ACTION, ACTION_DELETE)
                    putExtra(RESULT_TRIP, trip)
                }
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }
}