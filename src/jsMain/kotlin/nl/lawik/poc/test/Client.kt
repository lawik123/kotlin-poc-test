package nl.lawik.poc.test

import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.host
import io.ktor.client.request.port
import nl.lawik.poc.test.endpoint.API_HOST
import nl.lawik.poc.test.endpoint.API_PORT

val client = HttpClient {
    install(JsonFeature)
    defaultRequest {
        host = API_HOST
        port = API_PORT
    }
}
