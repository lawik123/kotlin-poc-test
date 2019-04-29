# Kotlin Multiplatform PoC
This project is a PoC of a Kotlin multiplatform project with a JavaScript frontend using React and a JVM backend using JAX-RS and Hibernate.
The project showcases the following features:
* Sharing model classes between platforms.
* Aligning REST-endpoints on the frontend and the backend.
* Sharing validators between platforms.

## Installation

### prerequisites
* Installation of PostgreSQL 9.6 (and optionally a database tool like pgAdmin)

### Installation steps
1. Clone this repository
2. Restore the provided `database.backup` (located in the root folder of this project) on a clean database named `multiplatform_poc`
3. configure the following fields in `src/jvmMain/resources/hibernate.cfg.xml` to match against your database settings:
   * `hibernate.connection.username`
   * `hibernate.connection.password`
   * `hibernate.connection.url`

## Running the project
1. Run the following command in the root directory of the project `gradlew bundle` this will create a `web` folder 
in the root directory of the project with a `kotlin-poc-multiplatform.bundle.js` file and a `index.html` file.
2. Run the following command in the root directory of the project `gradlew farmRun` this will create a WAR file
for the JVM code and run it in TomCat.
3. Open the `index.html` file from the `web` folder or perform a HTTP request to one of the endpoints defined in `src/jvmMain/kotlin/nl/lawik/poc/multiplatform/endpoint/PersonEndpoint.kt`

## About
The project is split into 3 parts:
* Common code located in `src/commonMain`
* JavaScript target code located in `src/jsMain`
* JVM target code located in `src/jvmMain`

Any code written in common is accessible by the other platforms, it's as if the code is actually defined in the platform,
this means you can't define something in a package that has already been defined in the same package in the common code.

### Sharing model classes between platforms
You can simply define your model(DTO) classes in the common code and they will be accessible from the JVM and JavaScript platforms.

### Aligning REST-endpoints between the frontend and the backend
The paths to the endpoints are defined in the common code as follows:
```kotlin
object PersonPaths {
    const val ROOT = "/person"
    const val GET_BY_ID = "/{id}"
    const val RESULTS_LIST_PATH = "/resultslist"

    fun getByIdPath(id: Long) = "/$id"
}
```
The paths used for HTTP-REST client/server configuring on the frontend/backend are set via this object, this means you only need to change the values once and it will be applied to all platforms.

In the common code a PersonEndpoint class is defined with the `expect` keyword:
```kotlin
expect class PersonEndpoint : Endpoint {
    suspend fun getById(id: Long): PersonDTO
    suspend fun getAll(): List<PersonDTO>
    suspend fun getAllResultsList(): ResultsList<PersonDTO>
    suspend fun create(personDTO: PersonDTO): Long
}
```
This is basically only the *definition* of the class, the `expect` keyword basically means that you must provide the `actual` *implementation*
for this class on every other platform (in the same package).

On the JVM platform:
```kotlin
@Path(PersonPaths.ROOT)
@Produces(MediaType.APPLICATION_JSON)
actual class PersonEndpoint : Endpoint() {

    @GET
    @Path(PersonPaths.GET_BY_ID)
    actual suspend fun getById(@PathParam("id") id: Long): PersonDTO = openAndCloseSession {
        val genericDaoImpl = GenericDaoImpl<Person, Long>(Person::class.java, it)
        genericDaoImpl.load(id)
    }?.dto ?: throw WebApplicationException(Response.Status.NOT_FOUND)

    @GET
    actual suspend fun getAll(): List<PersonDTO> = openAndCloseSession {
        val genericDaoImpl = GenericDaoImpl<Person, Long>(Person::class.java, it)
        genericDaoImpl.loadAll()
    }.map { it.dto }

    @GET
    @Path(PersonPaths.RESULTS_LIST_PATH)
    actual suspend fun getAllResultsList(): ResultsList<PersonDTO> = ResultsList(openAndCloseSession {
        val genericDaoImpl = GenericDaoImpl<Person, Long>(Person::class.java, it)
        genericDaoImpl.loadAll()
    }.map { it.dto })


    @POST
    @Status(201)
    actual suspend fun create(personDTO: PersonDTO): Long = openAndCloseSession {
        val genericDaoImpl = GenericDaoImpl<Person, Long>(Person::class.java, it)
        genericDaoImpl.save(personDTO.entity)
    }

}
```

On the JavaScript platform:
```kotlin
actual class PersonEndpoint : Endpoint(PersonPaths.ROOT) {
    actual suspend fun getById(id: Long): PersonDTO = client.get {
        setPath(PersonPaths.getByIdPath(id))
    }

    actual suspend fun getAll(): List<PersonDTO> = client.list {
        setPath()
    }

    actual suspend fun getAllResultsList(): ResultsList<PersonDTO> = client.resultsList {
        setPath(PersonPaths.RESULTS_LIST_PATH)
    }

    actual suspend fun create(personDTO: PersonDTO): Long = client.post {
        setPath()
        json(personDTO)
    }
}
```
This ensures that the method name/parameters/return type must always match, if this doesn't match you will get a compiler error.

