package com.example.incidentscompose.data.api

import com.example.incidentscompose.data.model.VehicleInfo
import com.example.incidentscompose.data.model.VehicleInfoResponse
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class VehicleApiTest {

    @Test
    fun getVehicleInfoReturnsVehicleInfoWhenSuccessful() = runBlocking {
        // Mock data
        val mockVehicle = VehicleInfo(
            kenteken = "01-RDW-1",
            voertuigsoort = "Personenauto",
            merk = "TOYOTA",
            handelsbenaming = "AYGO",
            eerste_kleur = "WIT"
        )
        val mockResponse = VehicleInfoResponse(value = listOf(mockVehicle))
        val responseJson = Json.encodeToString(mockResponse)

        // Mock engine
        val mockEngine = MockEngine { request ->
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val api = VehicleApi(client)
        val result = api.getVehicleInfo("01-RDW-1")

        assertNotNull(result)
        assertEquals(1, result?.value?.size)
        assertEquals("TOYOTA", result?.value?.first()?.merk)
        assertEquals("01-RDW-1", result?.value?.first()?.kenteken)
    }

    @Test
    fun getVehicleInfoReturnsNullWhenApiReturnsError() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val api = VehicleApi(client)
        val result = api.getVehicleInfo("INVALID")

        assertNull(result)
    }

    @Test
    fun getVehicleInfoReturnsNullWhenExceptionOccurs() = runBlocking {
        val mockEngine = MockEngine { request ->
            throw Exception("Network error")
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val api = VehicleApi(client)
        val result = api.getVehicleInfo("01-RDW-1")

        assertNull(result)
    }
}
