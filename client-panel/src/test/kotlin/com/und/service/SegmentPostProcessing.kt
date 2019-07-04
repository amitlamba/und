package com.und.service

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.nhaarman.mockito_kotlin.times
import com.und.model.IncludeUsers
import com.und.model.jpa.ClientSettings
import com.und.model.mongo.Metadata
import com.und.model.mongo.SegmentUsers
import com.und.repository.jpa.ClientSettingsRepository
import com.und.repository.mongo.SegmentMetadataRepository
import com.und.repository.mongo.SegmentUsersRepository
import com.und.web.model.Segment
import com.nhaarman.mockito_kotlin.any
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.lang.reflect.Method
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@RunWith(MockitoJUnitRunner::class)
class SegmentPostProcessing {

//    @get:Rule
//    var mockitoRunner = MockitoJUnit.rule()

    //@Mock
    lateinit var objectMapper:ObjectMapper

    @Mock
    lateinit var clientSettingsRepository: ClientSettingsRepository

    @Mock
    lateinit var segmentService: SegmentServiceImpl

    @Mock
    lateinit var segmentUsersRepository: SegmentUsersRepository

    @Mock
    lateinit var metadataRepository: SegmentMetadataRepository

    @InjectMocks
    lateinit var eventProcessingService:CreateMetadataService


    companion object {
        @JvmStatic
        @BeforeClass
        fun initFirst(){
            println("init first called")
        }
    }
    @Before
    fun init(){
        objectMapper = ObjectMapper()
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        val module = SimpleModule()
        module.addSerializer(LocalDateTime::class.java,LocalDateTimeSerializer())
        module.addDeserializer(LocalDateTime::class.java,LocalDateTimeDeserializer())
        module.addSerializer(ZoneId::class.java,CustomZoneIdSerializer())
        module.addDeserializer(ZoneId::class.java,CustomZoneIdDeserializer())
        objectMapper.registerModule(module)
    }
    @Test
    fun universalMetaDataCreator(){
        for(i in 1..5 step 1) {
            val file = File("src/test/resources/DID_DIDNOT/did_didnot_segment${i}.json")
            val jsonSegment = file.readText(Charsets.UTF_8)
            val segment = objectMapper.readValue<Segment>(jsonSegment)
            val metadata = eventProcessingService.createSegmentMetadata(segment, 111, 11, "past")
            val file1 = File("src/test/resources/DID_DIDNOT/did_didnot_metadata${i}.json")
            objectMapper.writeValue(file1,metadata)
        }
    }

    @Test
    fun segmentPostProcessingTest(){
        val file = File("src/test/resources/DID/did_segment3")
        val jsonSegment = file.readText(Charsets.UTF_8)
        val segment = objectMapper.readValue<Segment>(jsonSegment)

        //using reflection to access private method or field

        var segmentServiceClass = SegmentServiceImpl::class.java
        var segmentService1 = segmentServiceClass.newInstance()
        //val segmentServiceClass = segmentService.javaClass   //Its not possible to use access private member of mock object using reflection
        var field = segmentServiceClass.getDeclaredField("objectMapper")
        field.isAccessible = true
        field.set(segmentService1,objectMapper)
        var buildJpaSegmentMethod: Method = segmentServiceClass.getDeclaredMethod("buildSegment", Segment::class.java,Long::class.java,Long::class.java)
        buildJpaSegmentMethod.isAccessible=true

        //build jpa segment
        val jpaSegment = buildJpaSegmentMethod.invoke(segmentService1,segment,1,1) as com.und.model.jpa.Segment
        jpaSegment.id = 1


        val eventProcessingClass = eventProcessingService.javaClass
        val field1 = eventProcessingClass.getDeclaredField("objectMapper")
        field1.isAccessible = true
        field1.set(eventProcessingService,objectMapper)



        //mock dao logic
        `when`(clientSettingsRepository.findByClientID(anyLong())).thenReturn(ClientSettings())
        `when`(segmentService.segmentUserIds(any<Segment>(), anyLong(), any<IncludeUsers>())).thenReturn(emptyList())
        `when`(segmentUsersRepository.save(any<SegmentUsers>())).then(Answers.RETURNS_DEFAULTS)
        `when`(metadataRepository.save(any<Metadata>())).then(Answers.RETURNS_DEFAULTS)

        eventProcessingService.segmentPostProcessing(jpaSegment)

        verify(clientSettingsRepository, times(1)).findByClientID(anyLong())
        verify(segmentService).segmentUserIds(any<Segment>(), anyLong(), any<IncludeUsers>())
        verify(segmentUsersRepository).save(any<SegmentUsers>())
    }
}


class LocalDateTimeSerializer : JsonSerializer<LocalDateTime>(){

    override fun serialize(value: LocalDateTime?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }

}

class LocalDateTimeDeserializer: JsonDeserializer<LocalDateTime>(){

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): LocalDateTime {
        return p?.let {
            LocalDateTime.parse(it.text)
        }?: LocalDateTime.now()
    }

}

class CustomZoneIdSerializer: JsonSerializer<ZoneId>(){
    override fun serialize(value: ZoneId?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value?.id)
    }
}

class CustomZoneIdDeserializer: JsonDeserializer<ZoneId>(){
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ZoneId {
        return p?.let { ZoneId.of(it.text) }?: ZoneId.systemDefault()
    }
}