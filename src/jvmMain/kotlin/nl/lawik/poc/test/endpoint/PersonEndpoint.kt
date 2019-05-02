package nl.lawik.poc.test.endpoint

import nl.lawik.poc.test.ResultsList
import nl.lawik.poc.test.dao.generic.GenericDaoImpl
import nl.lawik.poc.test.dto.PersonDTO
import nl.lawik.poc.test.entity.Person
import nl.lawik.poc.test.entity.entity
import nl.lawik.poc.test.jaxrs.Status
import nl.lawik.poc.test.util.openAndCloseSession
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

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

