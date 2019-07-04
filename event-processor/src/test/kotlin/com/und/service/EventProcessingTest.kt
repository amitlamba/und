package com.und.service

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
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
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.und.model.IncludeUsers
import com.und.model.Segment
import com.und.model.mongo.Metadata
import com.und.service.segmentquerybuilder.SegmentServiceImpl
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

//@RunWith(MockitoJUnitRunner::class)
class EventProcessingTest {

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()

    //@Mock
    //private lateinit var dateUtils:DateUtils

    @Mock
    private lateinit var ipLocationRepository:IpLocationRepository

    @Mock
    private lateinit var metadataRepository: MetadataRepository

//    @Mock
//    private lateinit var mongoEventUtils: MongoEventUtils

    @Mock
    private lateinit var streamClass:StreamClass

    @Mock
    private lateinit var segmentService:SegmentServiceImpl

    @InjectMocks
    private lateinit var eventSegmentProcessing: EventSegmentProcessing

    //@Mock
    private lateinit var objectMapper: ObjectMapper

    @Before
    fun setUp(){
        objectMapper = ObjectMapper()
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        val module = SimpleModule()
        module.addSerializer(LocalDateTime::class.java,CustomLocalDateTimeSerializer())
        module.addDeserializer(LocalDateTime::class.java,CustomLocalDateTimeDeserializer())
        module.addDeserializer(ZoneId::class.java,CustomZoneIdDeserializer())
        module.addSerializer(ZoneId::class.java,CustomZoneIdSerializer())
        objectMapper.registerModule(module)
        `when`(ipLocationRepository.getGeographyByIpAddress(anyString())).thenReturn(Geogrophy("India","Haryana","Gurgaon"))

        //injecting real object of utility classes
        val eventSegmentProcessingClass  = eventSegmentProcessing.javaClass
        val dateUtilsField = eventSegmentProcessingClass.getDeclaredField("dateUtils")
        dateUtilsField.isAccessible = true
        dateUtilsField.set(eventSegmentProcessing,DateUtils())

        val mongoUtilsFiled = eventSegmentProcessingClass.getDeclaredField("mongoEventUtils")
        mongoUtilsFiled.isAccessible = true
        mongoUtilsFiled.set(eventSegmentProcessing,MongoEventUtils())
//        `when`(mongoEventUtils.toDateInMap(any<HashMap<String,Any>>())).thenCallRealMethod()
//        `when`(dateUtils.getStartOfDay(anyString(), any<ZoneId>())).thenCallRealMethod()
//        `when`(dateUtils.getMidnight(anyString(), any<ZoneId>())).thenCallRealMethod()
    }

    @Test
    fun testProcessEvent1_Did_Metadata1(){
        var eventFile = File("src/test/resources/did_metadata/search_26.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)

        Assert.assertThat("Because for dead segment event have no effect on it.", isCheckEventEffectOnSegmentCalled, Matchers.`is`(false))
    }

    @Test
    fun testProcessEvent1_Did_Metadata2(){
        var eventFile = File("src/test/resources/did_metadata/search_26.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata2.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)

        Assert.assertThat("Segment is not dead and we expected that check event effect method is called",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, never())
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, never())
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent2_Did_Metadata2(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/search_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata2.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is called",isCheckEventEffectOnSegmentCalled,`is`(true))

        //this event happen on 02 but endDate is before start of 03 thats why this userid is going to add in
        verify(segmentService, times(1))
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, times(1))
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent3_Did_Metadata2(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/Add to Cart_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata2.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is called",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, never())
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, never())
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent1_Did_Metadata3(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/search_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata3.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is called",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, times(1))
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, times(1))
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent2_Did_Metadata3(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/Add to Cart_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata3.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is called",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, times(1))
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, times(1))
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent3_Did_Metadata3(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/support_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata3.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is called",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, never())
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, never())
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent1_Did_Metadata4(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/search_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata4.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is dead thats why we are not checking event effect on this segment",isCheckEventEffectOnSegmentCalled,`is`(false))

        verify(segmentService, never())
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, never())
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent1_Did_Metadata5(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/Add to Cart_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata5.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead thats why we are expecting check event effect method is called.",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, times(1))
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, times(1))
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent2_Did_Metadata5(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/support_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata5.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead thats why we are expecting check event effect method is called.",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, never())
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, never())
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent1_Did_Metadata6(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/Add to Cart_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata6.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead thats why we are expecting check event effect method is called.",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, times(1))
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, times(1))
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent2_Did_Metadata6(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/view_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata6.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead thats why we are expecting check event effect method is called.",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, never())
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, never())
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent1_Did_Metadata7(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/Add to Cart_03.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata7.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead thats why we are expecting check event effect method is called.",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, never())
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, never())
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent2_Did_Metadata7(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/Add to Cart_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata7.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead thats why we are expecting check event effect method is called.",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, times(1))
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, times(1))
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent3_Did_Metadata7(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/support_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata7.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead thats why we are expecting check event effect method is called.",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, times(1))
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, times(1))
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }

    @Test
    fun testProcessEvent4_Did_Metadata7(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment(anyString(), anyLong(), anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/did_metadata/search_02.json")
        val metadataFile = File("src/test/resources/did_metadata/did_metadata7.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead thats why we are expecting check event effect method is called.",isCheckEventEffectOnSegmentCalled,`is`(true))

        verify(segmentService, never())
                .addUserInSegment(anyString(), anyLong(), anyLong())
        verify(segmentService, never())
                .isUserPresentInSegment(any<Segment>(),anyLong(), any<IncludeUsers>(),eq(null), anyString())
    }



    private fun processEvent(eventJson:File,metadataJSon:File):Boolean{
        `when`(metadataRepository.findByClientIdAndTypeAndStopped(anyLong(), anyString(), anyBoolean())).thenReturn(listOf(objectMapper.readValue<Metadata>(metadataJSon)))
        var webEvent = objectMapper.readValue<Event>(eventJson)
        var event  = eventSegmentProcessing.buildMongoEvent(webEvent)
        val metadata = metadataRepository.findByClientIdAndTypeAndStopped(1,"all",false)
        //filter list of metadata select which are not dead(stopped)
        val filteredList = metadata.filter { !it.stopped }
        for (i in filteredList){
            eventSegmentProcessing.checkEventEffectOnSegment(event,i)
            return true
        }
        return false
    }


}

class CustomLocalDateTimeDeserializer:JsonDeserializer<LocalDateTime>(){
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): LocalDateTime {
        return p?.let {
            LocalDateTime.parse(it.text)
        }?:LocalDateTime.now()
    }
}

class CustomLocalDateTimeSerializer:JsonSerializer<LocalDateTime>(){
    override fun serialize(value: LocalDateTime?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }
}


class CustomZoneIdDeserializer:JsonDeserializer<ZoneId>(){
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ZoneId {
        return p?.let {
            ZoneId.of(it.text)
        }?:ZoneId.of("UTC")
    }
}

class CustomZoneIdSerializer:JsonSerializer<ZoneId>(){
    override fun serialize(value: ZoneId?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value?.id)
    }
}