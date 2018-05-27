package com.und.common.utils

import org.junit.Test

import java.util.Date

import org.assertj.core.api.Assertions.*

/**
 * Created by shiv on 21/07/17.
 */
class DateUtilsTest {


    @Test
    //@Throws(Exception::class)
    fun now() {
        assertThat(DateUtils().now()).isCloseTo(Date(), 1000)
    }




}