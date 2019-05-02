import io.mockk.*
import io.restassured.RestAssured
import io.restassured.RestAssured.*
import io.restassured.http.ContentType
import nl.lawik.poc.test.ResultsList
import nl.lawik.poc.test.dao.generic.GenericDaoImpl
import nl.lawik.poc.test.dto.PersonDTO
import nl.lawik.poc.test.endpoint.API_PATH
import nl.lawik.poc.test.endpoint.PersonEndpoint
import nl.lawik.poc.test.endpoint.PersonPaths
import nl.lawik.poc.test.entity.Person
import nl.lawik.poc.test.util.openAndCloseSession
import org.hamcrest.Matchers
import org.hibernate.Session
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonEndpointTest {
    init {
        RestAssured.port = 8081
    }

    private lateinit var persons: Map<Long, Person>
    private val server = Server(PersonEndpoint::class)
    private val session: Session = mockk()

    @BeforeEach
    fun init() {
        // reset persons
        persons = mapOf(1L to Person(1L, "test", 24), 2L to Person(2L, "test2", 25))

        // set mocks
        mockkConstructor(GenericDaoImpl::class)
        every { anyConstructed<GenericDaoImpl<Person, Long>>().load(any()) } answers { persons[firstArg()] }
        every { anyConstructed<GenericDaoImpl<Person, Long>>().loadAll() } returns persons.values.toList()
        every { anyConstructed<GenericDaoImpl<Person, Long>>().save(any()) } answers {
            val newId = (persons.keys.max() ?: 0) + 1
            persons = persons + mapOf(newId to firstArg<Person>().copy(id = newId))
            newId
        }
        mockkStatic("nl.lawik.poc.test.util.HibernateUtilKt")
        every { openAndCloseSession<Any?>(captureLambda()) } answers { lambda<(Session) -> Any?>().invoke(session) }

        // start server
        server.start()
        server.deploy()
    }

    @AfterEach
    fun destroy() {
        server.stop()
        clearAllMocks()
    }

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

    @Nested
    inner class GetAllTest {
        @Test
        fun `get returns status code 200 and all persons DTOs as a list`() {
            val personDTOs = get("$API_PATH${PersonPaths.ROOT}")
                .then()
                .statusCode(200)
                .extract().toList<PersonDTO>()

            assertEquals(persons.values.map { it.dto }, personDTOs)
        }
    }

    @Nested
    inner class GetAllResultsListTest {
        @Test
        fun `get returns status code 200 and ResultsList with all persons DTOs`() {
            val resultsList =
                get("$API_PATH${PersonPaths.ROOT}${PersonPaths.RESULTS_LIST_PATH}")
                    .then()
                    .statusCode(200)
                    .extract().toResultsList<PersonDTO>()

            assertEquals(ResultsList(persons.values.map { it.dto }), resultsList)
        }
    }

    @Nested
    inner class CreateTest {
        @Test
        fun `post adds new person and returns it's id with status code 201`() {
            val newValidPerson = PersonDTO(null, "test3", 26)
            val insertedId =
                given()
                    .body(newValidPerson)
                    .contentType(ContentType.JSON)
                    .post("$API_PATH${PersonPaths.ROOT}")
                    .then()
                    .statusCode(201)
                    .extract().`as`(Long::class.java)

            assertEquals(persons.keys.max(), insertedId)
            assertEquals(newValidPerson.copy(id = insertedId), persons[insertedId]?.dto)
        }

        @Test
        fun `post with PersonDTO body where id is provided returns status code 422`() {
            val newInvalidPerson = PersonDTO(1, "test3", 26)
            given()
                .body(newInvalidPerson)
                .contentType(ContentType.JSON)
                .post("$API_PATH${PersonPaths.ROOT}")
                .then()
                .statusCode(422)
        }

        @Test
        fun `post with PersonDTO body where no name is provided returns status code 400`() {
            given()
                .body(mapOf("id" to null, "name" to null, "age" to 26))
                .contentType(ContentType.JSON)
                .post("$API_PATH${PersonPaths.ROOT}").then()
                .statusCode(400)
        }

        @Test
        fun `post with PersonDTO body where empty name is provided returns status code 422`() {
            val newInvalidPerson = PersonDTO(null, "", 26)
            given()
                .body(newInvalidPerson)
                .contentType(ContentType.JSON)
                .post("$API_PATH${PersonPaths.ROOT}")
                .then()
                .statusCode(422)
        }

        @Test
        fun `post with PersonDTO body where name of length 1 is provided returns status code 422`() {
            val newInvalidPerson = PersonDTO(null, "1", 26)
            given()
                .body(newInvalidPerson)
                .contentType(ContentType.JSON)
                .post("$API_PATH${PersonPaths.ROOT}")
                .then()
                .statusCode(422)
        }

        @Test
        fun `post with PersonDTO body where no age is provided returns status code 400`() {
            given()
                .body(mapOf("id" to null, "name" to "test3", "age" to null))
                .contentType(ContentType.JSON)
                .post("$API_PATH${PersonPaths.ROOT}")
                .then()
                .statusCode(400)
        }

        @Test
        fun `post with PersonDTO body where negative age is provided returns status code 422`() {
            val newInvalidPerson = PersonDTO(null, "test3", -26)
            given()
                .body(newInvalidPerson).contentType(ContentType.JSON)
                .post("$API_PATH${PersonPaths.ROOT}")
                .then()
                .statusCode(422)
        }
    }

}