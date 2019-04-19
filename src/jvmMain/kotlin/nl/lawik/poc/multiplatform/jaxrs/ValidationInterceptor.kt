package nl.lawik.poc.multiplatform.jaxrs

import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.ValidationResult
import nl.lawik.poc.multiplatform.Validateable
import java.io.IOException
import javax.ws.rs.Consumes
import javax.ws.rs.HttpMethod
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.Request
import javax.ws.rs.core.Response
import javax.ws.rs.ext.Provider
import javax.ws.rs.ext.ReaderInterceptor
import javax.ws.rs.ext.ReaderInterceptorContext


@Provider
@Consumes("application/json", "application/*+json", "text/json")
class ValidationInterceptor : ReaderInterceptor {

    @Context
    private lateinit var context: Request

    @Throws(IOException::class, WebApplicationException::class)
    override fun aroundReadFrom(interceptorContext: ReaderInterceptorContext): Any? {
        val body = interceptorContext.proceed()
        if (body is Validateable<*>) {
            lateinit var validation: ValidationResult<*>
            val method = context.method
            if (method == HttpMethod.POST) {
                validation = body.validateCreate() ?: body.validate()
            } else if (method == HttpMethod.PUT) {
                validation = body.validateUpdate() ?: body.validate()
            }
            when (validation) {
                is Valid -> return body
                is Invalid -> {
                    // hacky but whatever, this should be accessible anyway...
                    val errorsField = validation::class.java.getDeclaredField("errors")
                    errorsField.isAccessible = true
                    val errors = errorsField.get(validation)

                    throw WebApplicationException(Response.status(422).entity(mapOf("errors" to errors)).build())
                }
            }
        }
        return body
    }
}