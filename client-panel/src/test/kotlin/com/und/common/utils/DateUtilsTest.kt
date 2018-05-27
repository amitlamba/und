package com.und.common.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Test
import java.util.*

/**
 * Created by shiv on 21/07/17.
 */
class DateUtilsTest {


    @Test
    //@Throws(Exception::class)
    fun now() {
        assertThat(DateUtils().now()).isCloseTo(Date(), 1000)
    }

    @Test
    fun testTimezone() {
        val ids = TimeZone.getAvailableIDs()
        ids.forEach { println(it) }
        println("Timezone is: " + TimeZone.getTimeZone("EST"))
    }

    @Test
    fun validateTimezone() {
        Assert.assertTrue("Asia/Kolkata is not a valid Timezone", TimeZone.getAvailableIDs().contains("Asia/Kolkata"))
    }

    @Test
    fun validateCaseSensitiveTimezone() {
        Assert.assertFalse("asia/Kolkata should not be a valid Timezone!!", TimeZone.getAvailableIDs().contains("asia/Kolkata"))
        Assert.assertFalse("Asia/kolkata should not be a valid Timezone!!", TimeZone.getAvailableIDs().contains("Asia/kolkata"))
    }
}