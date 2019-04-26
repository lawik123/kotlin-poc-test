package nl.lawik.poc.multiplatform.react.input

import io.konform.validation.ValidationResult
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import kotlin.reflect.KProperty

interface StringInputProps : RProps {
    var label: String?
    var value: String
    var submitted: Boolean
    var onChange: (String) -> Unit
    var field: KProperty<*>
    var validator: () -> ValidationResult<*>
}

class StringInput : RComponent<StringInputProps, RState>() {
    override fun RBuilder.render() {
        inputWrapper(InputType.text, props.value, props.submitted, props.field, props.validator, props.label) { event, _ ->
            props.onChange((event.target as HTMLInputElement).value)
        }
    }
}

fun RBuilder.stringInput(
    value: String,
    field: KProperty<*>,
    submitted: Boolean,
    validator: () -> ValidationResult<*>,
    label: String? = null,
    onChange: (String) -> Unit
) = child(StringInput::class) {
    attrs.label = label
    attrs.value = value
    attrs.submitted = submitted
    attrs.onChange = onChange
    attrs.field = field
    attrs.validator = validator
}