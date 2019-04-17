package nl.lawik.poc.multiplatform

import kotlinx.serialization.*

data class ResultsList<T>(val results: List<T>)

@Serializer(forClass = ResultsList::class)
class ResultsListSerializer<T : Any>(private val resultsSerializer: KSerializer<T>) : KSerializer<ResultsList<T>> {
    override val descriptor = object : SerialDescriptor {
        override fun isElementOptional(index: Int) = false

        override val name = "kotlin.ResultsList"
        override val kind: SerialKind = StructureKind.CLASS
        override fun getElementName(index: Int) = when(index) {
            0 -> "results"
            else -> ""
        }
        override fun getElementIndex(name: String) = when(name) {
            "results" -> 0
            else -> -1
        }
    }

    override fun serialize(encoder: Encoder, obj: ResultsList<T>) {
        val out = encoder.beginStructure(descriptor)
        out.encodeSerializableElement(descriptor, 0, resultsSerializer.list, obj.results)
        out.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): ResultsList<T> {
        val inp = decoder.beginStructure(descriptor)
        lateinit var data: List<T>
        loop@ while (true) {
            when (val i = inp.decodeElementIndex(descriptor)) {
                CompositeDecoder.READ_DONE -> break@loop
                0 -> data = inp.decodeSerializableElement(descriptor, i, resultsSerializer.list)
                else -> throw SerializationException("Unknown index $i")
            }
        }
        inp.endStructure(descriptor)
        return ResultsList(data)
    }
}