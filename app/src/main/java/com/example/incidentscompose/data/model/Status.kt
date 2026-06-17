package com.example.incidentscompose.data.model

import androidx.annotation.StringRes
import com.example.incidentscompose.R
import kotlinx.serialization.Serializable

@Serializable
enum class Status (
    @StringRes val titleRes: Int
)
{
    REPORTED(R.string.REPORTED),
    ASSIGNED(R.string.ASSIGNED),
    RESOLVED(R.string.RESOLVED)
}