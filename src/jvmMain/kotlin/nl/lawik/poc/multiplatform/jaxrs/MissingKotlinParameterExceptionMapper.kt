package nl.lawik.poc.multiplatform.jaxrs

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider


@Provider
class MissingKotlinParameterExceptionMapper : ExceptionMapper<MissingKotlinParameterException> {
    override fun toResponse(mkpe: MissingKotlinParameterException): Response {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(mapOf("error" to "Invalid data supplied for request")).build()
    }
}