import nl.lawik.poc.test.endpoint.API_PATH
import nl.lawik.poc.test.jaxrs.*
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer
import javax.ws.rs.ApplicationPath
import javax.ws.rs.core.Application
import kotlin.reflect.KClass

class Server(val resource: KClass<*>) : UndertowJaxrsServer() {

    @ApplicationPath(API_PATH)
    private inner class App : Application() {
        override fun getClasses(): MutableSet<Class<*>> {
            return mutableSetOf(resource.java, ContinuationMessageBodyReader::class.java,
                CorsFilter::class.java, JSONConsumer::class.java,
                MismatchedInputExceptionMapper::class.java,
                MissingKotlinParameterExceptionMapper::class.java,
                StatusFilter::class.java, ValidationInterceptor::class.java)
        }
    }

    fun deploy() {
        this.deploy(App())
    }
}