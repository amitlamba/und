package com.und

import com.und.model.jpa.AmPm
import com.und.model.jpa.CampaignTime
import org.junit.Test
import java.net.URI
import java.util.regex.Pattern
import java.net.URISyntaxException
import java.sql.Timestamp
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


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

    @Test
    fun toCampaignTime() {
        val date=LocalDateTime.of(2019,3,13,11,59)
        val minutes=date.minute
        val hours=date.hour //24 hours
        val dates=date.toLocalDate()
        var aMpM = when(hours){
            in 0 .. 11 -> AmPm.AM
            else -> AmPm.PM
        }
        val campaignTime=CampaignTime()
        campaignTime.ampm= aMpM
        campaignTime.hours=hours
        campaignTime.minutes=minutes
        campaignTime.date=dates
        print("minutes $minutes hours $hours date $dates ampm $aMpM")
    }

    @Test
    fun testDateTimeFormatter(){
        println( LocalDateTime.now(ZoneId.of("UTC")).toString())
        var v= OffsetDateTime.now(ZoneId.systemDefault()).offset
        var date=Date().toInstant().atOffset(v).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        println(date)
        val newDate=LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .plusSeconds(60).atOffset(v).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        println(newDate)
    }
}