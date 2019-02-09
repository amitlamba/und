package com.und.service

import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


class TimeTest {
    @Test
    public fun testTimeWithTimeZone(){
        println(LocalDateTime.now())
        println(LocalDateTime.now(ZoneId.of("UTC")))
        println(LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.of("UTC")))
        println(Date.from(LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.of("UTC")).toInstant()))
    }
}