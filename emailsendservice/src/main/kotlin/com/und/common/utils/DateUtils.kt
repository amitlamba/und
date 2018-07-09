package com.und.common.utils

import org.springframework.stereotype.Component

import java.io.Serializable
import java.time.*
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

    fun getMidnight(date: String, tz:ZoneId):Date {
        return getDateAt(date, LocalTime.of(23,59,59, 999999999), tz)

    }


    fun getStartOfDay(date: String, tz:ZoneId):Date {
        return getDateAt(date, LocalTime.of(0,0,0), tz)
    }

    private fun getDateAt(date:String, lt:LocalTime, tz:ZoneId ):Date {
        val datePart = extractDatePart(date)
        val morningtWithoutTz = LocalDate.parse(date).atTime(lt)
        val dateInTz = Date.from(morningtWithoutTz.atZone(tz).toInstant())
        return dateInTz
    }

    private fun extractDatePart (date: String):String {
        return if (regexDateTime.matches(date)) {
            date.substringBefore("T")
        } else if (regexDate.matches(date)) {
            date
        } else {
            throw IllegalArgumentException("date is in invalid format $date")
        }
    }

    fun parseToDate(date: String): Any {
        return if (regexDateTime.matches(date)) {
            convertDateTimeToDate(parseDateTime(date))
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

    fun convertDateTimeToDate(ld:LocalDateTime):Date {
        val  zdtTime = ld.atZone(ZoneId.systemDefault());
        return  Date.from(zdtTime.toInstant())

    }


    fun convertDateToDateTime(dateToConvert:Date):LocalDateTime {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

    }

    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE
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

