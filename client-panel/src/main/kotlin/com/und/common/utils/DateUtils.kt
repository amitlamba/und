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
        return getDateAt(date, LocalTime.of(0,0,0, 999999999), tz)
    }

    private fun getDateAt(date:String, lt:LocalTime, tz:ZoneId ):Date {
        val datePart = extractDatePart(date)
        val morningtWithoutTz = LocalDate.parse(datePart).atTime(lt)
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

    fun formatDateToOffsetDate(dateToConvert:Date):String{
        val systemDefaultOffset=OffsetDateTime.now(ZoneId.systemDefault()).offset
        return dateToConvert.toInstant().atOffset(systemDefaultOffset).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
    fun parseDateTime(date: String): LocalDateTime {
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

//fun main(args: Array<String>) {
//    val lt = LocalTime.of(10,12,13)
//    val tz = ZoneId.of("UTC")
//    val tz2 = ZoneId.of("America/Araguaina")
//    val testDate = "2018-07-07"
//    val morningtWithoutTz = LocalDateTime.now(tz2)
//    val morningwithTz = morningtWithoutTz.atZone(tz)
//    val dateInTz = Date.from(morningtWithoutTz.atZone(tz).toInstant())
//    //val dateInTz2 = Date.from(morningwithTz.atZone(tz).toInstant())
//    println(dateInTz)
//}