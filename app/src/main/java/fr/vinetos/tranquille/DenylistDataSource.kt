package fr.vinetos.tranquille

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import fr.vinetos.tranquille.data.DenylistItem
import fr.vinetos.tranquille.data.datasource.DenylistDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.concurrent.Volatile

class DenylistDataSource(
    private val dao: DenylistDao
) : PositionalDataSource<DenylistItem>() {

    class Factory(
        private var denylistDao: DenylistDao
    ) : DataSource.Factory<Int, DenylistItem>() {

        @Volatile
        private var ds: DenylistDataSource? = null

        val currentDataSource: DenylistDataSource?
            get() = ds

        fun invalidate() {
            LOG.debug("invalidate()")

            val ds: DenylistDataSource? = this.ds
            ds?.invalidate()
        }

        override fun create(): DataSource<Int, DenylistItem> {
            return DenylistDataSource(denylistDao).also { ds = it }
        }
    }

    /**
     * The iterable must be iterated through
     * or the underlying cursor won't be closed.
     *
     * @return an iterable containing ids of all items this DS would load
     */
    fun getAllIds(): Iterable<Long> {
        // TODO: Avoid loading everything into memory
        val query = dao.getAll()
        val iterator: Iterator<DenylistItem> = query.listIterator()

        return Iterable {
            object : Iterator<Long> {
                override fun hasNext(): Boolean {
                    return iterator.hasNext()
                }

                override fun next(): Long {
                    return iterator.next().id
                }
            }
        }
    }

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<DenylistItem>
    ) {
        LOG.debug("loadInitial({}, {})", params.requestedStartPosition, params.requestedLoadSize)

        var offset = params.requestedStartPosition

        var items: List<DenylistItem> = loadItems(offset, params.requestedLoadSize)

        var totalCount: Int? = null

        if (items.isEmpty()) {
            totalCount = countAll()
            if (totalCount > 0) {
                LOG.debug(
                    "loadInitial() initial range is empty: totalCount={}, offset={}",
                    totalCount, offset
                )

                offset = ((totalCount - params.requestedLoadSize)
                        / params.pageSize * params.pageSize) // align to pageSize using integer math

                if (offset < 0) offset = 0

                LOG.debug("loadInitial() reloading with offset={}", offset)
                items = loadItems(offset, params.requestedLoadSize)
            } else {
                offset = 0
            }
        }

        if (params.placeholdersEnabled) {
            if (totalCount == null) totalCount = countAll()

            callback.onResult(items, offset, totalCount)
        } else {
            callback.onResult(items, offset)
        }
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<DenylistItem>
    ) {
        LOG.debug("loadRange({}, {})", params.startPosition, params.loadSize)

        callback.onResult(loadItems(params.startPosition, params.loadSize))
    }

    private fun loadItems(offset: Int, limit: Int): List<DenylistItem> {
        return dao.getAll(offset, limit)
    }

    private fun countAll(): Int {
        return dao.countAll()
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(DenylistDataSource::class.java)
    }
}
