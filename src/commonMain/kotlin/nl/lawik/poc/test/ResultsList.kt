package nl.lawik.poc.test

import kotlinx.serialization.*

@Serializable
data class ResultsList<T>(val results: List<T>)