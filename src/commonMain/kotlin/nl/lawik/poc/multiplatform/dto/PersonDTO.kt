package nl.lawik.poc.multiplatform.dto

import kotlinx.serialization.Serializable

@Serializable
data class PersonDTO(val id: Long? = null, val name: String, var age: Int)