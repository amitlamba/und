package com.und.common.utils

import org.junit.Test

import java.util.Date

import org.assertj.core.api.Assertions.*
import java.net.URI
import java.net.URISyntaxException

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
    fun matchDomain(){
        var url1 = "https://www.myadvo.in/blog/how-to-prove-a-false-498a-case/"
        var url2= "https://myadvo.in"
        try {
            val uri1 = URI(url1)
            val domain1 = uri1.host
            val domainWoWww1 = if (domain1.startsWith("www.")) domain1.substring(4) else domain1
            val scheme1 = uri1.scheme
            val uri2 = URI(url2)
            val domain2 = uri2.host
            val domainWoWww2 = if (domain2.startsWith("www.")) domain2.substring(4) else domain2
            val scheme2 = uri2.scheme
            print( scheme1 == scheme2 && domainWoWww1 == domainWoWww2)
        }catch(ex: URISyntaxException){
            print( false)
        }
    }

}