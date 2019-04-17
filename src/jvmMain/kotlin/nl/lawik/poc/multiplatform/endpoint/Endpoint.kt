package nl.lawik.poc.multiplatform.endpoint

import org.jboss.resteasy.spi.HttpResponse
import javax.ws.rs.core.Context

actual abstract class Endpoint {
    @Context
    private lateinit var response: HttpResponse

    protected fun setHeaders(headers: Map<String, List<Any>>) {
        response.outputHeaders.putAll(headers)
    }

    protected fun setHeader(key: String, vararg value: String) {
        response.outputHeaders[key] = value.toList()
    }
}