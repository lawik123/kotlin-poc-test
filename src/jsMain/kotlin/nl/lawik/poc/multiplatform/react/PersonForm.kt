package nl.lawik.poc.multiplatform.react

import io.konform.validation.Valid
import io.konform.validation.ValidationResult
import io.ktor.client.features.BadResponseStatusException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.html.js.onClickFunction
import nl.lawik.poc.multiplatform.dto.PersonDTO
import nl.lawik.poc.multiplatform.endpoint.PersonEndpoint
import nl.lawik.poc.multiplatform.react.input.intInput
import nl.lawik.poc.multiplatform.react.input.stringInput
import react.*
import react.dom.button
import react.dom.div
import react.dom.form
import kotlin.browser.window


interface PersonFormState : RState {
    var name: String
    var age: Int
    var submitted: Boolean
}

class PersonForm : RComponent<RProps, PersonFormState>() {
    override fun PersonFormState.init() {
        name = ""
        submitted = false
    }

    override fun RBuilder.render() {
        div {
            form {
                stringInput(
                    state.name,
                    PersonDTO::name,
                    state.submitted,
                    ::validator
                ) {
                    setState { name = it }
                }
                intInput(
                    state.age,
                    PersonDTO::age,
                    state.submitted,
                    ::validator
                ) {
                    setState { age = it }
                }
            }
            button {
                +"submit"
                attrs.onClickFunction = {
                    setState {
                        submitted = true
                    }
                    if (validator() is Valid<*>) {
                        val endpoint = PersonEndpoint()
                        GlobalScope.async {
                            try {
                                val insertedId = endpoint.create(PersonDTO(null, state.name, state.age))
                                window.alert("person with id: $insertedId created!")
                                setState {
                                    name = ""
                                    age = undefined.asDynamic()
                                    submitted = false
                                }
                            } catch (e: Exception) {
                                when (e) {
                                    is BadResponseStatusException -> println(e.statusCode)
                                    else -> console.log(e)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun validator(): ValidationResult<PersonDTO> {
        val person = PersonDTO(null, state.name, state.age)
        return person.validateCreate()
    }
}

fun RBuilder.personForm() = child(PersonForm::class) {}

