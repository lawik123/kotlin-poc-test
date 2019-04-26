package nl.lawik.poc.multiplatform

import nl.lawik.poc.multiplatform.react.personForm
import react.dom.render
import kotlin.browser.document

fun main() {
    val rootDiv = document.getElementById("root")
    render(rootDiv) {
        personForm()
    }
}