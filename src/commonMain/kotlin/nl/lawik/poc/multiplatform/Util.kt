package nl.lawik.poc.multiplatform

import io.konform.validation.ValidationBuilder

fun <T> ValidationBuilder<T>.isNull() = addConstraint(
    "must be null"
) { it == null }

fun ValidationBuilder<String>.notBlank() = addConstraint(
    "may not be empty"
) { it.isNotBlank() }