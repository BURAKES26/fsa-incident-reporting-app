package com.example.incidentscompose.data.model

import androidx.annotation.StringRes
import com.example.incidentscompose.R
import kotlinx.serialization.Serializable

@Serializable
enum class Priority (
    @StringRes val titleRes: Int
){
    LOW(R.string.LOW),
    MEDIUM(R.string.MEDIUM),
    HIGH(R.string.HIGH),
    CRITICAL(R.string.CRITICAL)
}