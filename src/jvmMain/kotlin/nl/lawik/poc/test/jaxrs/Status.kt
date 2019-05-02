package nl.lawik.poc.test.jaxrs

import java.io.IOException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.ext.Provider

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