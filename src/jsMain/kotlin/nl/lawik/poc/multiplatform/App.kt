package nl.lawik.poc.multiplatform

import io.konform.validation.Invalid
import io.ktor.client.features.BadResponseStatusException
import kotlinx.serialization.ImplicitReflectionSerializer
import nl.lawik.poc.multiplatform.dto.PersonDTO
import nl.lawik.poc.multiplatform.endpoint.PersonEndpoint
import kotlin.js.Date

suspend fun main() {
    val api = PersonEndpoint()

    try {
        val person = api.getById(2)
        println(person)
    } catch (e: Exception) {
        when (e) {
            is BadResponseStatusException -> println(e.statusCode)
            else -> console.log(e)
        }
    }

    try {
        val persons = api.getAll()
        println(persons)
    } catch (e: Exception) {
        when (e) {
            is BadResponseStatusException -> println(e.statusCode)
            else -> console.log(e)
        }
    }

    try {
        val persons = api.getAllResultsList()
        println(persons)
    } catch (e: Exception) {
        when (e) {
            is BadResponseStatusException -> println(e.statusCode)
            else -> console.log(e)
        }
    }

    try {
        val insertedId = api.create(PersonDTO(null, "lawik", 24))
        println("Inserted id: $insertedId")
    } catch (e: Exception) {
        when (e) {
            is BadResponseStatusException -> println(e.statusCode)
            else -> console.log(e)
        }
    }

    try {
        api.create(PersonDTO(11, "lawik", 0)) // this will cause a (in this case intended) BadResponseException
    } catch (e: Exception) {
        when (e) {
            is BadResponseStatusException -> println(e.statusCode)
            else -> console.log(e)
        }
    }
}