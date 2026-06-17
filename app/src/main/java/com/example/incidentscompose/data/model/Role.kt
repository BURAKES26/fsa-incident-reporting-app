package com.example.incidentscompose.data.model

import androidx.annotation.StringRes
import com.example.incidentscompose.R
import kotlinx.serialization.Serializable

@Serializable
enum class Role (
    @StringRes val titleRes: Int
) {
    USER(R.string.USER),
    OFFICIAL(R.string.OFFICIAL),
    ADMIN(R.string.ADMIN)
}