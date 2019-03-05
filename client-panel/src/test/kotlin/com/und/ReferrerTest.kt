package com.und

import org.junit.Test
import java.net.URI
import java.net.URISyntaxException
import java.util.regex.Pattern

class ReferrerTest {
    @Test
    fun refrerTest(){
//        var v="https://userndot.com/sdk/js/index.html"
        var v="http://myadvo-beta.tech"
//        var pattern= Pattern.compile("^(?<scheme>https?)(:\\/\\/)(?<host>\\w+(\\.\\w+)+\\/?)")
//        var matcher=pattern.matcher(v)
//        if(matcher.find()){
//            var scheme=matcher.group("scheme")
//            var host=matcher.group("host")
//            v="$scheme://$host"
//
//            println(v)
//        }else{
//            print("emd")
////            logger.info("Referer format not match $v")
//        }

        var c= arrayOf("http://myadvo-beta.tech/")
        print(isInDomains(c,v))

    }


    private fun isInDomains(url1s: Array<String>, url2: String): Boolean {
//        logger.error("Referer is $url2")
        url1s.forEach { if(matchDomains(it,url2)) return true }
        return false
    }

    private fun matchDomains(url1: String, url2: String): Boolean {
//        logger.error("Matching referer with domian $url1")
        try {
            val uri1 = URI(url1)
            val domain1 = uri1.host
            val domainWoWww1 = if (domain1.startsWith("www.")) domain1.substring(4) else domain1
            val scheme1 = uri1.scheme
            val uri2 = URI(url2)
            val domain2 = uri2.host
            val domainWoWww2 = if (domain2.startsWith("www.")) domain2.substring(4) else domain2
            val scheme2 = uri2.scheme
            return scheme1 == scheme2 && domainWoWww1 == domainWoWww2
        } catch (ex: URISyntaxException) {
            return false
        }
    }
}