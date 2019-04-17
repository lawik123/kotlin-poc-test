package nl.lawik.poc.multiplatform.endpoint

import nl.lawik.poc.multiplatform.ResultsList
import nl.lawik.poc.multiplatform.Status
import nl.lawik.poc.multiplatform.dao.generic.GenericDaoImpl
import nl.lawik.poc.multiplatform.dto.PersonDTO
import nl.lawik.poc.multiplatform.entity.Person
import nl.lawik.poc.multiplatform.entity.entity
import nl.lawik.poc.multiplatform.util.openAndCloseSession
import org.apache.http.HttpStatus
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
    actual suspend fun create(personDTO: PersonDTO): Long {
        if (personDTO.id != null) throw WebApplicationException(422)

        return openAndCloseSession {
            val genericDaoImpl = GenericDaoImpl<Person, Long>(Person::class.java, it)
            genericDaoImpl.save(personDTO.entity)
        }
    }
}

