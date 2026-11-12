package com.example.travelplanner

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TravelDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "travel_planner.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_TRIPS = "trips"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_DESTINATION = "destination"
        const val COL_DATE = "date"
        const val COL_BUDGET = "budget"
        const val COL_NOTES = "notes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TRIPS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_DESTINATION TEXT NOT NULL,
                $COL_DATE TEXT NOT NULL,
                $COL_BUDGET REAL NOT NULL,
                $COL_NOTES TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        // Добавляем 2 стартовые записи для наглядности при первом запуске
        seedInitialData(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val sampleTrips = listOf(
            Trip(
                title = "Вікенд у Львові",
                destination = "Львів, Україна",
                date = "20.06.2026",
                budget = 250.0,
                notes = "Площа Ринок, Високий Замок, кав'ярні"
            ),
            Trip(
                title = "Літній трекінг",
                destination = "Яремче, Карпати",
                date = "12.07.2026",
                budget = 420.0,
                notes = "Скелі Довбуша, водоспад Пробій, підйом на Говерлу"
            )
        )
        sampleTrips.forEach { trip ->
            val values = ContentValues().apply {
                put(COL_TITLE, trip.title)
                put(COL_DESTINATION, trip.destination)
                put(COL_DATE, trip.date)
                put(COL_BUDGET, trip.budget)
                put(COL_NOTES, trip.notes)
            }
            db.insert(TABLE_TRIPS, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRIPS")
        onCreate(db)
    }

    // CREATE
    fun insertTrip(trip: Trip): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, trip.title)
            put(COL_DESTINATION, trip.destination)
            put(COL_DATE, trip.date)
            put(COL_BUDGET, trip.budget)
            put(COL_NOTES, trip.notes)
        }
        return db.insert(TABLE_TRIPS, null, values)
    }

    // READ + SEARCH
    fun searchTrips(query: String = ""): List<Trip> {
        val db = readableDatabase
        val trips = mutableListOf<Trip>()

        val selection = if (query.isNotBlank()) {
            "$COL_TITLE LIKE ? OR $COL_DESTINATION LIKE ?"
        } else {
            null
        }
        val selectionArgs = if (query.isNotBlank()) {
            arrayOf("%$query%", "%$query%")
        } else {
            null
        }

        val cursor = db.query(
            TABLE_TRIPS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COL_ID DESC"
        )

        cursor.use {
            val idIdx = it.getColumnIndexOrThrow(COL_ID)
            val titleIdx = it.getColumnIndexOrThrow(COL_TITLE)
            val destIdx = it.getColumnIndexOrThrow(COL_DESTINATION)
            val dateIdx = it.getColumnIndexOrThrow(COL_DATE)
            val budgetIdx = it.getColumnIndexOrThrow(COL_BUDGET)
            val notesIdx = it.getColumnIndexOrThrow(COL_NOTES)

            while (it.moveToNext()) {
                trips.add(
                    Trip(
                        id = it.getLong(idIdx),
                        title = it.getString(titleIdx),
                        destination = it.getString(destIdx),
                        date = it.getString(dateIdx),
                        budget = it.getDouble(budgetIdx),
                        notes = it.getString(notesIdx)
                    )
                )
            }
        }
        return trips
    }

    // UPDATE
    fun updateTrip(trip: Trip): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, trip.title)
            put(COL_DESTINATION, trip.destination)
            put(COL_DATE, trip.date)
            put(COL_BUDGET, trip.budget)
            put(COL_NOTES, trip.notes)
        }
        return db.update(TABLE_TRIPS, values, "$COL_ID = ?", arrayOf(trip.id.toString()))
    }

    // DELETE
    fun deleteTrip(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_TRIPS, "$COL_ID = ?", arrayOf(id.toString()))
    }
}