As you can see the functions are `suspend` functions, this is done because the HTTP-client on the frontend makes use of Kotlin Coroutines for asynchronous request.
Sadly this causes problems on the JVM as the `suspend` keyword adds a parameter of the type `Continuation` to the functions marked with the `suspend` keyword.
This is what the `getById` functions get compiled to:
```java
public final Object getById(@PathParam("id") final long id, @NotNull Continuation $completion) {
    // ...
}
``` 
The problem is that JAX-RS sees this as a body parameter and tries to deserialize it which causes a server error.
I have written a JAX-RS MessageBodyReader which basically ignores the parameter as a workaround:
```kotlin
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
```
As you can see, the `create` endpoint has a `@Status` annotation on the JVM platform, this is done to set HTTP response status code *if the request is successful*.
Usually this is done by setting the status code in the JAX-RS Response object you return but this project doesn't make use of the JAX-RS response object as it wouldn't be possible to align the frontend and backend properly.
Below is the code for the annotation and the JAX-RS filter that sets the status code based on the annotation:
```kotlin
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
```

### Validation
This project makes use of the [Konform](https://github.com/konform-kt/konform) library for validation, this allows you to write validation rules for classes using a type-safe builder.
Below you will find a description of how it works (taken from the Konform github).

UserProfile data class:
```kotlin
data class UserProfile(
    val fullName: String,
    val age: Int?
)
```

UserProfile Validator:
```kotlin
val validateUser = Validation<UserProfile> {
    UserProfile::fullName {
        minLength(2)
        maxLength(100)
    }

    UserProfile::age ifPresent {
        minimum(0)
        maximum(150)
    }
}
```
You can call the validator as a function and pass a UserProfile object as a parameter which will return either a `Valid` or `Invalid` object:
```kotlin
val invalidUser = UserProfile("A", -1)
val validationResult = validateUser(invalidUser)
```
The above example will return an `Invalid` object, you can access the errors as follows:
```kotlin
validationResult[UserProfile::fullName]
// yields listOf("must be at least 2 characters")

validationResult[UserProfile::age]
// yields listOf("must be equal or greater than 0")
```

For this project I created a `Validateable` interface:
```kotlin
interface Validateable<T> {
    fun validate(): ValidationResult<T>
    fun validateCreate(): ValidationResult<T>? = null
    fun validateUpdate(): ValidationResult<T>? = null
}
```
As you can see you must implement the `validate` function and optionally the `validateCreate` and `validateUpdate` functions
(because you could possibly want different validation for POST and PUT requests).
Below is an example of a class that implements the interface:
```kotlin
data class PersonDTO(val id: Long? = null, val name: String, var age: Int) :
    Validateable<PersonDTO> {
    override fun validate() = validator(this)
    override fun validateCreate() = createValidator(this)
    override fun validateUpdate() = updateValidator(this)
}

object PersonDTOValidator {
    val validator = Validation<PersonDTO> {
        PersonDTO::age{
            minimum(0)
            maximum(200)
        }
        PersonDTO::name{
            notBlank()
            minLength(2)
        }
    }
    val createValidator = Validation<PersonDTO> {
        PersonDTO::id {
            isNull()
        }
        run(validator)
    }
    val updateValidator = Validation<PersonDTO> {
        PersonDTO::id required {
            minimum(1)
        }
        run(validator)
    }
}
```
As you can see the validators themselves are defined in a separate `object`, this is done to prevent them being created for every instance of `PersonDTO`.
You can also see that you can re-use validators with `run`, the `createValidator` and `updateValidator` both make use of the `validator` and have their own validation rules too.

`isNull` and `notBlank` are custom validation rules, you can define custom validation rules as follows:
```kotlin
fun ValidationBuilder<String>.notBlank() = addConstraint(
    "may not be empty"
) { it.isNotBlank() }
```
You create an extension function for `ValidationBuilder` and call the `addConstraint` function on it to which you supply 
the error message and a lambda which returns a boolean, the `it` in the lambda refers to the property the validation is being called on.

I have written a JAX-RS interceptor which validates request body parameters before reaching the request function, this means you won't have to validate requests in your request function manually as it will be done automatically:
```kotlin
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
```
The interceptor first checks whether the body object implements the `Validateable` interface, if not the body will be returned and the request's function will be invoked as usual.
If it does implement the `Validateable` interface it will check which validator should be used based on the request method and which validators are implemented.
The object will validated, if it is valid it will be returned and the request's function will be invoked as usual, if it is invalid a response will be sent with status code 422 (Unprocessable Entity) with the errors as the body.

Because the `Konform` library is multiplatform and the validation rules are written in the common code you can also access the same validators on in the JavaScript target code.
In this project they are called in the onChange function of an input field and when submitting a form (See the files located in `src/jsMain/kotlin/nl/lawik/poc/multiplatform/react`).



 