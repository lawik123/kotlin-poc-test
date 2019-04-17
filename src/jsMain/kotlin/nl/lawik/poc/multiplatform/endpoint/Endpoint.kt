package nl.lawik.poc.multiplatform.endpoint

import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.http.formUrlEncode

actual abstract class Endpoint(rootPath: String) {
    protected companion object {
        val client = HttpClient {
            install(JsonFeature)
            defaultRequest {
                host = API_HOST
                port = API_PORT
            }
        }
    }


    private val apiPath = "$API_PATH$rootPath"

    protected fun HttpRequestBuilder.setPath(path: String? = null, query: List<Pair<String, String?>> = listOf()) {
        url {
            val queryString = query.formUrlEncode()
            val url = "$apiPath${path ?: ""}${if (queryString.isNotEmpty()) "?$queryString" else ""}"
            encodedPath = url
        }
    }
}

