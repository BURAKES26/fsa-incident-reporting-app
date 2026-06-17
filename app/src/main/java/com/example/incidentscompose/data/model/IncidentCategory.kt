package com.example.incidentscompose.data.model

import androidx.annotation.StringRes
import com.example.incidentscompose.R

enum class IncidentCategory (
    @StringRes val titleRes: Int
){
    CRIME(R.string.CRIME),              // Illegal activities and safety threats
    ENVIRONMENT(R.string.ENVIRONMENT),  // Nature, pollution and conservation issues
    COMMUNAL(R.string.COMMUNAL),        // Shared spaces and neighborhood quality of life
    TRAFFIC(R.string.TRAFFIC),          // Roads, vehicles and transportation safety
    OTHER(R.string.OTHER)               // Any issue that doesn't fit the above categories
}