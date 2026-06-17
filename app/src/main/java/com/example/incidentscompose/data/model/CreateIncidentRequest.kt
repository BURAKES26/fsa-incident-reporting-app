package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateIncidentRequest(
    val category: IncidentCategory,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val priority: Priority = Priority.LOW,
    val licensePlateNumber: String? = null
)