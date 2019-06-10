package com.und.report.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.escape.Escaper
import com.google.common.html.HtmlEscapers
import com.und.report.model.EventChronology
import com.und.report.model.FunnelData
import com.und.report.model.UserData
import com.und.report.web.model.FunnelReport
import com.und.service.SegmentParserCriteria
import com.und.web.model.GlobalFilterType
//import jdk.internal.org.objectweb.asm.TypeReference
//import jdk.nashorn.internal.parser.JSONParser
import org.apache.commons.lang.StringEscapeUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.test.util.ReflectionTestUtils
import zipkin2.internal.JsonEscaper
import java.time.ZoneId

@RunWith(MockitoJUnitRunner::class)
class AWSFunnelLambdaInvokerTest {

    private lateinit var awsFunnelLambdaInvoker: AWSFunnelLambdaInvoker

    private lateinit var mapper: ObjectMapper

    @Before
    fun setup(){
        awsFunnelLambdaInvoker = AWSFunnelLambdaInvoker()
        mapper = ObjectMapper()
    }

    @Test
    fun testWithoutSplit(){

        val userData = UserData()
        userData.userId = "testuser"
        userData.chronologies = listOf(EventChronology("Search", "all", listOf(10, 20, 30, 40, 50)),
                EventChronology("View", "all", listOf(15, 25, 45, 65)),
                EventChronology("Charged", "all", listOf(30, 50, 70)))

        val funnelData = FunnelData(listOf(userData), listOf("Search", "View", "Charged"), 30)

        val funnels = awsFunnelLambdaInvoker.computeFunnels(funnelData)

        if(funnels != null) {
            println(funnels)
        } else {
            println("Test failed")
        }
    }
}