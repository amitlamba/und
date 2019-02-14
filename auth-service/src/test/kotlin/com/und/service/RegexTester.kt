package com.und.service

import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.regex.Pattern


class RegexTester {
    @Test
    fun tester(){
        var input="https://developer.mozilla.org/en-US/docs/Web/JavaScript"
        var input1="http://developer.mozilla.org/"
        var input2="https://developer.org/en-US"
        var input3="https:/developer.mozilla.org/en-US"
        var input4="http://developer.mozilla.org"
        var input5="https://developer.com"
        var pattern= Pattern.compile("^(?<scheme>https?)(:\\/\\/)(?<host>\\w+(\\.\\w+)+\\/?)")

        var matcher=pattern.matcher(input)
        assertEquals(true,matcher.find())

         matcher=pattern.matcher(input1)
        assertEquals(true,matcher.find())

         matcher=pattern.matcher(input2)
        assertEquals(true,matcher.find())

         matcher=pattern.matcher(input3)
        assertEquals(false,matcher.find())

         matcher=pattern.matcher(input4)
        assertEquals(true,matcher.find())

        matcher=pattern.matcher(input5)
        assertEquals(true,matcher.find())

    }
}