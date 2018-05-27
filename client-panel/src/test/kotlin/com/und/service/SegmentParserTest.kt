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
import org.hamcrest.CoreMatchers.`is` as Is


class SegmentParserTest {


    lateinit var mapper: ObjectMapper

    var testDataBase = "segmentdata"



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
    fun testSegmentParser1() {

        val testData = readFileText("$testDataBase/test1.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val parsedResponse = SegmentParser().segmentQueries(segment)
        MatcherAssert.assertThat(2, Is(2))
    }


    @Test
    fun testSegmentParser2() {

        val testData = readFileText("$testDataBase/test2.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val parsedResponse = SegmentParser().segmentQueries(segment)
        MatcherAssert.assertThat(2, Is(2))
    }

    @Test
    fun testSegmentParser3() {

        val testData = readFileText("$testDataBase/test3.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val parsedResponse = SegmentParser().segmentQueries(segment)


        MatcherAssert.assertThat(2, Is(2))
    }

    @Test
    fun testSegmentParser4() {

        val testData = readFileText("$testDataBase/test4.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val parsedResponse = SegmentParser().segmentQueries(segment)
        MatcherAssert.assertThat(2, Is(2))
    }

    @Test
    fun testSegmentParser5() {

        val testData = readFileText("$testDataBase/test5.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val parsedResponse = SegmentParser().segmentQueries(segment)
        MatcherAssert.assertThat(2, Is(2))
    }

    @Test
    fun testSegmentParser6() {

        val testData = readFileText("$testDataBase/test6.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val parsedResponse = SegmentParser().segmentQueries(segment)
        MatcherAssert.assertThat(2, Is(2))

    }

    @Test
    fun testSegmentParser12() {

        val testData = readFileText("$testDataBase/JsonTestData12.json")
        val segment = mapper.readValue(testData, Segment::class.java)
        val parsedResponse = SegmentParser().segmentQueries(segment)
        MatcherAssert.assertThat(2, Is(2))

    }

    @Test
    fun testSegmentParser7() {
        var count = 0
        val dir = ResourceUtils.getFile("classpath:$testDataBase")
        val files = dir.list { dir, name -> true }
        files.forEach {
            val testData = readFileText("$testDataBase/$it")
            val segment = mapper.readValue(testData, Segment::class.java)
            println("****$it**${count++}******")
            println("************")

            val parsedResponse = SegmentParser().segmentQueries(segment)
            println("************")
            println("************")


        }


        MatcherAssert.assertThat(2, Is(2))
    }

}


