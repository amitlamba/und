package com.und

import org.junit.Test
import java.net.URI
import java.util.regex.Pattern
import java.net.URISyntaxException



class ReferrerTest {
    @Test
    fun refrerTest(){
//        var v="http://userndot.com/sdk/js/index.html"
        var v="http://myadvo-beta.tech/"
        var domains= arrayOf("http://myadvo-beta.tech")
        print(isInDomains(domains,v))

    }

    @Throws(URISyntaxException::class)
    fun getDomainName(url: String): String {
        val uri = URI(url)
        val domain = uri.getHost()
        val scheme=uri.scheme
        return if (domain.startsWith("www.")) "${scheme}://"+domain.substring(4) else "${scheme}://${domain}"
    }



    private fun isInDomains(url1s: Array<String>, url2: String): Boolean {
        url1s.forEach { if(matchDomains(it,url2)) return true }
        return false
    }

    private fun matchDomains(url1: String, url2: String): Boolean {
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