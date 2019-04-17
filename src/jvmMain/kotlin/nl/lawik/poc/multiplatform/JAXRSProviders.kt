package nl.lawik.poc.multiplatform

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.InputStream
import java.lang.reflect.Type
import javax.ws.rs.Consumes
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.ContextResolver
import javax.ws.rs.ext.MessageBodyReader
import javax.ws.rs.ext.Provider
import kotlin.coroutines.Continuation
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import java.io.IOException
import javax.ws.rs.HttpMethod
import javax.ws.rs.core.Response

@Provider
@Consumes("application/json", "application/*+json", "text/json")
class JSONConsumer : ContextResolver<ObjectMapper> {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    override fun getContext(type: Class<*>?): ObjectMapper {
        return objectMapper
    }

}

/**
 * NOTE: This is somewhat of a hack to get around the suspend keyword adding [Continuation]
 * as a body parameter to the method parameters of the compiled java bytecode which causes the server to send a 415 error as it can't find
 * a body reader for the type [Continuation].
 *
 * TODO? Maybe replace with a compiler plugin that removes [Continuation] from the REST method parameters during compilation.
 */
@Provider
@Consumes("*/*")
class ContinuationMessageBodyReader : MessageBodyReader<Continuation<*>> {
    override fun isReadable(
        type: Class<*>?,
        genericType: Type?,
        annotations: Array<out kotlin.Annotation>?,
        mediaType: MediaType?
    ): Boolean {
        return type == Continuation::class.java
    }

    override fun readFrom(
        type: Class<Continuation<*>>?,
        genericType: Type?,
        annotations: Array<out kotlin.Annotation>?,
        mediaType: MediaType?,
        httpHeaders: MultivaluedMap<String, String>?,
        entityStream: InputStream?
    ): Continuation<*>? {
        return null
    }

}

@Provider
class CorsFilter : ContainerResponseFilter {
    override fun filter(
        requestContext: ContainerRequestContext,
        responseContext: ContainerResponseContext
    ) {
        responseContext.headers.add("Access-Control-Allow-Origin", "*")
        responseContext.headers.add(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS, HEAD"
        )
        responseContext.headers.add(
            "Access-Control-Allow-Headers",
            "origin, content-type, accept, authorization"
        );
    }
}

// status code setter, partial credits: https://stackoverflow.com/a/25701949

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
annotation class Status(val statusCode: Int)

@Provider
class StatusFilter : ContainerResponseFilter {

    @Throws(IOException::class)
    override fun filter(
        containerRequestContext: ContainerRequestContext,
        containerResponseContext: ContainerResponseContext
    ) {
        if (containerResponseContext.status == 200) {
            containerResponseContext.entityAnnotations?.filterIsInstance<Status>()?.firstOrNull()
                ?.let { containerResponseContext.status = it.statusCode }
        }
    }
}






