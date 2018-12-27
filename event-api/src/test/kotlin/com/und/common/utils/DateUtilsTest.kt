package com.und.common.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Instant
import java.util.*

/**
 * Created by shiv on 21/07/17.
 */
class DateUtilsTest {


    @Test
    fun now() {
        assertThat(DateUtils().now()).isCloseTo(Date(), 1000)
    }
    @Test
    fun test(){
        println(Date.from(Instant.ofEpochMilli(1545661768692)))
    }

}