package com.example.incidentscompose.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.core.net.toUri
import com.example.incidentscompose.data.api.VehicleApi
import com.example.incidentscompose.data.model.*
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.util.PhotoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportIncidentUiState(
    val selectedCategory: IncidentCategory = IncidentCategory.COMMUNAL,
    val description: String = "",
    val licensePlateNumber: String = "",
    val vehicleInfo: VehicleInfo? = null,
    val showVehicleInfoDialog: Boolean = false,
    val photos: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val errorMessage: String? = null,
    val showSuccessDialog: Boolean = false,
    val createdIncident: IncidentResponse? = null,
    val showPermissionDeniedWarning: Boolean = false,
    val showImageSourceDialog: Boolean = false,
    val shouldRequestLocationPermission: Boolean = false,
    val shouldUseCurrentLocation: Boolean = false
)

class ReportIncidentViewModel(
    private val repository: IncidentRepository,
    private val vehicleApi: VehicleApi
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ReportIncidentUiState())
    val uiState = _uiState.asStateFlow()

    fun updateCategory(category: IncidentCategory) =
        _uiState.update { it.copy(selectedCategory = category) }

    fun updateDescription(description: String) =
        _uiState.update { it.copy(description = description) }

    fun updateLicensePlateNumber(licensePlateNumber: String) =
        _uiState.update { it.copy(licensePlateNumber = licensePlateNumber) }

    fun searchVehicleInfo() {
        val licensePlate = _uiState.value.licensePlateNumber.uppercase()
        if (licensePlate.length != 6) {
            setError("License plate must be 6 characters")
            return
        }
        
        viewModelScope.launch {
            withLoading {
                val response = vehicleApi.getVehicleInfo(licensePlate)
                if (response != null && response.value.isNotEmpty()) {
                    _uiState.update { it.copy(vehicleInfo = response.value.first(), showVehicleInfoDialog = true, errorMessage = null) }
                } else {
                    setError("Vehicle not found")
                }
            }
        }
    }
    
    fun dismissVehicleInfoDialog() {
        _uiState.update { it.copy(showVehicleInfoDialog = false) }
    }

    fun addPhoto(uri: String) =
        _uiState.update { it.copy(photos = it.photos + uri) }

    fun removePhoto(uri: String) =
        _uiState.update { it.copy(photos = it.photos - uri) }

    fun updateLocation(latitude: Double, longitude: Double) =
        _uiState.update {
            it.copy(latitude = latitude, longitude = longitude, errorMessage = null)
        }

    fun clearLocation() =
        _uiState.update { it.copy(latitude = null, longitude = null) }

    fun showLocationError(message: String) =
        _uiState.update { it.copy(errorMessage = message) }

    fun requestUseCurrentLocation() =
        _uiState.update {
            it.copy(shouldRequestLocationPermission = true, shouldUseCurrentLocation = true)
        }

    fun onLocationPermissionHandled() =
        _uiState.update { it.copy(shouldRequestLocationPermission = false) }

    fun onCurrentLocationUsed() =
        _uiState.update { it.copy(shouldUseCurrentLocation = false) }

    fun showImageSourceDialog() =
        _uiState.update { it.copy(showImageSourceDialog = true) }

    fun dismissImageSourceDialog() =
        _uiState.update { it.copy(showImageSourceDialog = false) }

    fun dismissPermissionWarning() =
        _uiState.update { it.copy(showPermissionDeniedWarning = false) }

    fun onPhotoPermissionResult(granted: Boolean) {
        if (granted) {
            showImageSourceDialog()
        } else {
            _uiState.update { it.copy(showPermissionDeniedWarning = true) }
        }
    }

    fun submitReport(context: Context) {
        val state = _uiState.value

        // Local 'val' for latitude and longitude to avoid null checks later
        val latitude = state.latitude
        val longitude = state.longitude

        // Validate description first
        if (state.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a description") }
            return
        }

        // Validate license plate number if category is TRAFFIC
        if (state.selectedCategory == IncidentCategory.TRAFFIC && state.licensePlateNumber.isNotBlank()) {
            if (state.licensePlateNumber.length != 6 || !state.licensePlateNumber.all { it.isLetterOrDigit() }) {
                _uiState.update { it.copy(errorMessage = "License plate number must be 6 characters long and alphanumeric") }
                return
            }
        }

        // Validate a location is selected:
        if (latitude == null || longitude == null) {
            _uiState.update { it.copy(errorMessage = "Please select a location") }
            return
        }


        viewModelScope.launch {
            withLoading {
                try {
                    val result = repository.createIncident(
                        CreateIncidentRequest(
                            category = state.selectedCategory,
                            description = state.description,
                            latitude = latitude,
                            longitude = longitude,
                            priority = Priority.LOW,
                            licensePlateNumber = if (state.selectedCategory == IncidentCategory.TRAFFIC && state.licensePlateNumber.isNotBlank()) state.licensePlateNumber else null
                        )
                    )

                    when (result) {
                        is ApiResult.Success -> handleIncidentCreated(context, state.photos, result.data)
                        is ApiResult.HttpError -> setError("Failed to report incident, please try again later")
                        is ApiResult.NetworkError -> setError("Network error occurred while reporting incident")
                        is ApiResult.Timeout -> setError("Request timed out. Please try again.")
                        is ApiResult.Unknown -> setError("Unexpected error occurred while reporting incident.")
                        is ApiResult.Unauthorized -> Unit
                    }
                } catch (e: Exception) {
                    setError("Unexpected error: ${e.message ?: "Please try again"}")
                }
            }
        }
    }

    private suspend fun handleIncidentCreated(context: Context, photos: List<String>, incident: IncidentResponse) {
        photos.forEach { uriString ->
            val file = PhotoUtils.getFileFromUri(context, uriString.toUri())
            file?.let {
                when (repository.uploadImageToIncident(
                    incidentId = incident.id,
                    imageFile = it,
                    description = ""
                )) {
                    is ApiResult.HttpError -> setError("Failed to upload image: ${file.name}")
                    is ApiResult.NetworkError -> setError("Network error uploading image: ${file.name}")
                    is ApiResult.Timeout -> setError("Image upload timed out: ${file.name}")
                    is ApiResult.Unknown -> setError("Unknown error uploading image: ${file.name}")
                    else -> Unit
                }
            }
        }

        _uiState.update {
            it.copy(showSuccessDialog = true, createdIncident = incident, errorMessage = null)
        }
    }

    private fun setError(message: String) =
        _uiState.update { it.copy(errorMessage = message) }

    fun dismissSuccessDialog() =
        _uiState.update { it.copy(showSuccessDialog = false) }

    fun resetForm() =
        _uiState.update { ReportIncidentUiState(selectedCategory = IncidentCategory.COMMUNAL) }
}