package nl.lawik.poc.test

import nl.lawik.poc.test.react.personForm
import react.dom.render
import kotlin.browser.document

fun main() {
    val rootDiv = document.getElementById("root")
    render(rootDiv) {
        personForm()
    }
}