package com.example.incidentscompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.incidentscompose.R
import com.example.incidentscompose.data.model.IncidentCategory
import com.example.incidentscompose.data.model.Priority
import com.example.incidentscompose.data.model.Role
import com.example.incidentscompose.data.model.Status
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object IncidentDisplayHelper {
    fun getStatusColor(status: Status): Color {
        return when (status) {
            Status.REPORTED -> Color(0xFFFFC107)
            Status.ASSIGNED -> Color(0xFFFF6B35)
            Status.RESOLVED -> Color(0xFF4CAF50)
        }
    }

    fun getPriorityColors(priority: Priority): Pair<Color, Color> {
        return when (priority) {
            Priority.CRITICAL -> Color(0xFFD32F2F) to Color.White
            Priority.HIGH -> Color(0xFFF57C00) to Color.White
            Priority.MEDIUM -> Color(0xFFFDD835) to Color.Black
            Priority.LOW -> Color(0xFF66BB6A) to Color.White
        }
    }

    fun getRoleColor(role: Role): Color {
        return when (role) {
            Role.ADMIN -> Color(0xFFE53935)
            Role.OFFICIAL -> Color(0xFF1E88E5)
            Role.USER -> Color(0xFF43A047)
        }
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val normalized = if (!dateString.endsWith("Z")) "${dateString}Z" else dateString
            val instant = Instant.parse(normalized)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            val day = localDateTime.date.day.toString().padStart(2, '0')
            val month = localDateTime.date.month.number.toString().padStart(2, '0')
            val year = localDateTime.date.year

            "$day-$month-$year"
        } catch (e: Exception) {
            dateString
        }
    }


    @Composable
    fun getCategoryLabel(category: IncidentCategory): String {
        val resId = when (category) {
            IncidentCategory.CRIME -> R.string.illegal_activities_and_safety_threats
            IncidentCategory.ENVIRONMENT -> R.string.nature_pollution_and_conservation_issues
            IncidentCategory.COMMUNAL -> R.string.shared_spaces_and_neighborhood_quality_of_life
            IncidentCategory.TRAFFIC -> R.string.roads_vehicles_and_transportation_safety
            IncidentCategory.OTHER -> R.string.any_issue_that_doesn_t_fit_the_other_categories
        }
        return stringResource(resId)
    }

    @Composable
    fun getPriorityLabel(priority: Priority): String {
        return stringResource(priority.titleRes)
    }

    @Composable
    fun getStatusLabel(status: Status): String {
        return stringResource(status.titleRes)
    }

    @Composable
    fun getRoleLabel(role: Role): String {
        return stringResource(role.titleRes)
    }
}