package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.jpa.Segment
import com.und.model.mongo.EventUser
import com.und.repository.jpa.SegmentRepository
import com.und.repository.jpa.security.UserRepository
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

    fun getUserData(webSegment: WebSegment): List<EventUser> {
        //TODO: Write the definition to get data from Mongo here
        val token = userRepository.findSystemUser().key
        val segmentId = webSegment.id
        return if (segmentId != null && token != null) {
            segmentUserServiceClient.users(segmentId, token)
        } else emptyList()
    }


}