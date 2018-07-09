package com.und.common.utils

import org.springframework.stereotype.Component

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * Created by shiv on 01/09/17.
 */
@Component
class DateUtils : Serializable {

    fun now(): Date {
        return Date()
    }

    fun parseToDate(date: String): Any {
        return if (regexDateTime.matches(date)) {
            parseDateTime(date)
        } else if (regexDate.matches(date)) {
            parseDate(date)
        } else {
            date
        }
    }


    private fun parseDateTime(date: String): LocalDateTime {
        return LocalDateTime.parse(date, dateTimeFormatter)
    }

    private fun parseDate(date: String): LocalDate {
        return LocalDate.parse(date)
    }

    val dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE
    private val regexDateTime = "[0-9][0-9][0-9][0-9][-][0-9][0-9][-][0-9][0-9][T][0-9][0-9][:][0-9][0-9][:][0-9][0-9][.][0-9]*[Z]".toRegex()
    private val regexDate = "[0-9][0-9][0-9][0-9][-][0-9][0-9][-][0-9][0-9]".toRegex()

    companion object {

        private const val serialVersionUID = -3301695478208950415L


        fun nowInTimeZone(tz: ZoneId): Date {
            return Date.from(LocalDateTime.now(tz).atZone(tz).toInstant())
        }

        fun nowInUTC(): Date {
            val tz = ZoneId.of("UTC")
            return Date.from(LocalDateTime.now(tz).atZone(tz).toInstant())
        }
    }
}