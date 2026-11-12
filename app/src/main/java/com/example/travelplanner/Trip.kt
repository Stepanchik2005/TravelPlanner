package com.example.travelplanner

import java.io.Serializable

data class Trip(
    val id: Long = 0,
    val title: String,         // Назва подорожі (напр. "Відпустка в Карпатах")
    val destination: String,   // Місто / Країна
    val date: String,          // Дата поїздки (напр. "15.06.2026")
    val budget: Double,        // Орієнтовний бюджет ($)
    val notes: String          // Нотатки / маршрут
) : Serializable