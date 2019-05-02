import io.restassured.response.ResponseBodyExtractionOptions
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import nl.lawik.poc.test.ResultsList

inline fun <reified T> ResponseBodyExtractionOptions.toList(): List<T> {
    return this.jsonPath().getList("", T::class.java)
}

inline fun <reified T> ResponseBodyExtractionOptions.to(): T {
    return this.jsonPath().getObject("", T::class.java)
}

inline fun <reified T: Any> ResponseBodyExtractionOptions.toResultsList(): ResultsList<T> {
    return Json.parse(ResultsList.serializer(T::class.serializer()), this.asString())
}