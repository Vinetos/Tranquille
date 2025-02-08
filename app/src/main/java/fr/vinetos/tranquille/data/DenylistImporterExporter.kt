package fr.vinetos.tranquille.data

import androidx.core.util.ObjectsCompat
import fr.vinetos.tranquille.data.datasource.DenylistDao
import fr.vinetos.tranquille.domain.service.DenylistService
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.Date

class DenylistImporterExporter {
    fun writeBackup(denylistItems: Iterable<DenylistItem>, out: Appendable): Boolean {
        try {
            CSVFormat.DEFAULT.print(out).use { printer ->
                printer.printRecord(
                    HEADER_ID, HEADER_NAME, HEADER_PATTERN,
                    "creationTimestamp", "numberOfCalls", "lastCallTimestamp"
                )
                for ((id, name, pattern, creationDate, _, numberOfCalls, lastCallDate) in denylistItems) {
                    printer.printRecord(
                        id, name,
                        DenylistUtils.patternToHumanReadable(pattern),
                        creationDate.time, numberOfCalls,
                        lastCallDate?.time ?: ""
                    )
                }
            }
        } catch (e: IOException) {
            LOG.warn("write()", e)
            return false
        }

        return true
    }

    fun importDenylist(
        denylistDao: DenylistDao,
        denylistService: DenylistService,
        fileDescriptor: FileDescriptor?
    ): Boolean {
        var items: List<DenylistItem>? = null

        try {
            FileInputStream(fileDescriptor).use { inputStream ->
                items = read(inputStream)
            }
        } catch (e: IOException) {
            LOG.warn("importDenylist()", e)
        }

        if (items == null) {
            return false
        }

        for (item in items!!) {
            val existingItem: DenylistItem? = runBlocking {
                denylistDao.findById(item.id)
            } ?: runBlocking {
                denylistDao.findByPattern(item.pattern)
            }

            val isNew = existingItem == null ||
                !ObjectsCompat.equals(item.pattern, existingItem.pattern)

            if (isNew) {
                denylistService.insert(item)
            } else {
                var changed = false
                val updatedItem = existingItem!!.copy(
                    name = if (existingItem.name.isNullOrEmpty() && !item.name.isNullOrEmpty()) {
                        changed = true
                        item.name
                    } else {
                        existingItem.name
                    },
                    numberOfCalls = if (existingItem.numberOfCalls < item.numberOfCalls) {
                        changed = true
                        item.numberOfCalls
                    } else {
                        existingItem.numberOfCalls
                    },
                    lastCallDate = if (item.lastCallDate != null && (
                        existingItem.lastCallDate == null ||
                            existingItem.lastCallDate.before(item.lastCallDate)
                        )
                    ) {
                        changed = true
                        item.lastCallDate
                    } else {
                        existingItem.lastCallDate
                    }
                )

                if (changed) {
                    denylistService.update(updatedItem)
                }
            }
        }

        return true
    }

    @Throws(IOException::class)
    fun read(inputStream: InputStream?): List<DenylistItem>? {
        var items: List<DenylistItem>? = null

        BufferedInputStream(inputStream).use { bis ->
            // BufferedReaders used in `isYacbBackup` and `isNoPhoneSpamBackup` use 8192 char buffers,
            // the max size of a UTF-8 char is 4 bytes.
            bis.mark(8192 * 4)
            if (isYacbBackup(InputStreamReader(bis))) {
                bis.reset()
                LOG.info("importDenylist() importing as YACB backup")
                items = readYacbBackup(InputStreamReader(bis))
            } else {
                LOG.debug("importDenylist() not a YACB backup")
                bis.reset()
                if (isNoPhoneSpamBackup(InputStreamReader(bis))) {
                    bis.reset()
                    LOG.info("importDenylist() trying to import as NoPhoneSpam backup")
                    items = readNoPhoneSpamBackup(InputStreamReader(bis))
                } else {
                    LOG.debug("importDenylist() not a NoPhoneSpam backup")
                }
            }
        }
        return items
    }

    fun isYacbBackup(`in`: Reader): Boolean {
        try {
            val parser = CSVFormat.DEFAULT.parse(`in`) // do NOT close

            val iterator: Iterator<CSVRecord> = parser.iterator()
            if (iterator.hasNext()) {
                val record = iterator.next()

                if (record.size() < INDEX_PATTERN + 1) return false

                val foundHeader = checkYacbHeader(record)
                LOG.debug("isYacbBackup() found header={}", foundHeader)
                if (foundHeader) return true

                // check that the types match
                try {
                    if (!get(record, INDEX_ID).isNullOrEmpty()) {
                        get(record, INDEX_ID)!!.toLong()
                    }

                    if (!DenylistUtils.isValidPattern(
                            DenylistUtils.cleanPattern(
                                DenylistUtils.patternFromHumanReadable(
                                    get(record, INDEX_PATTERN)
                                )
                            )
                        )
                    ) {
                        return false
                    }

                    if (!get(record, INDEX_CREATION_DATE).isNullOrEmpty()) {
                        get(record, INDEX_CREATION_DATE)!!.toLong()
                    }

                    if (!get(record, INDEX_NUMBER_OF_CALLS).isNullOrEmpty()) {
                        get(record, INDEX_NUMBER_OF_CALLS)!!.toInt()
                    }

                    if (!get(record, INDEX_LAST_CALL_DATE).isNullOrEmpty()) {
                        get(record, INDEX_LAST_CALL_DATE)!!.toLong()
                    }
                } catch (e: Exception) {
                    LOG.debug("isYacbBackup() error parsing item", e)
                    return false
                }

                return true
            }
        } catch (e: IOException) {
            LOG.debug("isYacbBackup()", e)
            return false
        }

        LOG.debug("isYacbBackup() empty file?")
        return true
    }

