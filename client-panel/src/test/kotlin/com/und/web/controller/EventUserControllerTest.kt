package com.und.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.IncludeUsers
import com.und.repository.jpa.CampaignRepository
import com.und.service.SegmentService
import com.und.service.SegmentServiceImpl
import com.und.web.model.Segment
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc

//@RunWith(SpringRunner::class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//////@WebMvcTest(EventUserController::class)
//@DataMongoTest
//@DataJpaTest
class EventUserControllerTest {

    @Autowired
    private lateinit var segmentService: SegmentService
//
//    @Autowired
//    lateinit var mockMvc:MockMvc
    @Autowired
    private lateinit var objetMapper: ObjectMapper

    val segment1="{\"didEvents\":{\"events\":[{\"dateFilter\":{\"operator\":\"Before\",\"values\":[\"2018-08-27\"]},\"whereFilter\":{\"operator\":\"GreaterThan\",\"values\":[0],\"whereFilterName\":\"Count\"},\"name\":\"Search\",\"propertyFilters\":[]}],\"joinCondition\":{\"conditionType\":\"AllOf\"}},\"didNotEvents\":{\"events\":[{\"dateFilter\":{\"operator\":\"Before\",\"values\":[\"2018-08-27\"]},\"whereFilter\":{},\"name\":\"Add to WishList\",\"propertyFilters\":[]}],\"joinCondition\":{\"conditionType\":\"AnyOf\"}},\"globalFilters\":[],\"geographyFilters\":[],\"type\":\"Behaviour\"}"
    val segment1ExpectedUser=2500
    var clientId:Long=3
    @Before
    fun setUp(){
    segmentService=SegmentServiceImpl()
     objetMapper=ObjectMapper()
    }
    @Test
    fun findEventUsersBySegment() {


        //make a web model segment
        var segment=objetMapper.readValue(segment1,Segment::class.java)

        var eventUserList=segmentService.segmentUsers(segment,clientId = clientId,includeUsers = IncludeUsers.KNOWN)
        println(eventUserList.size)
        assertEquals(segment1ExpectedUser,eventUserList.size)
    }


}