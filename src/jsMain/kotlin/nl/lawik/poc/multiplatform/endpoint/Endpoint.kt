package nl.lawik.poc.multiplatform.endpoint

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.formUrlEncode

actual abstract class Endpoint(rootPath: String) {
    private val apiPath = "$API_PATH$rootPath"

    protected fun HttpRequestBuilder.setPath(path: String? = null, query: List<Pair<String, String?>> = listOf()) {
        url {
            var url = apiPath

            path?.let { url += it }

            if (query.isNotEmpty()) {
                url += "?${query.formUrlEncode()}"
            }
            encodedPath = url
        }
    }
}

