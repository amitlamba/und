package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.jpa.Segment
import com.und.model.mongo.EventUser
import com.und.model.utils.CampaignType
import com.und.model.utils.IncludeUsers
import com.und.repository.jpa.SegmentRepository
import com.und.repository.jpa.security.UserRepository
import com.und.repository.mongo.EventUserRepository
import com.und.repository.mongo.SegmentUsersRepository
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import com.und.model.utils.Segment as WebSegment

@Service
class SegmentService {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var segmentRepository: SegmentRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var segmentUserServiceClient: SegmentUserServiceClient

    @Autowired
    private lateinit var segmentUsersRepository: SegmentUsersRepository

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    fun getSegment(segmentId: Long, clientId: Long): Segment {
        return segmentRepository.getSegmentByIdAndClientID(segmentId, clientId)
    }

    private fun buildWebSegment(segment: Segment): WebSegment {
        val websegment = objectMapper.readValue(segment.data, WebSegment::class.java)
        websegment.id = segment.id
        websegment.name = segment.name
        websegment.type = segment.type
        return websegment
    }

    fun getWebSegment(segmentId: Long, clientId: Long): WebSegment {
        val segment = getSegment(segmentId, clientId)
        return buildWebSegment(segment)
    }

    fun getUserData(webSegment: WebSegment, clientId: Long,campaignType:String): List<EventUser> {
        //TODO: Write the definition to get data from Mongo here
        val token = userRepository.findSystemUser().key
        val segmentId = webSegment.id
        return if (segmentId != null && token != null) {
            segmentUserServiceClient.users(segmentId, clientId, token,IncludeUsers.ALL,campaignType)
        } else emptyList()
    }

    fun getUserData(segmentId:Long,clientId: Long,type:String):List<EventUser>{
        val segmentUsers = segmentUsersRepository.findById(segmentId)
        return if(segmentUsers.isPresent){
            val users= segmentUsers.get().users
            eventUserRepository.findAllByIdAndByCampaignType(clientId,users.map { ObjectId(it) },CampaignType.valueOf(type))
        }else emptyList<EventUser>()
//        val token = userRepository.findSystemUser().key
//        return if (token != null) {
//            segmentUserServiceClient.users(segmentId, clientId, token,IncludeUsers.ALL,type)
//        } else emptyList()
    }


}