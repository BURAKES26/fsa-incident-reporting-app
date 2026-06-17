package com.example.incidentscompose.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleInfoResponse(
    val value: List<VehicleInfo>
)

@Serializable
data class VehicleInfo(
    val kenteken: String,
    val voertuigsoort: String,
    val merk: String,
    val handelsbenaming: String,
    val eerste_kleur: String
)
