package fr.vinetos.tranquille.data.datasource

import fr.vinetos.tranquille.data.Database
import fr.vinetos.tranquille.data.DenylistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DenylistDataSource(
    db: Database
) {

    private val queries = db.denylistItemQueries;
    suspend fun findById(id: Long): DenylistItem? {
        return withContext(Dispatchers.IO) {
            queries.findById(id).executeAsOneOrNull()
        }
    }

    suspend fun findByPattern(pattern: String): DenylistItem? {
        return withContext(Dispatchers.IO) {
            queries.findByPattern(pattern).executeAsOneOrNull()
        }
    }

    suspend fun findByNameAndPattern(name: String, pattern: String): DenylistItem? {
        return withContext(Dispatchers.IO) {
            queries.findByNameAndPattern(name, pattern).executeAsOneOrNull()
        }
    }

    fun getAll(): List<DenylistItem> {
        return queries.selectAll().executeAsList()
    }

    fun getFirstMatch(number: String): DenylistItem {
        throw NotImplementedError("Not implemented")
    }

    fun countValid(): Int {
        return queries.countValid().executeAsOne().toInt()
    }

    suspend fun save(denylistItem: DenylistItem) {
        withContext(Dispatchers.IO) {
            queries.insertItem(
                denylistItem.name,
                denylistItem.pattern,
                denylistItem.creationDate,
                denylistItem.invalid,
                denylistItem.numberOfCalls,
                denylistItem.lastCallDate,
            )
        }
    }

    suspend fun update(denylistItem: DenylistItem) {
        withContext(Dispatchers.IO) {
            queries.updateItem(
                denylistItem.id,
                denylistItem.name,
                denylistItem.pattern,
                denylistItem.creationDate,
                denylistItem.invalid,
                denylistItem.numberOfCalls,
                denylistItem.lastCallDate,
            )
        }
    }

    suspend fun addCall(denylistItem: DenylistItem, lastCallDate: String) {
        withContext(Dispatchers.IO) {
            queries.addCall(
                lastCallDate, denylistItem.id
            )
        }
    }

    suspend fun delete(ids: MutableIterator<Long>) {
        withContext(Dispatchers.IO) {
            ids.forEach { id ->
                queries.delete(id)
            }
        }
    }

    suspend fun sanitize(
        denylistItem: DenylistItem, invalid: Boolean, creationDate: String, numberOfCalls: Long
    ) {
        withContext(Dispatchers.IO) {
            queries.sanitize(
                if (invalid) 1 else 0, creationDate, numberOfCalls, denylistItem.id
            )
        }
    }

    fun countAll(): Long {
        return queries.countAll().executeAsOne()
    }

}