    private fun checkYacbHeader(record: CSVRecord): Boolean {
        var foundHeader = false
        try {
            foundHeader = HEADER_ID == record[INDEX_ID]
                    && HEADER_NAME == record[INDEX_NAME]
                    && HEADER_PATTERN == record[INDEX_PATTERN]
        } catch (e: Exception) {
            LOG.warn("checkYacbHeader() error checking header", e)
        }
        return foundHeader
    }

    fun readYacbBackup(`in`: Reader): List<DenylistItem>? {
        try {
            CSVFormat.DEFAULT.parse(`in`).use { parser ->
                val denylistItems: MutableList<DenylistItem> = ArrayList()
                var first = true

                for (record in parser) {
                    if (first) {
                        first = false

                        val foundHeader = checkYacbHeader(record)
                        LOG.debug("readYacbBackup() found header={}", foundHeader)

                        if (foundHeader) {
                            continue
                        }
                    }

                    var item = DenylistItem(
                        id = -1L,
                        name = null,
                        pattern = "",
                        creationDate = Date(),
                        invalid = false,
                        numberOfCalls = 0,
                        lastCallDate = null
                    )

                    var enough = false

                    try {
                        if (!get(record, INDEX_ID).isNullOrEmpty()) {
                            item = item.copy(
                                id = get(record, INDEX_ID)!!.toLong()
                            )
                        }

                        item = item.copy(
                            name = record[INDEX_NAME]
                        )

                        item = item.copy(
                            pattern = DenylistUtils.cleanPattern(
                                DenylistUtils.patternFromHumanReadable(
                                    record[INDEX_PATTERN]
                                )
                            )
                        )

                        enough = true

                        if (!get(record, INDEX_CREATION_DATE).isNullOrEmpty()) {
                            item = item.copy(
                                creationDate = Date(get(record, INDEX_CREATION_DATE)!!.toLong())
                            )
                        }

                        if (!get(record, INDEX_NUMBER_OF_CALLS).isNullOrEmpty()) {
                            item = item.copy(
                                numberOfCalls = get(record, INDEX_NUMBER_OF_CALLS)!!.toInt()
                                    .toLong()
                            )
                        }

                        if (!(get(record, INDEX_LAST_CALL_DATE)).isNullOrEmpty()) {
                            item = item.copy(
                                lastCallDate = Date(get(record, INDEX_LAST_CALL_DATE)!!.toLong())
                            )
                        }
                    } catch (e: Exception) {
                        LOG.warn("readYacbBackup() error parsing item", e)
                    }

                    LOG.trace("readYacbBackup() enough={}", enough)
                    if (enough) {
                        denylistItems.add(sanitize(item))
                    }
                }
                return denylistItems
            }
        } catch (e: IOException) {
            LOG.warn("readYacbBackup()", e)
            return null
        }
    }

    fun isNoPhoneSpamBackup(`in`: Reader?): Boolean {
        try {
            val br = BufferedReader(`in`) // do NOT close

            val delimiter = ": "

            val line = br.readLine()
            if (line != null) {
                return line.contains(delimiter)
            }
        } catch (e: IOException) {
            LOG.warn("isNoPhoneSpamBackup()", e)
            return false
        }

        return true
    }

    fun readNoPhoneSpamBackup(`in`: Reader?): List<DenylistItem>? {
        try {
            BufferedReader(`in`).use { br ->
                val denylistItems: MutableList<DenylistItem> = ArrayList()
                val delimiter = ": "

                var line: String
                while ((br.readLine().also { line = it }) != null) {
                    val delimiterIndex = line.indexOf(delimiter)
                    if (delimiterIndex == -1) {
                        LOG.warn("readNoPhoneSpamBackup() incorrect format: no delimiter")
                        continue
                    }

                    val pattern = line.substring(0, delimiterIndex).trim { it <= ' ' }
                    val name = line.substring(delimiterIndex + delimiter.length)

                    val item = DenylistItem(
                        id = -1L,
                        name = name,
                        pattern = DenylistUtils.cleanPattern(
                            DenylistUtils.patternFromHumanReadable(pattern)
                        ),
                        creationDate = Date(),
                        invalid = false,
                        numberOfCalls = 0,
                        lastCallDate = null
                    )
                    denylistItems.add(sanitize(item))
                }
                return denylistItems
            }
        } catch (e: IOException) {
            LOG.warn("readNoPhoneSpamBackup()", e)
            return null
        }
    }

    private fun sanitize(item: DenylistItem): DenylistItem {
        return item.copy(
            invalid = !DenylistUtils.isValidPattern(item.pattern)
        )
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(
            DenylistImporterExporter::class.java
        )

        private const val HEADER_ID = "ID"
        private const val HEADER_NAME = "name"
        private const val HEADER_PATTERN = "pattern"

        private const val INDEX_ID = 0
        private const val INDEX_NAME = 1
        private const val INDEX_PATTERN = 2
        private const val INDEX_CREATION_DATE = 3
        private const val INDEX_NUMBER_OF_CALLS = 4
        private const val INDEX_LAST_CALL_DATE = 5

        private fun get(record: CSVRecord, index: Int): String? {
            return if (record.size() > index) {
                record[index]
            } else {
                null
            }
        }
    }
}
