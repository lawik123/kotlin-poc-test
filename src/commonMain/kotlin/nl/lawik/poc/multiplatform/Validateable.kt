package nl.lawik.poc.multiplatform

import io.konform.validation.ValidationResult

interface Validateable<T> {
    fun validate(): ValidationResult<T>
    fun validateCreate(): ValidationResult<T>? = null
    fun validateUpdate(): ValidationResult<T>? = null
}

