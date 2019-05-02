package nl.lawik.poc.test.react.input

import io.konform.validation.ValidationResult
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import react.*
import kotlin.reflect.KProperty

interface IntInputProps : RProps {
    var label: String?
    var value: Int
    var submitted: Boolean
    var onChange: (Int) -> Unit
    var field: KProperty<*>
    var validator: () -> ValidationResult<*>
}

interface IntInputState : RState {
    var ref: RReadableRef<HTMLInputElement>
    var start: Int
    var end: Int
}

class IntInput : RComponent<IntInputProps, IntInputState>() {
    override fun RBuilder.render() {
        inputWrapper(
            InputType.tel, // tel because number doesn't support selection
            if (props.value != undefined) props.value.toString() else "",
            props.submitted,
            props.field,
            props.validator,
            props.label
        ) { event, ref ->
            val value = (event.target as HTMLInputElement).value
            if (value.isEmpty()) {
                props.onChange(undefined.asDynamic())
            } else if (value == "-") {
                props.onChange("-".asDynamic())
            } else {
                val int = value.toIntOrNull()
                if (int != null) {
                    props.onChange(int)
                }
            }
            setState {
                start = ref.current!!.selectionStart!! - 1
                end = ref.current!!.selectionEnd!! - 1
                this.ref = ref
            }
        }
    }

    override fun componentDidUpdate(prevProps: IntInputProps, prevState: IntInputState, snapshot: Any) {
        if (props.value != undefined && props.value == prevProps.value) {
            state.ref.current!!.setSelectionRange(state.start, state.end)
        }
    }
}


fun RBuilder.intInput(
    value: Int,
    field: KProperty<*>,
    submitted: Boolean,
    validator: () -> ValidationResult<*>,
    label: String? = null,
    onChange: (Int) -> Unit
) = child(IntInput::class) {
    attrs.label = label
    attrs.value = value
    attrs.submitted = submitted
    attrs.onChange = onChange
    attrs.field = field
    attrs.validator = validator
}