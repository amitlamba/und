package com.und.service

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.und.model.mongo.TriggerPoint
import com.und.web.model.Segment
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import com.und.model.jpa.Segment as JpaSegment
import java.lang.reflect.Method
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CreateMetadataServiceTest{
    //    private lateinit var segment1:String
//
//    private lateinit var segment2:String
//
//    private lateinit var segment3:String
//
//    private lateinit var segment4:String
//
//    private lateinit var segment5:String
//
//    private lateinit var segment6:String
//
//    private lateinit var segment7:String
//
//    private lateinit var segment8:String
//
//    private lateinit var segment9:String
//
//    private lateinit var segment10:String
    private lateinit var eventProcessingService:CreateMetadataService
    private lateinit var objectMapper: ObjectMapper
    private lateinit var creationDate: LocalDateTime
    private lateinit var segmentService:SegmentService

    companion object {
        @JvmStatic
        @BeforeClass
        fun beforeClass(){
            println("before class")
        }
    }
    @Before
    fun setUp() {
        eventProcessingService = CreateMetadataService()
        segmentService = SegmentServiceImpl()
        objectMapper = ObjectMapper()
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        creationDate = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(20,0))

//        val file1 = File("src/test/resources/segment1.json")
//        val file2 = File("src/test/resources/segment2.json")
//        val file3 = File("src/test/resources/segment3.json")
//        val file4 = File("src/test/resources/segment4.json")
//        val file5 = File("src/test/resources/segment5.json")
//        val file6 = File("src/test/resources/segment6.json")
//        val file7 = File("src/test/resources/segment7.json")
//        val file8 = File("src/test/resources/segment8.json")
//        val file9 = File("src/test/resources/segment9.json")
//        val file10 = File("src/test/resources/segment10.json")
//
//        segment1 = "{\"liveSegment\":null,\"didEvents\":{\"joinCondition\":{\"conditionType\":\"AllOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"On\",\"values\":[\"2019-06-24\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}}]},\"didNotEvents\":{\"joinCondition\":{\"conditionType\":\"AnyOf\",\"anyOfCount\":null},\"events\":[]},\"globalFilters\":[],\"geographyFilters\":[],\"type\":\"Behaviour\"}"
//        segment2 = "{\"liveSegment\":null,\"didEvents\":{\"joinCondition\":{\"conditionType\":\"AllOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"On\",\"values\":[\"2019-06-24\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}},{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"After\",\"values\":[\"2019-06-24\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}}]},\"didNotEvents\":{\"joinCondition\":{\"conditionType\":\"AnyOf\",\"anyOfCount\":null},\"events\":[]},\"globalFilters\":[],\"geographyFilters\":[],\"type\":\"Behaviour\"}"
//        segment3 = "{\"liveSegment\":null,\"didEvents\":{\"joinCondition\":{\"conditionType\":\"AllOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"On\",\"values\":[\"2019-06-24\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}},{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"After\",\"values\":[\"2019-06-24\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}}]},\"didNotEvents\":{\"joinCondition\":{\"conditionType\":\"AnyOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"WasExactly\",\"values\":[1],\"valueUnit\":\"days\"},\"propertyFilters\":[],\"whereFilter\":{}}]},\"globalFilters\":[],\"geographyFilters\":[],\"type\":\"Behaviour\"}"
//        segment4 = "{\"liveSegment\":null,\"didEvents\":{\"joinCondition\":{\"conditionType\":\"AllOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"On\",\"values\":[\"2019-06-24\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}},{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"After\",\"values\":[\"2019-06-24\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}}]},\"didNotEvents\":{\"joinCondition\":{\"conditionType\":\"AnyOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"WasExactly\",\"values\":[1],\"valueUnit\":\"days\"},\"propertyFilters\":[],\"whereFilter\":{}}]},\"globalFilters\":[{\"globalFilterType\":\"UserProperties\",\"name\":\"userType\",\"type\":\"string\",\"operator\":\"Equals\",\"values\":[\"Gold Package\"]}],\"geographyFilters\":[],\"type\":\"Behaviour\"}"
//        segment5 = "{\"liveSegment\":null,\"didEvents\":{\"joinCondition\":{\"conditionType\":\"AllOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"On\",\"values\":[\"2019-06-24\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}},{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"After\",\"values\":[\"2019-06-24\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}}]},\"didNotEvents\":{\"joinCondition\":{\"conditionType\":\"AnyOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"WasExactly\",\"values\":[1],\"valueUnit\":\"days\"},\"propertyFilters\":[],\"whereFilter\":{}},{\"name\":\"App Uninstalled\",\"dateFilter\":{\"operator\":\"InThePast\",\"values\":[1],\"valueUnit\":\"hours\"},\"propertyFilters\":[],\"whereFilter\":{}}]},\"globalFilters\":[{\"globalFilterType\":\"UserProperties\",\"name\":\"userType\",\"type\":\"string\",\"operator\":\"Equals\",\"values\":[\"Gold Package\"]}],\"geographyFilters\":[{\"country\":null,\"state\":null,\"city\":null}],\"type\":\"Behaviour\"}"
//        segment6 = "{\"liveSegment\":null,\"didEvents\":{\"joinCondition\":{\"conditionType\":\"AllOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"On\",\"values\":[\"2019-06-18\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}}]},\"didNotEvents\":{\"joinCondition\":{\"conditionType\":\"AnyOf\",\"anyOfCount\":null},\"events\":[]},\"globalFilters\":[{\"globalFilterType\":\"UserProperties\",\"name\":\"userType\",\"type\":\"string\",\"operator\":\"Equals\",\"values\":[\"Gold Package\"]}],\"geographyFilters\":[],\"type\":\"Behaviour\"}"
//        segment7 = "{\"liveSegment\":null,\"didEvents\":{\"joinCondition\":{\"conditionType\":\"AllOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"On\",\"values\":[\"2019-06-18\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}}]},\"didNotEvents\":{\"joinCondition\":{\"conditionType\":\"AnyOf\",\"anyOfCount\":null},\"events\":[]},\"globalFilters\":[{\"globalFilterType\":\"Technographics\",\"name\":\"Browser\",\"type\":\"string\",\"operator\":\"Equals\",\"values\":[\"Mobile Application\"]}],\"geographyFilters\":[],\"type\":\"Behaviour\"}"
//        segment8 = "{\"liveSegment\":null,\"didEvents\":{\"joinCondition\":{\"conditionType\":\"AllOf\",\"anyOfCount\":null},\"events\":[]},\"didNotEvents\":{\"joinCondition\":{\"conditionType\":\"AnyOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"Between\",\"values\":[\"2019-06-24\",\"2019-06-24\"]},\"propertyFilters\":[],\"whereFilter\":{}}]},\"globalFilters\":[{\"globalFilterType\":\"Technographics\",\"name\":\"Browser\",\"type\":\"string\",\"operator\":\"Equals\",\"values\":[\"Mobile Application\"]}],\"geographyFilters\":[],\"type\":\"Behaviour\"}"
//        segment9 = "{\"liveSegment\":null,\"didEvents\":{\"joinCondition\":{\"conditionType\":\"AllOf\",\"anyOfCount\":null},\"events\":[]},\"didNotEvents\":{\"joinCondition\":{\"conditionType\":\"AnyOf\",\"anyOfCount\":null},\"events\":[]},\"globalFilters\":[{\"globalFilterType\":\"Technographics\",\"name\":\"Browser\",\"type\":\"string\",\"operator\":\"Equals\",\"values\":[\"Mobile Application\"]}],\"geographyFilters\":[],\"type\":\"Behaviour\"}"
//        segment10 = "{\"liveSegment\":null,\"didEvents\":{\"joinCondition\":{\"conditionType\":\"AllOf\",\"anyOfCount\":null},\"events\":[]},\"didNotEvents\":{\"joinCondition\":{\"conditionType\":\"AnyOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"InThePast\",\"values\":[40],\"valueUnit\":\"mins\"},\"propertyFilters\":[],\"whereFilter\":{}},{\"name\":\"App Uninstalled\",\"dateFilter\":{\"operator\":\"InThePast\",\"values\":[3],\"valueUnit\":\"hours\"},\"propertyFilters\":[],\"whereFilter\":{}},{\"name\":\"Added to cart\",\"dateFilter\":{\"operator\":\"InThePast\",\"values\":[1],\"valueUnit\":\"days\"},\"propertyFilters\":[],\"whereFilter\":{}}]},\"globalFilters\":[],\"geographyFilters\":[],\"type\":\"Behaviour\"}"
//
//        file1.writeText(segment1,Charsets.UTF_8)
//        file2.writeText(segment2,Charsets.UTF_8)
//        file3.writeText((segment3),Charsets.UTF_8)
//        file4.writeText((segment4),Charsets.UTF_8)
//        file5.writeText((segment5),Charsets.UTF_8)
//        file6.writeText((segment6),Charsets.UTF_8)
//        file7.writeText((segment7),Charsets.UTF_8)
//        file8.writeText((segment8),Charsets.UTF_8)
//        file9.writeText((segment9),Charsets.UTF_8)
//        file10.writeText((segment10),Charsets.UTF_8)



        //print(objectMapper.writeValueAsString(segment1))

    }
    //did and not dead no trigger point
    @Test
    fun test1(){
        val file1 = File("src/test/resources/segment1.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        createMetadataAndPrintTrigerPoint(segment)
    }
    //did and not dead no triggerpoint
    @Test
    fun test2(){
        val file1 = File("src/test/resources/segment2.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        createMetadataAndPrintTrigerPoint(segment)
    }
    //did and did not and not dead have trigger point every day
    @Test
    fun test3(){
        val file1 = File("src/test/resources/segment3.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        createMetadataAndPrintTrigerPoint(segment)
    }
    //did didnot user not dead have trigger point every day
    @Test
    fun test4(){
        val file1 = File("src/test/resources/segment4.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        createMetadataAndPrintTrigerPoint(segment)
    }
    //did didnot user event not dead have trigger point every hour every day
    @Test
    fun test5(){
        val file1 = File("src/test/resources/segment5.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        segment.creationDate = creationDate
        val expectedOutput = mutableListOf<LocalDateTime>()
        val date6 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(1,0))
        val date2 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(21,0))
        val date3 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(22,0))
        val date4 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(23,0))
        val date5 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(0,0))
        expectedOutput.add(date2)
        expectedOutput.add(date3)
        expectedOutput.add(date4)
        expectedOutput.add(date5)
        expectedOutput.add(date6)
        val output = createMetadataAndPrintTrigerPoint(segment,expectedOutput.size)
        Assert.assertThat(expectedOutput, CoreMatchers.`is`(output))
    }
    //did user not dead trigger point null
    @Test
    fun test6(){
        val file1 = File("src/test/resources/segment6.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        createMetadataAndPrintTrigerPoint(segment)
    }
    //did eventprop dead trigger point null
    @Test
    fun test7(){
        val file1 = File("src/test/resources/segment7.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        createMetadataAndPrintTrigerPoint(segment)
    }
    //did not event not dead trigger point null
    @Test
    fun test8(){
        val file1 = File("src/test/resources/segment8.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        createMetadataAndPrintTrigerPoint(segment)
    }
    //event prop not dead trigger point null
    @Test
    fun test9(){
        val file1 = File("src/test/resources/segment9.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        createMetadataAndPrintTrigerPoint(segment)
    }
    //did not not dead trigger point 40 min,3hour,every day
    @Test
    fun test10(){
        val file1 = File("src/test/resources/segment10.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        segment.creationDate = creationDate
        val expectedOutput = mutableListOf<LocalDateTime>()
        val date1 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(20,40))
        val date2 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(21,20))
        val date3 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(22,0))
        val date4 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(22,40))
        val date5 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(23,0))
        val date6 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(23,20))
        val date7 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(0,0))
        val date8 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(0,40))
        val date9 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(1,20))
        val date10 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(2,0))
        val date11 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(2,40))
        expectedOutput.add(date1)
        expectedOutput.add(date2)
        expectedOutput.add(date3)
        expectedOutput.add(date4)
        expectedOutput.add(date5)
        expectedOutput.add(date6)
        expectedOutput.add(date7)
        expectedOutput.add(date8)
        expectedOutput.add(date9)
        expectedOutput.add(date10)
        expectedOutput.add(date11)


        val output = createMetadataAndPrintTrigerPoint(segment,expectedOutput.size)
        Assert.assertThat(expectedOutput, CoreMatchers.`is`(output))

    }

    //90 min , 7  hour ,  every day
    @Test
    fun test11(){
        val file1 = File("src/test/resources/segment11.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        segment.creationDate = creationDate
        val expectedOutput = mutableListOf<LocalDateTime>()
        val date1 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(21,30))
        val date2 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(23,0))
        val date3 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(0,0))
        val date4 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(0,30))
        val date5 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(2,0))
        val date18 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(3,0))
        val date6 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(3,30))
        val date7 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(5,0))
        val date8 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(6,30))
        val date9 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(8,0))
        val date10 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(9,30))
        val date12 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(10,0))
        val date13 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(11,0))
        val date14 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(12,30))
        val date15 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(14,0))
        val date16 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(15,30))
        val date17 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(17,0))

        expectedOutput.add(date1)
        expectedOutput.add(date2)
        expectedOutput.add(date3)
        expectedOutput.add(date4)
        expectedOutput.add(date5)
        expectedOutput.add(date18)
        expectedOutput.add(date6)
        expectedOutput.add(date7)
        expectedOutput.add(date8)
        expectedOutput.add(date9)
        expectedOutput.add(date10)
        expectedOutput.add(date12)
        expectedOutput.add(date13)
        expectedOutput.add(date14)
        expectedOutput.add(date15)
        expectedOutput.add(date16)
        expectedOutput.add(date17)


        val output = createMetadataAndPrintTrigerPoint(segment,expectedOutput.size)
        Assert.assertThat(expectedOutput, CoreMatchers.`is`(output))
    }

    // 7 hour and today
    @Test
    fun test12(){
        val file1 = File("src/test/resources/segment12.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        segment.creationDate = creationDate
        val expectedOutput = mutableListOf<LocalDateTime>()
        val date1 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(0,0))
        val date2 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(3,0))
        val date3 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(10,0))
        val date4 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(17,0))
        val date5 = LocalDateTime.of(LocalDate.parse("2019-06-22"), LocalTime.of(0,0))
        val date18 = LocalDateTime.of(LocalDate.parse("2019-06-22"), LocalTime.of(7,0))
        val date6 = LocalDateTime.of(LocalDate.parse("2019-06-22"), LocalTime.of(14,0))
        val date7 = LocalDateTime.of(LocalDate.parse("2019-06-22"), LocalTime.of(21,0))
        val date8 = LocalDateTime.of(LocalDate.parse("2019-06-23"), LocalTime.of(0,0))
        val date9 = LocalDateTime.of(LocalDate.parse("2019-06-23"), LocalTime.of(4,0))

        expectedOutput.add(date1)
        expectedOutput.add(date2)
        expectedOutput.add(date3)
        expectedOutput.add(date4)
        expectedOutput.add(date5)
        expectedOutput.add(date18)
        expectedOutput.add(date6)
        expectedOutput.add(date7)
        expectedOutput.add(date8)
        expectedOutput.add(date9)


        val output = createMetadataAndPrintTrigerPoint(segment,expectedOutput.size)
        Assert.assertThat(expectedOutput, CoreMatchers.`is`(output))
    }

    //35 min , 55 min
    @Test
    fun test13(){
        val file1 = File("src/test/resources/segment13.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        segment.creationDate = creationDate
        val expectedOutput = mutableListOf<LocalDateTime>()
        val date1 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(20,35))
        val date2 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(20,55))
        val date3 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(21,10))
        val date4 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(21,45))
        val date5 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(21,50))
        val date18 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(22,20))
        val date6 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(22,45))
        val date7 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(22,55))
        val date8 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(23,30))
        val date9 = LocalDateTime.of(LocalDate.parse("2019-06-20"), LocalTime.of(23,40))
        val date10 = LocalDateTime.of(LocalDate.parse("2019-06-21"), LocalTime.of(0,5))
        val date12 = getDate("2019-06-21",0,35)
        val date13 = getDate("2019-06-21",0,40)
        val date14 = getDate("2019-06-21",1,15)
        val date15 = getDate("2019-06-21",1,30)
        val date16 = getDate("2019-06-21",1,50)

        expectedOutput.add(date1)
        expectedOutput.add(date2)
        expectedOutput.add(date3)
        expectedOutput.add(date4)
        expectedOutput.add(date5)
        expectedOutput.add(date18)
        expectedOutput.add(date6)
        expectedOutput.add(date7)
        expectedOutput.add(date8)
        expectedOutput.add(date9)
        expectedOutput.add(date10)
        expectedOutput.add(date12)
        expectedOutput.add(date13)
        expectedOutput.add(date14)
        expectedOutput.add(date15)
        expectedOutput.add(date16)


        val output = createMetadataAndPrintTrigerPoint(segment,expectedOutput.size)
        Assert.assertThat(expectedOutput, CoreMatchers.`is`(output))
    }
    // 7 hours 15 hours,every day
    @Test
    fun test14(){
        val file1 = File("src/test/resources/segment14.json")
        val jsonSegment = file1.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)
        segment.creationDate = LocalDateTime.of(LocalDate.parse("2019-06-22"), LocalTime.of(19,0))
        val expectedOutput = mutableListOf<LocalDateTime>()
        val date1 = LocalDateTime.of(LocalDate.parse("2019-06-23"), LocalTime.of(0,0))
        val date2 = LocalDateTime.of(LocalDate.parse("2019-06-23"), LocalTime.of(2,0))
        val date3 = LocalDateTime.of(LocalDate.parse("2019-06-23"), LocalTime.of(9,0))
        val date4 = LocalDateTime.of(LocalDate.parse("2019-06-23"), LocalTime.of(10,0))
        val date5 = LocalDateTime.of(LocalDate.parse("2019-06-23"), LocalTime.of(16,0))
        val date18 = LocalDateTime.of(LocalDate.parse("2019-06-23"), LocalTime.of(23,0))
        val date6 = LocalDateTime.of(LocalDate.parse("2019-06-24"), LocalTime.of(0,0))
        val date7 = LocalDateTime.of(LocalDate.parse("2019-06-24"), LocalTime.of(1,0))
        val date8 = LocalDateTime.of(LocalDate.parse("2019-06-24"), LocalTime.of(6,0))
        val date9 = LocalDateTime.of(LocalDate.parse("2019-06-24"), LocalTime.of(13,0))
        val date10 = LocalDateTime.of(LocalDate.parse("2019-06-24"), LocalTime.of(16,0))
        val date12 = getDate("2019-06-24",20,0)
        val date13 = getDate("2019-06-25",0,0)
        val date14 = getDate("2019-06-25",3,0)
        val date15 = getDate("2019-06-25",7,0)
        val date16 = getDate("2019-06-25",10,0)

        expectedOutput.add(date1)
        expectedOutput.add(date2)
        expectedOutput.add(date3)
        expectedOutput.add(date4)
        expectedOutput.add(date5)
        expectedOutput.add(date18)
        expectedOutput.add(date6)
        expectedOutput.add(date7)
        expectedOutput.add(date8)
        expectedOutput.add(date9)
        expectedOutput.add(date10)
        expectedOutput.add(date12)
        expectedOutput.add(date13)
        expectedOutput.add(date14)
        expectedOutput.add(date15)
        expectedOutput.add(date16)


        val output = createMetadataAndPrintTrigerPoint(segment,expectedOutput.size)
        Assert.assertThat(expectedOutput, CoreMatchers.`is`(output))
    }

    private fun getDate(date:String,hour:Int,min:Int): LocalDateTime {
        return LocalDateTime.of(LocalDate.parse(date), LocalTime.of(hour,min))
    }
    private fun createMetadataAndPrintTrigerPoint(segment: Segment, runNo:Int=20):List<LocalDateTime> {
        val metadata = eventProcessingService.createSegmentMetadata(segment, 111, 11, "past")
        val triggerInfo = metadata.triggerInfo
        val result = mutableListOf<LocalDateTime>()
        println("criteria group ${metadata.criteriaGroup}")
        //println("trigger info ${triggerInfo?.nextTriggerPoint}")
        println("consider this segment ${!metadata.stopped}")
        triggerInfo?.let {
            for (i in 1..runNo) {
                val date = eventProcessingService.findNextTriggerPoint(triggerInfo.triggerPoint, triggerInfo.timeZoneId)
                println(date.first)
                result.add(date.first)                //contain name of those trigger point which cause for schedule. it may be one or multiple(multiple when two or more trigger point time is same.)
                val trigger = date.second

                trigger.forEach { name ->
                    //finding the tigger point whose last execution point should be changed.
                    val result: List<TriggerPoint> = triggerInfo.triggerPoint.filter {
                        it.name == name
                    }
                    //updating the last execution date of those trigger point.
                    result.forEach {
                        it.lastExecutionPoint = date.first
                    }
                }
            }
        }

        return result
    }
}