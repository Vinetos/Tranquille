package fr.vinetos.tranquille.data.datasource

import app.cash.sqldelight.ColumnAdapter
import java.util.Date

object DateColumnAdapter : ColumnAdapter<Date, Long> {
    override fun decode(databaseValue: Long): Date = Date(databaseValue)
    override fun encode(value: Date): Long = value.time
}