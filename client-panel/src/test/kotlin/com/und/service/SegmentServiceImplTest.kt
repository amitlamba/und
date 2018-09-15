package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoClient
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyVararg
import com.und.model.mongo.eventapi.EventUser
import com.und.repository.jpa.SegmentRepository
import com.und.repository.mongo.*
import com.und.web.model.ConditionType
import com.und.web.model.Segment
import junit.framework.Assert.assertEquals
import org.hamcrest.core.Is
import org.hamcrest.core.IsAnything
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Query
import org.springframework.util.ResourceUtils
import java.time.ZoneId

//@RunWith(SpringRunner::class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@DataMongoTest
@RunWith(MockitoJUnitRunner::class)
class SegmentServiceImplTest {


    private lateinit var segmentParserCriteria: SegmentParserCriteria
//    @Autowired
    @Mock
    private lateinit var eventCustomRepository: EventCustomRepository
//    @Autowired
    @Mock
    private lateinit var eventUserCustomRepository: EventUserCustomRepository
    //@Mock
    private lateinit var mongoTemplate: MongoTemplate
    lateinit var mongoClient:MongoClient
    lateinit var objectMapper: ObjectMapper

    @Before
    fun setup(){
        segmentParserCriteria= SegmentParserCriteria()
        objectMapper= ObjectMapper()
        mongoClient=MongoClient("192.168.0.109",27017)
        mongoTemplate= MongoTemplate(mongoClient,"eventdbstaging")
        eventCustomRepository= EventCustomRepositoryImpl()
        eventUserCustomRepository= EventUserCustomRepositoryImpl()
        Mockito.doReturn(emptyList<Any>()).`when`(eventCustomRepository)
        Mockito.doReturn(emptyList<Any>()).`when`(eventUserCustomRepository)
       // when(eventCustomRepository.usersFromEvent(IsAnything<List>(),IsAnything<Long>())
    }

    @After
    fun cleanup(){
        mongoClient.close()
    }

    @Test
    fun testDidSegment1(){
        var testData=readFile("didsegment1")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=0
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Test
    fun testDidSegment2(){
        var testData=readFile("didsegment2")
        println(testData)
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=36
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Ignore
    @Test
    fun testDidSegment3(){
        var testData=readFile("didsegment3")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=3054
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Test
    fun testDidSegment4(){
        var testData=readFile("didsegment4")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=3431
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Test
    fun testDidNotSegment1(){
        var testData=readFile("didnotsegment1")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=5000
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Ignore("ui 5000 4990 without fgroup on userid")
    @Test
    fun testDidNotSegment2(){
        var testData=readFile("didnotsegment2")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=4990
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }
    @Ignore("4969  and 5000 without fgroup on userid ui 4997")
    @Test
    fun testDidNotSegment3(){
        var testData=readFile("didnotsegment3")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=4997
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Test
    fun testGeography(){
        var testData=readFile("geography")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=1
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Ignore("need to check how dob handle")
    @Test
    fun testProperty(){
        var testData=readFile("property")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=0
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Test
    fun testAll(){
        var testData=readFile("all")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=1
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Ignore("ui 804 query 941")
    @Test
    fun testDidAndDidNot(){
        var testData=readFile("did-didnot")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=804
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Test
    fun testDidGeo(){
        var testData=readFile("did-geo")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=1
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Test
    fun testDidNotAndGeo(){
        var testData=readFile("didnot-geo")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=2
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Test
    fun testDidNotAndProperty(){
        var testData=readFile("didnot-property")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=2500
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    @Test
    fun testDidAndProperty(){
        var testData=readFile("did-property")
        var segment=objectMapper.readValue(testData,Segment::class.java)
        var expectedResult=1286
        var result=getSegmentUserList(segment)
        assertEquals("expectedResult is $expectedResult actual is $result",expectedResult,result)
    }

    private fun getSegmentUserList(websegment:Segment): Int {
        val clientId=3L
        val tz = ZoneId.of("Asia/Kolkata")
        val allResult = mutableListOf<Set<String>>()
        //val websegment = buildWebSegment(segment)
        val queries = segmentParserCriteria.segmentQueries(websegment, tz)
        val (didQueries, joincondition) = queries.didq
        if (didQueries.isNotEmpty()) {
            val userDidList = retrieveUsers(didQueries, joincondition, clientId)
            allResult.add(userDidList.toSet())
        }else if (queries.query!=null){
            var query= Query().addCriteria(queries.query)
            val userList=eventCustomRepository.usersFromEvent(query,clientId)
            allResult.add(userList.toSet())
        }

        val (didNotQueries, joinconditionfornot) = queries.didntq
        if (didNotQueries.isNotEmpty()) {
            val userDidNotDid = retrieveUsers(didNotQueries, joinconditionfornot, clientId)

            val userDidNotList = eventUserCustomRepository.findUsersNotIn(userDidNotDid.toSet(), clientId)
            allResult.add(userDidNotList.toSet())
        }

        val userQuery = queries.userQuery
        if (userQuery != null) {
            val userProfiles = eventUserCustomRepository.usersFromUserProfile(userQuery, clientId)
            allResult.add(userProfiles.toSet())
        }
        val userList = allResult.reduce { f, s -> f.intersect(s) }
        var result= eventUserCustomRepository.findUserByIds(userList,clientId)
//        assertEquals(segment1resultExpected,result)
        return result.size
    }

    private fun retrieveUsers(queries: List<Aggregation>, conditionType: ConditionType, clientId: Long): MutableSet<String> {
        val userDidList = mutableListOf<Set<String>>()
        queries.forEach { aggregation ->
            val idList = eventCustomRepository.usersFromEvent(aggregation, clientId)
            userDidList.add(idList.toSet())

        }
        val result = when (conditionType) {
            ConditionType.AnyOf -> userDidList.reduce { f, s -> f.union(s) }
            ConditionType.AllOf -> userDidList.reduce { f, s -> f.intersect(s) }
        }
        val mutableResult = mutableSetOf<String>()
        mutableResult.addAll(result)
        return mutableResult
    }

    private fun readFile(fileName:String):String{
        return ResourceUtils.getFile("classpath:segementData/$fileName").readText(Charsets.UTF_8)
    }
}