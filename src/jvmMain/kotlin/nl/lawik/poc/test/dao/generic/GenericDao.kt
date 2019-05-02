package nl.lawik.poc.test.dao.generic

import java.io.Serializable

interface GenericDao<T, ID: Serializable> {
    fun load(id: ID): T?
    fun multiLoad(vararg ids: ID, orderedReturn: Boolean = true): List<T>
    fun multiLoad(ids: List<ID>, orderedReturn: Boolean = true): List<T>
    fun loadAll(firstResult: Int = 0, maxResults: Int = Int.MAX_VALUE): List<T>
    fun count(): Long
    fun save(o: T): ID?
    fun saveOrUpdate(o: T)
    fun delete(o: T)
}