package nl.lawik.poc.multiplatform

import kotlinx.serialization.*

@Serializable
data class ResultsList<T>(val results: List<T>)