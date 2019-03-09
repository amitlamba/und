package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.common.utils.loggerFor
import com.und.livesegment.model.jpa.LiveSegment
import com.und.livesegment.repository.jpa.LiveSegmentRepository
import com.und.model.jpa.Segment
import com.und.model.mongo.eventapi.EventUser
import com.und.repository.jpa.SegmentRepository
import com.und.repository.mongo.EventRepository
import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.CustomException
import com.und.web.controller.exception.SegmentNotFoundException
import com.und.web.model.ConditionType
import org.bson.types.ObjectId
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.ConvertOperators
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType
import com.und.web.model.EventUser as EventUserWeb
import com.und.web.model.Segment as WebSegment

@Service
class SegmentServiceImpl : SegmentService {

    companion object {
        val logger: Logger = loggerFor(SegmentServiceImpl::class.java)
    }

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

    @Autowired
    lateinit var mongoTemplate:MongoTemplate

    @Autowired
    private lateinit var liveSegmentRepository: LiveSegmentRepository


    //@CacheEvict("segmentlist", key = "'client_'+T(com.und.security.utils.AuthenticationUtils).INSTANCE.getClientID()+'_segment_'" )
    override fun createSegment(websegment: WebSegment): WebSegment {
        logger.debug("Saving segment: ${websegment.name}")
        val segment = buildSegment(websegment)
        try{
            segmentRepository.save(segment)
            logger.debug("Segment with name: ${websegment.name} is saved successfully.")
            websegment.id = segment.id
            return websegment
        }catch (ex: ConstraintViolationException){
            throw CustomException("Template with this name already exists.")
        }catch (ex: DataIntegrityViolationException){
            throw CustomException("Template with this name already exists.")
        }
    }

    //@Cacheable(cacheNames = ["segmentlist"], key = "'client_'+T(com.und.security.utils.AuthenticationUtils).INSTANCE.getClientID()+'_segment_'" )
    override fun allSegment(): List<WebSegment> {
        val clientID = AuthenticationUtils.clientID
        val websegments = mutableListOf<WebSegment>()
        if (clientID != null) {
            val segments = segmentRepository.findByClientID(clientID)
            val liveSegments=liveSegmentRepository.findByClientID(clientID)
            segments?.forEach {
                val liveSegment=liveSegments.get().find { liveSegment -> liveSegment.segmentId==it.id }
                websegments.add(buildWebSegmentWithLive(it,liveSegment))
            }
        }

        return websegments
    }

    //@Cacheable(cacheNames = ["segment"], key = "'client_'+T(com.und.security.utils.AuthenticationUtils).INSTANCE.getClientID()+'_segment_'+#id" )
    override fun segmentById(id: Long): WebSegment {
        logger.debug("Fetching segment: $id")
        return buildWebSegment(this.persistedSegmentById(id))
    }

    override fun persistedSegmentById(id: Long): Segment {
        val clientID = AuthenticationUtils.clientID
        if (clientID != null) {
            val segmentOption = segmentRepository.findByIdAndClientID(id, clientID)
            if (segmentOption.isPresent) {
                return segmentOption.get()
            }
        }
        throw SegmentNotFoundException("No Segment Exists with id $id")
    }


    override fun segmentUserIds(segmentId: Long, clientId: Long): List<String> {
        val segmentOption = segmentRepository.findByIdAndClientID(segmentId, clientId)
        return if (segmentOption.isPresent) {
            val segment = segmentOption.get()
            getSegmentUsers(segment,clientId,"userId").second
        } else emptyList()
    }

