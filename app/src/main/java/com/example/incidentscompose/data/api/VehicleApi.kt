package com.example.incidentscompose.data.api

import com.example.incidentscompose.data.model.VehicleInfoResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class VehicleApi(private val client: HttpClient) {
    private val baseUrl = "https://opendata.rdw.nl/api/odata/v4/m9d7-ebf2"

    suspend fun getVehicleInfo(licensePlate: String): VehicleInfoResponse? {
        return try {
            val response = client.get(baseUrl) {
                url {
                    parameters.append("\$select", "kenteken,voertuigsoort,merk,handelsbenaming,eerste_kleur")
                    encodedParameters.append("\$filter", "kenteken%20eq%20%27$licensePlate%27")
                    parameters.append("\$format", "json")
                }
            }
            if (response.status.isSuccess()) {
                response.body<VehicleInfoResponse>()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
