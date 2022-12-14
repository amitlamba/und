package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.und.config.StreamClass
import com.und.model.IncludeUsers
import com.und.model.Segment
import com.und.model.mongo.Geogrophy
import com.und.model.mongo.Metadata
import com.und.model.web.Event
import com.und.repository.mongo.IpLocationRepository
import com.und.repository.mongo.MetadataRepository
import com.und.service.segmentquerybuilder.SegmentServiceImpl
import com.und.utils.DateUtils
import com.und.utils.MongoEventUtils
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Answers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito. *
import org.mockito.junit.MockitoJUnit
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId

class EventProcessingDidNotTest {

    @get:Rule
    val runner = MockitoJUnit.rule()

    @Mock
    private lateinit var ipLocationRepository: IpLocationRepository

    @Mock
    private lateinit var metadataRepository: MetadataRepository

    @Mock
    private lateinit var streamClass: StreamClass

    @Mock
    private lateinit var segmentService: SegmentServiceImpl

    @InjectMocks
    private lateinit var eventSegmentProcessing: EventSegmentProcessing


    lateinit var objectMapper:ObjectMapper
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

         `when`(ipLocationRepository.getGeographyByIpAddress( anyString())).thenReturn(Geogrophy("India","Haryana","Gurgaon"))

        //injecting real object of utility classes
        val eventSegmentProcessingClass  = eventSegmentProcessing.javaClass
        val dateUtilsField = eventSegmentProcessingClass.getDeclaredField("dateUtils")
        dateUtilsField.isAccessible = true
        dateUtilsField.set(eventSegmentProcessing, DateUtils())