    override fun segmentUsers(segmentId: Long, clientId: Long): List<EventUser> {
        if(segmentId == -2L) {
            return getTestSEgmentUsers(clientId)
        }
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

    override fun isUserPresentInSegment(segment: Segment, clientId: Long, userId: String): Boolean {
        return checkUserInSegment(segment, clientId, userId)
    }

    private fun checkUserInSegment(segment: Segment, clientId: Long, userId: String): Boolean {
        val tz = userSettingsService.getTimeZone()
        val websegment = buildWebSegment(segment)
        val queries = segmentParserCriteria.segmentQueries(websegment, tz)

        val (didQueries, joincondition) = queries.didq
        if (didQueries.isNotEmpty()) {
            val userDidList = retrieveUsers(didQueries, joincondition, clientId)
            if (!userDidList.toSet().contains(userId)) {
                return false
            }
        } else if (queries.query != null) {
            var query = Query().addCriteria(queries.query)
            val userList = eventRepository.usersFromEvent(query, clientId)
            if (!userList.toSet().contains(userId)) {
                return false
            }
        }

        val (didNotQueries, joinconditionfornot) = queries.didntq
        if (didNotQueries.isNotEmpty()) {
            val userWhoDidIt = retrieveUsers(didNotQueries, joinconditionfornot, clientId)
            if (userWhoDidIt.toSet().contains(userId)) {
                return false
            }
        }

        val userQuery = queries.userQuery
        if (userQuery != null) {
            val userProfiles = eventUserRepository.usersFromUserProfile(userQuery, clientId)
            if (!userProfiles.toSet().contains(userId)) {
                return false
            }
        }

        return true
    }

    private fun getSegmentUsersList(segment: Segment, clientId: Long): List<EventUser> {
        return getSegmentUsers(segment,clientId).first
    }

    private fun getTestSEgmentUsers(clientId: Long): List<EventUser> {
        val userIds = eventUserRepository.testSegmentUsers(clientId)
        return eventUserRepository.findUserByIds(userIds.toSet(), clientId)
    }

/*
* TODO performance improvement  we can add project aggregation stage to remove that part of document which is not used in next stage.
* eg. we add geo filter in first stage it mean after this stage we are not performing geo specific match so we can drop that field here.
* Its decrease the size of document for next stage.
* */
    private fun getSegmentUsers(segment: Segment, clientId: Long, type: String = "eventuser"): Pair<List<EventUser>, List<String>> {
        val tz = userSettingsService.getTimeZoneByClientId(clientId)
        val websegment = buildWebSegment(segment)
        var eventAggregation = segmentParserCriteria.getEventSpecificAggOperation(websegment, tz)
        val idList = eventRepository.usersFromEvent(eventAggregation.first, clientId)
        val didNotIdList = eventRepository.usersFromEvent(eventAggregation.second, clientId)
    var userAggregation:MutableList<AggregationOperation>
    if (idList.isNotEmpty()) {
        var filteredResult = idList.toMutableList()
        didNotIdList.forEach{
            filteredResult.remove(it)
        }
        userAggregation = segmentParserCriteria.getUserSpecificAggOperation(websegment, tz, filteredResult.toList())
    } else {
        userAggregation = segmentParserCriteria.getUserSpecificAggOperation(websegment, tz, didNotIdList, true)
    }
//        var userAggregation = segmentParserCriteria.getUserSpecificAggOperation(websegment, tz, idList)
        if (type.equals("userId")) {
            //Adding aggregation to return  only id of user instead of user profile.
            if (type.equals("userId")) {
                var convertor = ConvertOperators.ConvertOperatorFactory("_id").convertToString()
                userAggregation.add(Aggregation.project().and(convertor).`as`("_id"))
                userAggregation.add(Aggregation.group().push("_id").`as`("userId"))
            }
            val result = eventUserRepository.usersIdFromEventUser(userAggregation, clientId)
            return Pair(emptyList(), result)
        } else {
            val result = eventUserRepository.usersProfileFromEventUser(userAggregation, clientId)
            return Pair(result, emptyList())
        }
    }


    private fun retrieveUsers(queries: List<Aggregation>, conditionType: ConditionType, clientId: Long): MutableSet<String> {
        val userDidList = mutableListOf<Set<String>>()
        queries.forEach { aggregation ->
            val idList = eventRepository.usersFromEvent(aggregation, clientId)
            userDidList.add(idList.toSet())
        }

        val result = when (conditionType) {
            ConditionType.AnyOf -> if(userDidList.isNotEmpty()) userDidList.reduce { f, s -> f.union(s) } else emptySet()
            ConditionType.AllOf -> if(userDidList.isNotEmpty()) userDidList.reduce { f, s -> f.intersect(s) } else emptySet()
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

    private fun buildWebSegmentWithLive(segment: Segment,livSegment:LiveSegment?):WebSegment{
        if(livSegment!=null){
            val webSegment=buildWebSegment(segment)
            with(webSegment){
                liveSegment=livSegment
            }
            return webSegment
        }else{
            return buildWebSegment(segment)
        }
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

    override fun segmentByClientId(clientId: Long): List<Segment> {
        return segmentRepository.findByClientID(clientId)?: emptyList()
    }
}