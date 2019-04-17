package nl.lawik.poc.multiplatform.endpoint

import nl.lawik.poc.multiplatform.ResultsList
import nl.lawik.poc.multiplatform.dto.PersonDTO


internal object PersonPaths {
    const val ROOT = "/person"
    const val GET_BY_ID = "/{id}"
    const val RESULTS_LIST_PATH = "/resultslist"

    fun getByIdPath(id: Long) = "/$id"
}

expect class PersonEndpoint : Endpoint {
    suspend fun getById(id: Long): PersonDTO
    suspend fun getAll(): List<PersonDTO>
    suspend fun getAllResultsList(): ResultsList<PersonDTO>
    suspend fun create(personDTO: PersonDTO): Long
}

