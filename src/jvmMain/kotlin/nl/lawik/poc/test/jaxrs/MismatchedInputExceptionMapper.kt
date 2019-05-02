package nl.lawik.poc.test.jaxrs

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider


@Provider
class MismatchedInputExceptionMapper : ExceptionMapper<MismatchedInputException> {
    override fun toResponse(mie: MismatchedInputException): Response {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(mapOf("error" to "Invalid data supplied for request")).build()
    }
}