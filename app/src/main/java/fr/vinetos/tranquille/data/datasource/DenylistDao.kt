package fr.vinetos.tranquille.data.datasource

import fr.vinetos.tranquille.DenylistDataSource
import fr.vinetos.tranquille.data.Database
import fr.vinetos.tranquille.data.DenylistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class DenylistDao(
    db: Database
) {

    fun dataSourceFactory(): DenylistDataSource.Factory {
        return DenylistDataSource.Factory(this)
    }

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

    fun getAll(offset: Int, limit: Int): List<DenylistItem> {
        return queries.selectAllLimitOffset(limit.toLong(), offset.toLong()).executeAsList()
    }

    fun getFirstMatch(number: String): DenylistItem? {
        return queries.getMatches(number).executeAsList().getOrNull(0)
    }

    fun countValid(): Int {
        return queries.countValid().executeAsOne().toInt()
    }

    fun countAll(): Int {
        return queries.countAll().executeAsOne().toInt()
    }

    suspend fun save(denylistItem: DenylistItem) {
        if (denylistItem.id < 1) {
            insert(denylistItem)
        } else {
            update(denylistItem)
        }
    }

    suspend fun insert(denylistItem: DenylistItem) {
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

    suspend fun addCall(denylistItem: DenylistItem, lastCallDate: Date) {
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
        denylistItem: DenylistItem, invalid: Boolean, creationDate: Date, numberOfCalls: Long
    ) {
        withContext(Dispatchers.IO) {
            queries.sanitize(
                invalid, creationDate, numberOfCalls, denylistItem.id
            )
        }
    }
}
