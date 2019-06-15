package com.und.service

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.regex.Pattern


class RegexTester {
    @Test
    fun tester() {
        val input = "https://developer.mozilla.org/en-US/docs/Web/JavaScript"
        val input1 = "http://developer.mozilla.org/"
        val input2 = "https://developer.org/en-US"
        val input3 = "https:/developer.mozilla.org/en-US"
        val input4 = "http://developer.mozilla.org"
        val input5 = "https://developer.com"
        val pattern = Pattern.compile("^(?<scheme>https?)(:\\/\\/)(?<host>\\w+(\\.\\w+)+\\/?)")

        var matcher = pattern.matcher(input)
        assertEquals(true, matcher.find())

        matcher = pattern.matcher(input1)
        assertEquals(true, matcher.find())

        matcher = pattern.matcher(input2)
        assertEquals(true, matcher.find())

        matcher = pattern.matcher(input3)
        assertEquals(false, matcher.find())

        matcher = pattern.matcher(input4)
        assertEquals(true, matcher.find())

        matcher = pattern.matcher(input5)
        assertEquals(true, matcher.find())

    }
}