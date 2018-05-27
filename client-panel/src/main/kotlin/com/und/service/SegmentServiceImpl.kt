package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.jpa.Segment
import com.und.model.mongo.eventapi.EventUser
import com.und.repository.jpa.SegmentRepository
import com.und.repository.mongo.EventRepository
import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.model.ConditionType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.stereotype.Service
import com.und.web.model.Segment as WebSegment

@Service
class SegmentServiceImpl : SegmentService {


    @Autowired
    lateinit var segmentRepository: SegmentRepository

    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var eventUserRepository: EventUserRepository


    @Autowired
    private lateinit var objectMapper: ObjectMapper


    override fun createSegment(websegment: WebSegment): WebSegment {
        val segment = buildSegment(websegment)
        segmentRepository.save(segment)
        websegment.id = segment.id
        return websegment

    }

    override fun allSegment(): List<WebSegment> {
        val clientID = AuthenticationUtils.clientID
        val websegments = mutableListOf<WebSegment>()
        if (clientID != null) {
            val segments = segmentRepository.findByClientID(clientID)
            segments?.forEach { websegments.add(buildWebSegment(it)) }
        }

        return websegments
    }

    override fun segmentUsers(segmentId: Long, clientId: Long): List<EventUser> {
        val segment = segmentRepository.findByIdAndClientID(segmentId, clientId)
        return if (segment != null) {
            buildWebSegment(segment)
            val queries = SegmentParserCriteria().segmentQueries(buildWebSegment(segment))
            val userDidList = retrieveUsers(queries.didq.first, queries.didq.second,clientId)
            val userDidNotList = retrieveUsers(queries.didntq.first, queries.didntq.second,clientId)


            val userList = userDidList.intersect(userDidNotList)
            val users = userList.map {
                eventUserRepository.findUserById(it, clientId)

            }.filterNotNull()


            return users
        } else emptyList()
    }

    private fun retrieveUsers(queries: List<Aggregation>, conditionType: ConditionType, clientId: Long): MutableSet<String> {
        val userDidList = mutableSetOf<String>()
        queries.forEach {
            val idList = eventRepository.usersFromEvent(it, clientId)
            when (conditionType) {
                ConditionType.AnyOf -> userDidList.addAll(idList)
                ConditionType.AllOf -> userDidList.intersect(idList)
            }

        }
        return userDidList
    }

    private fun buildSegment(websegment: WebSegment): Segment {
        val segment = Segment()
        with(segment) {
            id = websegment.id
            name = websegment.name
            type = websegment.type
            clientID = AuthenticationUtils.clientID
            appuserID = AuthenticationUtils.principal.id
            //FIXME create a separate class for json conversion
            data = objectMapper.writeValueAsString(websegment)
        }
        return segment
    }

    private fun buildWebSegment(segment: Segment): WebSegment {
        val websegment = objectMapper.readValue(segment.data, WebSegment::class.java)
        with(websegment) {
            id = segment.id
            name = segment.name
            type = segment.type
        }
        return websegment
    }


}