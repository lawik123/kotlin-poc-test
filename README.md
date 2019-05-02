# Kotlin Testing PoC
This project is a PoC of a Kotlin multiplatform project with a JavaScript frontend using React and a JVM backend using JAX-RS and Hibernate.
The project showcases the following features:
* *API* tests

For information about the project itself refer to the following [repository](https://github.com/lawik123/kotlin-poc-multiplatform).

## Installation
Clone this repository.

NOTE: This is only suitable for running the tests, if you want to run the actual source code, additional steps are required, please refer to the following [repository](https://github.com/lawik123/kotlin-poc-multiplatform).

## Running the tests
Run the following command in the root directory of the project `gradlew jvmTest`, this will run the tests written for the jvm and generate a report in the `build/reports/tests/jvmTest` directory.

Alternatively you can run the tests via IntelliJ IDEA.

## About
The written tests only tests the actual REST-endpoints, the rest is mocked.

The following technologies are used for the tests:
* [JUnit5](https://junit.org/junit5/) - Test framework
* [Mockk](https://mockk.io/) - Used for mocking
* [UndertowJaxrsServer](https://docs.jboss.org/resteasy/docs/3.0.8.Final/userguide/html/RESTEasy_Embedded_Container.html#d4e1391) - Embedded container for the API
* [REST-assured](http://rest-assured.io/) - Used to call/test the endpoints

### Mocking
Mockk allows you to mock in Kotlin easily using a DSL, below are some example.

Defining a mocked variable:
```kotlin
private val session: Session = mockk()
```

Mocking a constructor:
```kotlin
mockkConstructor(GenericDaoImpl::class)
```

Mocking a function from a class (persons is defined elsewhere):
```kotlin
every { anyConstructed<GenericDaoImpl<Person, Long>>().load(any()) } answers { persons[firstArg()] } // any() and firstArg to retrieve the first argument
```

Mocking statics:
```kotlin
mockkStatic("nl.lawik.poc.test.util.HibernateUtilKt")

// mocking the static `openAndCloseSession` function defined in HibernateUtil.kt
every { openAndCloseSession<Any?>(captureLambda()) } answers { lambda<(Session) -> Any?>().invoke(session) }
```

### The JAX-RS server
A wrapper class `Server` has been written for `UndertowJaxrsServer` which you supply the resource you want to test to and 
it will configure the server for you when you invoke `deploy()` on it. 

### Writing the tests
The tests are grouped by endpoint, this is achieved by making use of `inner class` and the JUnit `@Nested` annotation.

The root test class has the `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` annotation, this causes only one instance of the class for all the tests to be created.

Functions are run before and after each test by marking functions with the `@BeforeEach` and `@AfterEach` annotations respectively.

Below is a code example of the `getById` tests:
```kotlin
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonEndpointTest {
    // ...
    @Nested
    inner class GetByIdTest {
        @Test
        fun `get by id 1 returns status code 200 and PersonDTO`() {
            val personDTO = get("$API_PATH${PersonPaths.ROOT}/1")
                .then()
                .statusCode(200)
                .extract().to<PersonDTO>()
            assertEquals(persons[1]?.dto, personDTO)
        }
        @Test
        fun `get by invalid id returns status code 404`() {
            get("$API_PATH${PersonPaths.ROOT}/-1")
                .then()
                .statusCode(404)
                .body(Matchers.isEmptyString())
        }
    }
    // ...
   }
```

As you can see both tests are grouped in the same inner class, this causes the test report to be more readable.
You can also see that the method name is defined using backticks, this is also done for more readability 
as `get by invalid id returns status code 404` is more readable than `getByInvalidIdReturnsStatusCode404`.

```kotlin
val personDTO = get("$API_PATH${PersonPaths.ROOT}/1")
               .then()
               .statusCode(200)
               .extract().to<PersonDTO>()
```
This part is provided by the REST-assured library, it makes a http request to the provided path and then checks 
if the status code is 200 (this is an assertion) after which it will deserialize the body as a `PersonDTO` object
which is used for another assertion afterwards.

`to` is one of the util functions used to make deserialization easier, see `Util.kt` for details.

