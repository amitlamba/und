package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.und.config.StreamClass
import com.und.model.mongo.Geogrophy
import com.und.model.web.Event
import com.und.model.web.Identity
import com.und.repository.mongo.IpLocationRepository
import com.und.repository.mongo.MetadataRepository
import com.und.service.segmentquerybuilder.SegmentService
import com.und.utils.DateUtils
import com.und.utils.MongoEventUtils
import org.junit.Before
import org.junit.Rule
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.hamcrest.Matchers.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.time.ZoneId

@RunWith(MockitoJUnitRunner::class)
class EventProcessingTest {

//    @Rule
//    var mockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var dateUtils:DateUtils

    @Mock
    private lateinit var ipLocationRepository:IpLocationRepository

    @Mock
    private lateinit var metadataRepository: MetadataRepository

    @Mock
    private lateinit var mongoEventUtils: MongoEventUtils

    @Mock
    private lateinit var streamClass:StreamClass

    @Mock
    private lateinit var segmentService:SegmentService

    @InjectMocks
    private lateinit var eventSegmentProcessing: EventSegmentProcessing

    private lateinit var objectMapper: ObjectMapper

    @Before
    fun setUp(){
        objectMapper = ObjectMapper()
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        `when`(ipLocationRepository.getGeographyByIpAddress(anyString())).thenReturn(Geogrophy("India","Haryana","Gurgaon"))
//        //`when`(mongoEventUtils.toDateInMap(ArgumentMatchers.any(java.util.HashMap<String,Any>::class.java))).thenCallRealMethod()
//        `when`(dateUtils.getStartOfDay(anyString(), any(ZoneId::class.java))).thenCallRealMethod()
//        `when`(dateUtils.getMidnight(anyString(), any(ZoneId::class.java))).thenCallRealMethod()
    }

    @Test
    fun testProcessEvent1Segment1(){
        //read event
        //read metadata
        var eventFile = File("src/test/resources/did_metadata1/event1.json")
        var webEvent = objectMapper.readValue<Event>(eventFile)

        //var metadataList =
        `when`(metadataRepository.findByClientIdAndTypeAndStopped(anyLong(), anyString(), anyBoolean())).thenReturn(listOf())
        var event  = eventSegmentProcessing.buildMongoEvent(webEvent)
        val metadata = metadataRepository.findByClientIdAndTypeAndStopped(1,"all",false)

        for (i in metadata){
            eventSegmentProcessing.checkEventEffectOnSegment(event,i)
        }

//        verify(segmentService, times(1))
//                .addUserInSegment(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong())
//        verify(segmentService, never())
//                .addUserInSegment(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong())
//        verify(segmentService, times(1))
//                .isUserPresentInSegment(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())


    }

}