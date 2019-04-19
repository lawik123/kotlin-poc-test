package nl.lawik.poc.multiplatform.dto

import io.konform.validation.Validation
import io.konform.validation.jsonschema.minLength
import io.konform.validation.jsonschema.minimum
import kotlinx.serialization.Serializable
import nl.lawik.poc.multiplatform.Validateable
import nl.lawik.poc.multiplatform.dto.PersonDTOValidator.createValidator
import nl.lawik.poc.multiplatform.dto.PersonDTOValidator.updateValidator
import nl.lawik.poc.multiplatform.dto.PersonDTOValidator.validator
import nl.lawik.poc.multiplatform.isNull

@Serializable
data class PersonDTO(val id: Long? = null, val name: String, var age: Int) :
    Validateable<PersonDTO> {
    override fun validate() = validator(this)
    override fun validateCreate() = createValidator(this)
    override fun validateUpdate() = updateValidator(this)
}

private object PersonDTOValidator{
    val validator = Validation<PersonDTO> {
        PersonDTO::age{
            minimum(0)
        }
        PersonDTO::name{
            minLength(1)
        }
    }
    val createValidator = Validation<PersonDTO> {
        PersonDTO::id {
            isNull()
        }
        run(validator)
    }
    val updateValidator = Validation<PersonDTO> {
        PersonDTO::id required {
            minimum(1)
        }
        run(validator)
    }
}
