package nl.lawik.poc.multiplatform.jaxrs

import java.io.InputStream
import java.lang.reflect.Type
import javax.ws.rs.Consumes
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyReader
import javax.ws.rs.ext.Provider
import kotlin.coroutines.Continuation

/**
 * NOTE: This is somewhat of a hack to get around the `suspend` keyword adding [Continuation]
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