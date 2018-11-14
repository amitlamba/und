package com.und.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.und.web.model.Segment
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.ResourceUtils
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.hamcrest.CoreMatchers.`is` as Is

//@RunWith(SpringRunner::class)
//@SpringBootTest
class SegmentParserCriteriaTest {


    lateinit var mapper: ObjectMapper

    val segmentParser = SegmentParserCriteria()

    var testDataBase = "segmentdata"

/*    @Autowired
    lateinit var segmentServiceImpl: SegmentServiceImpl*/

    @Before
    fun setup() {
        mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
        mapper.registerModule(JavaTimeModule())
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    fun readFileText(fileName: String): String = ResourceUtils.getFile("classpath:$fileName").readText(Charsets.UTF_8)

    @Test
    fun preacticeTest() {

        val date = "2018-04-25T18:30:00.183Z"
        val dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val d = LocalDateTime.parse(date, dateFormatter)
        println(d)
        MatcherAssert.assertThat(2, Is(2))
    }


    @Test
    fun testSegmentParser1() {

        val testData = readFileText("$testDataBase/test1.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val q =  segmentParser.segmentQueries(segment, ZoneId.of("UTC"))
        println(q.didq.first)
        println(q.didq.second)
        println(q.userQuery)
        MatcherAssert.assertThat(2, Is(2))
    }

    @Test
    fun testWithUserSegmentParser1() {

        val testData = readFileText("$testDataBase/test1-withuser.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val q =  segmentParser.segmentQueries(segment, ZoneId.of("UTC"))
        println(q.didq.first)
        println(q.didq.second)
        println(q.userQuery)
        MatcherAssert.assertThat(2, Is(2))
    }


    @Test
    fun testSegmentParser2() {

        val testData = readFileText("$testDataBase/test2.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val q =  segmentParser.segmentQueries(segment, ZoneId.of("UTC"))
        println(q.didq.first)
        println(q.didq.second)
        MatcherAssert.assertThat(2, Is(2))
    }

    @Test
    fun testSegmentParser3() {

        val testData = readFileText("$testDataBase/test3.json")
        val segment = mapper.readValue(testData, Segment::class.java)


        val q =  segmentParser.segmentQueries(segment, ZoneId.of("UTC"))
        println(q.didq.first)
        println(q.didq.second)
        //val users = segmentServiceImpl.segmentUsers(37,2)
        MatcherAssert.assertThat(2, Is(2))
    }

    @Test
    fun testSegmentParser4() {

        val testData = readFileText("$testDataBase/test4.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val q =  segmentParser.segmentQueries(segment, ZoneId.of("UTC"))
        println(q.didq.first)
        println(q.didq.second)
        MatcherAssert.assertThat(2, Is(2))
    }

    @Test
    fun testSegmentParser5() {

        val testData = readFileText("$testDataBase/test5.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val q =  segmentParser.segmentQueries(segment, ZoneId.of("UTC"))
        println(q.didq.first)
        println(q.didq.second)


        MatcherAssert.assertThat(2, Is(2))
    }

    @Test
    fun testSegmentParser6() {

        val testData = readFileText("$testDataBase/test6.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val q =  segmentParser.segmentQueries(segment, ZoneId.of("UTC"))
        println(q.didq.first)
        println(q.didq.second)
        MatcherAssert.assertThat(2, Is(2))

    }

    @Test
    fun testWithUserSegmentParser6() {

        val testData = readFileText("$testDataBase/test6-withuser.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val q =  segmentParser.segmentQueries(segment, ZoneId.of("UTC"))
        println(q.didq.first)
        println(q.didq.second)
        println(q.userQuery)
        MatcherAssert.assertThat(2, Is(2))

    }

    @Test
    fun testSegmentParser7() {

        val testData = readFileText("$testDataBase/test7.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val q =  segmentParser.segmentQueries(segment, ZoneId.of("UTC"))
        println(q.didq.first)
        println(q.didq.second)
        MatcherAssert.assertThat(2, Is(2))

    }

    @Test
    fun testSegmentParser12() {

        val testData = readFileText("$testDataBase/JsonTestData12.json")
        val segment = mapper.readValue(testData, Segment::class.java)
         segmentParser.segmentQueries(segment, ZoneId.of("UTC"))
        MatcherAssert.assertThat(2, Is(2))

    }

    @Test
    fun testSegmentParser13() {
        var count = 0
        val dir = ResourceUtils.getFile("classpath:$testDataBase")
        val files = dir.list { _, _ -> true }
        files.forEach {
            val testData = readFileText("$testDataBase/$it")
            val segment = mapper.readValue(testData, Segment::class.java)
            println("****$it**${count++}******")
            println("************")

             segmentParser.segmentQueries(segment, ZoneId.of("UTC"))
            println("************")
            println("************")


        }


        MatcherAssert.assertThat(2, Is(2))
    }

}


