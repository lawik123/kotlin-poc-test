package nl.lawik.poc.test.react.input

import io.konform.validation.Invalid
import io.konform.validation.ValidationResult
import kotlinx.css.Color
import kotlinx.css.px
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.br
import react.dom.input
import react.dom.label
import styled.css
import styled.styledDiv
import styled.styledSpan
import kotlin.reflect.KProperty


interface InputWrapperProps : RProps {
    var value: String
    var type: InputType
    var submitted: Boolean
    var onChange: (Event, RReadableRef<HTMLInputElement>) -> Unit
    var validator: () -> ValidationResult<*>
    var label: String?
    var field: KProperty<*>
}

interface InputWrapperState : RState {
    var validationResult: ValidationResult<*>
}

class InputWrapper : RComponent<InputWrapperProps, InputWrapperState>() {
    private val inputRef = createRef<HTMLInputElement>()

    override fun RBuilder.render() {
        styledDiv {
            label {
                +"${props.label ?: props.field.name}:"
            }
            br {}
            input(props.type) {
                attrs.value = props.value
                ref = inputRef
                attrs.onChangeFunction = {
                    props.onChange(it, inputRef)
                    if (props.submitted) {
                        setState {
                            validationResult = props.validator()
                        }
                    }
                }
            }
            if (state.validationResult is Invalid<*>) {
                br {}
                (state.validationResult as Invalid<*>)[props.field]?.forEachIndexed { index, error ->
                    if (index % 2 == 1) {
                        br {}
                    }
                    styledSpan {
                        +error
                        css {
                            color = Color.red
                        }
                    }
                }
            }
            css {
                marginBottom = 10.px
            }
        }
    }

    override fun componentDidUpdate(prevProps: InputWrapperProps, prevState: InputWrapperState, snapshot: Any) {
        if (!prevProps.submitted && props.submitted) {
            setState {
                validationResult = props.validator()
            }
        }
    }
}

fun RBuilder.inputWrapper(
    type: InputType,
    value: String,
    submitted: Boolean,
    field: KProperty<*>,
    validator: () -> ValidationResult<*>,
    label: String? = null,
    onChange: (Event, RReadableRef<HTMLInputElement>) -> Unit
) = child(InputWrapper::class) {
    attrs.type = type
    attrs.label = label
    attrs.submitted = submitted
    attrs.value = value
    attrs.onChange = onChange
    attrs.validator = validator
    attrs.field = field
}