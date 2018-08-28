package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.jpa.Segment
import com.und.model.mongo.eventapi.EventUser
import com.und.repository.jpa.SegmentRepository
import com.und.repository.mongo.EventRepository
import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.SegmentNotFoundException
import com.und.web.model.ConditionType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import com.und.web.model.EventUser as EventUserWeb
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
    private lateinit var userSettingsService: UserSettingsService

    @Autowired
    private lateinit var objectMapper: ObjectMapper


    @Autowired
    private lateinit var segmentParserCriteria: SegmentParserCriteria


    //@CacheEvict("segmentlist", key = "'client_'+T(com.und.security.utils.AuthenticationUtils).INSTANCE.getClientID()+'_segment_'" )
    override fun createSegment(websegment: WebSegment): WebSegment {
        val segment = buildSegment(websegment)
        segmentRepository.save(segment)
        websegment.id = segment.id
        return websegment

    }

    //@Cacheable(cacheNames = ["segmentlist"], key = "'client_'+T(com.und.security.utils.AuthenticationUtils).INSTANCE.getClientID()+'_segment_'" )
    override fun allSegment(): List<WebSegment> {
        val clientID = AuthenticationUtils.clientID
        val websegments = mutableListOf<WebSegment>()
        if (clientID != null) {
            val segments = segmentRepository.findByClientID(clientID)
            segments?.forEach { websegments.add(buildWebSegment(it)) }
        }

        return websegments
    }

    //@Cacheable(cacheNames = ["segment"], key = "'client_'+T(com.und.security.utils.AuthenticationUtils).INSTANCE.getClientID()+'_segment_'+#id" )
    override fun segmentById(id: Long): WebSegment {
        val clientID = AuthenticationUtils.clientID
        if (clientID != null) {
            val segmentOption = segmentRepository.findByIdAndClientID(id, clientID)
            if (segmentOption.isPresent) {
                return buildWebSegment(segmentOption.get())
            }
        }
        throw SegmentNotFoundException("No Segment Exists with id $id")
    }

    override fun segmentUsers(segmentId: Long, clientId: Long): List<EventUser> {
        val segmentOption = segmentRepository.findByIdAndClientID(segmentId, clientId)
        return if (segmentOption.isPresent) {
            val segment = segmentOption.get()
            getSegmentUsersList(segment, clientId)

        } else emptyList()
    }

    override fun segmentUsers(segment: WebSegment, clientId: Long): List<EventUserWeb> {
        val segmentJpa = buildSegment(segment)
        val eventUsers = getSegmentUsersList(segmentJpa, clientId)
        return buildEventUserList(eventUsers)
    }

    private fun getSegmentUsersList(segment: Segment, clientId: Long): List<EventUser> {

        val tz = userSettingsService.getTimeZone()
        val allResult = mutableListOf<Set<String>>()
        val websegment = buildWebSegment(segment)
        val queries = segmentParserCriteria.segmentQueries(websegment, tz)

        val (didQueries, joincondition) = queries.didq
        if (didQueries.isNotEmpty()) {
            val userDidList = retrieveUsers(didQueries, joincondition, clientId)
            allResult.add(userDidList.toSet())
        }else if (queries.query!=null){
            var query= Query().addCriteria(queries.query)
            val userList=eventRepository.usersFromEvent(query,clientId)
            allResult.add(userList.toSet())
        }

        val (didNotQueries, joinconditionfornot) = queries.didntq
        if (didNotQueries.isNotEmpty()) {
            val userDidNotDid = retrieveUsers(didNotQueries, joinconditionfornot, clientId)

            val userDidNotList = eventUserRepository.findUsersNotIn(userDidNotDid.toSet(), clientId)
            allResult.add(userDidNotList.toSet())
        }

        val userQuery = queries.userQuery
        if (userQuery != null) {
            val userProfiles = eventUserRepository.usersFromUserProfile(userQuery, clientId)
            allResult.add(userProfiles.toSet())
        }

        val userList = allResult.reduce { f, s -> f.intersect(s) }
//        return userList.asSequence().map {
//            eventUserRepository.findUserById(it, clientId)
//
//        }.filter { it.isPresent }.map { it.get() }.toList()
        return eventUserRepository.findUserByIds(userList,clientId)

    }

    private fun retrieveUsers(queries: List<Aggregation>, conditionType: ConditionType, clientId: Long): MutableSet<String> {
        val userDidList = mutableListOf<Set<String>>()
        queries.forEach { aggregation ->
            val idList = eventRepository.usersFromEvent(aggregation, clientId)
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


            //check here
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

    private fun buildEventUserList(eventUserList: List<EventUser>): List<EventUserWeb> {
        var eventUserListWeb: List<EventUserWeb> = emptyList()
        val eventUserService = EventUserService()
        for (eventUser in eventUserList) {
            val listElement = eventUserService.buildEventUser(eventUser)
            eventUserListWeb += listElement
        }
        return eventUserListWeb
    }

}