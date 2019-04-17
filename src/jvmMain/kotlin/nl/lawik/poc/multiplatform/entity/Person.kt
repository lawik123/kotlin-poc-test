package nl.lawik.poc.multiplatform.entity

import nl.lawik.poc.multiplatform.dto.PersonDTO
import javax.persistence.*

@Entity
data class Person(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    val name: String,

    @Column
    var age: Int
) {
    val dto: PersonDTO
        get() = PersonDTO(id, name, age)
}

val PersonDTO.entity: Person
    get() = Person(id, name, age)



