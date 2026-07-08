package com.example.incidentscompose.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.incidentscompose.data.model.ApiResult
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.Role
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.data.store.TokenPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
enum class StatsPeriod {
    DAY, WEEK, MONTH
}

data class StatsUiState(
    val isLoading: Boolean = false,
    val incidents: List<IncidentResponse> = emptyList(),
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEK,
    val errorMessage: String? = null,
    val userRole: Role? = null
)

class StatsViewModel(
    private val incidentRepository: IncidentRepository,
    private val tokenPreferences: TokenPreferences
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadIncidents()
        loadUserRole()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(userRole = tokenPreferences.getUserRole())
        }
    }

    fun loadIncidents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = incidentRepository.getAllIncidents()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        incidents = result.data,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is ApiResult.HttpError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error ${result.code}: ${result.message}"
                    )
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Network error: ${result.exception.message}"
                    )
                }
                is ApiResult.Timeout -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Request timed out"
                    )
                }
                is ApiResult.Unknown -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Unknown error: ${result.exception.message}"
                    )
                }
                ApiResult.Unauthorized -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Unauthorized access"
                    )
                }
            }
        }
    }

    fun selectPeriod(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
    }
}
