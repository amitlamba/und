package com.und.common.utils

import org.springframework.stereotype.Component

import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * Created by shiv on 01/09/17.
 */
@Component
class DateUtils : Serializable {

    fun now(): Date {
        return Date()
    }



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