        val mongoUtilsFiled = eventSegmentProcessingClass.getDeclaredField("mongoEventUtils")
        mongoUtilsFiled.isAccessible = true
        mongoUtilsFiled.set(eventSegmentProcessing, MongoEventUtils())
    }

    @Test
    fun testProcessEvent1_DidNot_Metadata1(){
         `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(true)
         `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/events/search_02.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata1.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is dead and we expected that check event effect method is not called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(false))

         verify(segmentService,  times(0))
                .addUserInSegment( anyString(),  anyLong(),  anyLong())
         verify(segmentService,  times(0))
                .isUserPresentInSegment(any<Segment>(),  anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }

    @Test
    fun testProcessEvent1_DidNot_Metadata2(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/events/search_02.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata2.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is dead and we expected that check event effect method is not called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(false))

        verify(segmentService,  times(0))
                .addUserInSegment( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(0))
                .isUserPresentInSegment(any<Segment>(),  anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }

    @Test
    fun testProcessEvent1_DidNot_Metadata3(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.removeUserFromSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/events/search_02.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata3.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is  called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(true))

        verify(segmentService,  times(1))
                .removeUserFromSegment( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(0))
                .isUserPresentInSegment(any<Segment>(),  anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }

    @Test
    fun testProcessEvent2_DidNot_Metadata3(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.removeUserFromSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/events/Add to Cart_02.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata3.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is  called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(true))

        verify(segmentService,  times(1))
                .addUserInSegment( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(0))
                .isUserPresentInSegment(any<Segment>(),  anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }

    @Test
    fun testProcessEvent1_DidNot_Metadata4(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.removeUserFromSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)

        var eventFile = File("src/test/resources/events/search_02.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata4.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is  called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(true))

        verify(segmentService,  times(1))
                .addUserInSegment( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(0))
                .isUserPresentInSegment(any<Segment>(), anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }

    @Test
    fun testProcessEvent2_DidNot_Metadata4(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.removeUserFromSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.isUserPresent( anyString(),  anyLong(),  anyLong())).thenReturn(true)

        var eventFile = File("src/test/resources/events/view_02.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata4.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is  called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(true))

        verify(segmentService,  times(1))
                .addUserInSegment( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(1))
                .isUserPresent( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(1))
                .isUserPresentInSegment(any<Segment>(), anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }

    @Test
    fun testProcessEvent1_DidNot_Metadata5(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.removeUserFromSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.isUserPresent( anyString(),  anyLong(),  anyLong())).thenReturn(true)

        var eventFile = File("src/test/resources/events/support_02.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata5.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is  called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(true))

        //if isUserPResnt is true removeUserFromSegment is not called. else called
        verify(segmentService,  times(0))
                .removeUserFromSegment( anyString(),  anyLong(),  anyLong())

        verify(segmentService,  times(1))
                .isUserPresent( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(1))
                .isUserPresentInSegment(any<Segment>(), anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }

    @Test
    fun testProcessEvent1_User1_DidNot_Metadata6(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(false)
        `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.removeUserFromSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.isUserPresent( anyString(),  anyLong(),  anyLong())).thenReturn(true)

        var eventFile = File("src/test/resources/events/search_04.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata6.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile,"user1")
        Assert.assertThat("Segment is not dead and we expected that check event effect method is  called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(true))

        //if isUserPResnt is true removeUserFromSegment is not called. else called
        verify(segmentService,  times(1))
                .removeUserFromSegment( anyString(),  anyLong(),  anyLong())

        verify(segmentService,  times(1))
                .isUserPresent( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(1))
                .isUserPresentInSegment(any<Segment>(), anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }

    @Test
    fun testProcessEvent1_User2_DidNot_Metadata6(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.removeUserFromSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.isUserPresent( anyString(),  anyLong(),  anyLong())).thenReturn(true)

        var eventFile = File("src/test/resources/events/search_04.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata6.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile,"user2")
        Assert.assertThat("Segment is not dead and we expected that check event effect method is  called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(true))

        //if isUserPResnt is true removeUserFromSegment is not called. else called
        verify(segmentService,  times(0))
                .removeUserFromSegment( anyString(),  anyLong(),  anyLong())

        verify(segmentService,  times(1))
                .isUserPresent( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(1))
                .isUserPresentInSegment(any<Segment>(), anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }

    @Test
    fun testProcessEvent1_User11_DidNot_Metadata6(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.removeUserFromSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.isUserPresent( anyString(),  anyLong(),  anyLong())).thenReturn(false)

        var eventFile = File("src/test/resources/events/search_04.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata6.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is  called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(true))

        //if isUserPResnt is true removeUserFromSegment is not called. else called
        verify(segmentService,  times(0))
                .removeUserFromSegment( anyString(),  anyLong(),  anyLong())

        verify(segmentService,  times(1))
                .addUserInSegment( anyString(),  anyLong(),  anyLong())

        verify(segmentService,  times(1))
                .isUserPresent( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(1))
                .isUserPresentInSegment(any<Segment>(), anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }

    @Test
    fun testProcessEvent2_DidNot_Metadata6(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.removeUserFromSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.isUserPresent( anyString(),  anyLong(),  anyLong())).thenReturn(true)

        var eventFile = File("src/test/resources/events/Add to Cart_04.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata6.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is  called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(true))

        //if isUserPResnt is true removeUserFromSegment is not called. else called
        verify(segmentService,  times(0))
                .removeUserFromSegment( anyString(),  anyLong(),  anyLong())

        verify(segmentService,  times(1))
                .addUserInSegment( anyString(),  anyLong(),  anyLong())

        verify(segmentService,  times(0))
                .isUserPresent( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(1))
                .isUserPresentInSegment(any<Segment>(), anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }
    @Test
    fun testProcessEvent3_User1_DidNot_Metadata6(){
        `when`(segmentService.isUserPresentInSegment(any(), anyLong(), any(), eq(null),  anyString())).thenReturn(true)
        `when`(segmentService.addUserInSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.removeUserFromSegment( anyString(),  anyLong(),  anyLong())).thenAnswer(Answers.RETURNS_DEFAULTS)
        `when`(segmentService.isUserPresent( anyString(),  anyLong(),  anyLong())).thenReturn(true)

        var eventFile = File("src/test/resources/events/support_04.json")
        val metadataFile = File("src/test/resources/DIDNOT_metadata/didnot_metadata6.json")
        val isCheckEventEffectOnSegmentCalled = processEvent(eventFile,metadataFile)
        Assert.assertThat("Segment is not dead and we expected that check event effect method is  called",isCheckEventEffectOnSegmentCalled, Matchers.`is`(true))

        //if isUserPResnt is true removeUserFromSegment is not called. else called
        verify(segmentService,  times(0))
                .removeUserFromSegment( anyString(),  anyLong(),  anyLong())

        verify(segmentService,  times(1))
                .isUserPresent( anyString(),  anyLong(),  anyLong())
        verify(segmentService,  times(1))
                .isUserPresentInSegment(any<Segment>(), anyLong(), any<IncludeUsers>(), eq(null),  anyString())
    }



    private fun processEvent(eventJson: File, metadataJSon: File,userId:String?=null):Boolean{
         `when`(metadataRepository.findByClientIdAndTypeAndStopped( anyLong(),  anyString(),  anyBoolean())).thenReturn(listOf(objectMapper.readValue<Metadata>(metadataJSon)))
        var webEvent = objectMapper.readValue<Event>(eventJson)
        userId?.let {
            webEvent.identity.userId = it
        }
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