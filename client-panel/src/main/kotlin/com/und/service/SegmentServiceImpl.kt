package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.und.common.utils.loggerFor
import com.und.config.EventStream
import com.und.livesegment.model.jpa.LiveSegment
import com.und.livesegment.model.webmodel.WebLiveSegment
import com.und.livesegment.repository.jpa.LiveSegmentRepository
import com.und.model.IncludeUsers
import com.und.model.jpa.Segment
import com.und.model.mongo.eventapi.EventUser
import com.und.repository.jpa.SegmentRepository
import com.und.repository.mongo.EventRepository
import com.und.repository.mongo.EventUserRepository
import com.und.repository.mongo.SegmentMetadataRepository
import com.und.repository.mongo.SegmentUsersRepository
import com.und.web.controller.exception.CustomException
import com.und.web.controller.exception.SegmentNotFoundException
import com.und.web.model.ConditionType
import com.und.web.model.EventUserMinimal
import com.und.web.model.IdName
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.ConvertOperators
import org.springframework.data.mongodb.core.query.Query
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
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
    lateinit var eventUserService: EventUserService



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
    lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var liveSegmentRepository: LiveSegmentRepository

    @Autowired
    private lateinit var eventStream:EventStream

    @Autowired
    private lateinit var segmentUsersRepository: SegmentUsersRepository


    override fun createSegment(websegment: WebSegment, clientId: Long, userId: Long): WebSegment {
        logger.debug("Saving segment: ${websegment.name}")
        val segment = buildSegment(websegment, clientId, userId)
        try {
            val persistedSegment = segmentRepository.save(segment)
            logger.debug("Segment with name: ${websegment.name} is saved successfully.")
            websegment.id = persistedSegment.id
            //compute segment
            eventStream.outSegment().send(MessageBuilder.withPayload(persistedSegment).build())
            return websegment
        } catch (ex: ConstraintViolationException) {
            throw CustomException("Template with this name already exists.")
        } catch (ex: DataIntegrityViolationException) {
            throw CustomException("Template with this name already exists.")
        }
    }

    override fun allSegment(clientId: Long): List<WebSegment> {
        val websegments = mutableListOf<WebSegment>()
        val segments = segmentRepository.findByClientID(clientId)
        val liveSegments = liveSegmentRepository.findByClientID(clientId)
        segments.forEach {
            if (liveSegments.isNotEmpty()) {
                val liveSegment = liveSegments.find { liveSegment -> liveSegment.segmentId == it.id }
                websegments.add(buildWebSegmentWithLive(it, liveSegment))
            } else {
                websegments.add(buildWebSegmentWithLive(it, null))
            }
        }

        return websegments
    }

    override fun allSegmentIdName(clientId: Long): List<IdName> {
        val segments = segmentRepository.findByClientID(clientId)
        return segments.map { segment ->
            IdName(segment.id!!, segment.name)

        }



    }

    override fun segmentById(id: Long, clientId: Long?): WebSegment {
        if (clientId == null) throw AccessDeniedException("Access Denied.")
        logger.debug("Fetching segment: $id")
        val segment = this.persistedSegmentById(id, clientId)
        val livesegment = liveSegmentRepository.findByClientIDAndSegmentId(clientId, id)
        if (livesegment.isPresent) {
            return buildWebSegmentWithLive(segment, livSegment = livesegment.get())
        }
        return buildWebSegmentWithFilters(segment)
    }

    override fun persistedSegmentById(id: Long, clientId: Long?): Segment {

        if (clientId != null) {
            val segmentOption = segmentRepository.findByIdAndClientID(id, clientId)
            if (segmentOption.isPresent) {
                return segmentOption.get()
            }
        }
        throw SegmentNotFoundException("No Segment Exists with id $id")
    }

    /**
     * we cant replace this code with our already compute segment users because here we need to compute segment
     */
    override fun segmentUserIds(segment: com.und.web.model.Segment, clientId: Long, includeUsers: IncludeUsers): List<String> {
        return getSegmentUsers(segment, clientId, "userId", includeUsers, null).second
    }

    override fun segmentUserIds(segmentId: Long, clientId: Long, includeUsers: IncludeUsers): List<String> {
        val segmentUsers = segmentUsersRepository.findById(segmentId)
        return if(segmentUsers.isPresent){
            segmentUsers.get().users.toList()
        }else emptyList()
//        val segmentOption = segmentRepository.findByIdAndClientID(segmentId, clientId)
//        return if (segmentOption.isPresent) {
//            val segment = segmentOption.get()
//            getSegmentUsers(segment, clientId, "userId", includeUsers, null).second
//        } else emptyList()
    }

    override fun segmentUsers(segmentId: Long, clientId: Long, includeUsers: IncludeUsers, campaign: String?): List<EventUser> {
       return getEventUsersByIds(segmentId)
//        if (segmentId == -2L) {
//            return getTestSEgmentUsers(clientId)
//        }
//        val segmentOption = segmentRepository.findByIdAndClientID(segmentId, clientId)
//        return if (segmentOption.isPresent) {
//            val segment = segmentOption.get()
//            getSegmentUsersList(segment, clientId, includeUsers, campaign)
//        } else emptyList()
    }

    override fun segmentUsers(segment: WebSegment, clientId: Long,userId: Long, includeUsers: IncludeUsers): List<EventUserMinimal> {
        var timestart = System.currentTimeMillis()
        val eventUsers = getSegmentUsersList(segment, clientId, includeUsers, null)
        println(System.currentTimeMillis()-timestart)
        val build = buildEventUserList(eventUsers)
        println(System.currentTimeMillis()-timestart)
        return build
    }

    override fun isUserPresentInSegment(segment: Segment, clientId: Long, userId: String, includeUsers: IncludeUsers): Boolean {
        return checkUserInSegment(segment, clientId, userId, includeUsers)
    }

    private fun getEventUsersByIds(segmentId: Long):List<EventUser>{
        val segmentUsers = segmentUsersRepository.findById(segmentId)
        return if(segmentUsers.isPresent){
            eventUserRepository.findUserByIds(segmentUsers.get().users,segmentUsers.get().clientId!!)
        }else emptyList()

    }
    private fun checkUserInSegment(segment: Segment, clientId: Long, userId: String, includeUsers: IncludeUsers): Boolean {
        val tz = userSettingsService.getTimeZoneByClientId(clientId)

        //TODO need only query dont fetch full object
        val websegment = buildWebSegmentFilters(segment)
        val queries = segmentParserCriteria.segmentQueries(websegment, tz, includeUsers)

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

    private fun getSegmentUsersList(segment: Segment, clientId: Long, includeUsers: IncludeUsers, campaign: String?): List<EventUser> {
        return getSegmentUsers(segment, clientId, includeUsers = includeUsers, campaign = campaign).first
    }

    private fun getSegmentUsersList(segment: WebSegment, clientId: Long, includeUsers: IncludeUsers, campaign: String?): List<EventUser> {
        return getSegmentUsers(segment, clientId, includeUsers = includeUsers, campaign = campaign).first
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
    private fun getSegmentUsers(segment: Segment, clientId: Long, type: String = "eventuser", includeUsers: IncludeUsers, campaign: String?): Pair<List<EventUser>, List<String>> {
        val websegment = buildWebSegmentFilters(segment)

        return getSegmentUsers(websegment, clientId, type, includeUsers, campaign)
    }

    private fun getSegmentUsers(websegment: com.und.web.model.Segment,clientId: Long, type: String = "eventuser", includeUsers: IncludeUsers, campaign: String?): Pair<List<EventUser>, List<String>> {
        val userIdentified = when (includeUsers) {
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val tz = userSettingsService.getTimeZoneByClientId(clientId)

        val eventAggregation = segmentParserCriteria.getEventSpecificAggOperation(websegment, tz, userIdentified)
        val idList = eventRepository.usersFromEvent(eventAggregation.first, clientId)
        val didNotIdList = eventRepository.usersFromEvent(eventAggregation.second, clientId)
        val userAggregation: MutableList<AggregationOperation>
        userAggregation = if (idList.isNotEmpty()) {
            val filteredResult = idList.toMutableList()
            didNotIdList.forEach {
                filteredResult.remove(it)
            }
            segmentParserCriteria.getUserSpecificAggOperation(websegment, tz, filteredResult.toList(), fromCampaign = campaign)
        } else {
            segmentParserCriteria.getUserSpecificAggOperation(websegment, tz, didNotIdList, true, fromCampaign = campaign)
        }
        //        var userAggregation = segmentParserCriteria.getUserSpecificAggOperation(websegment, tz, idList)

        return if (type == "userId") {
            //Adding aggregation to return  only id of user instead of user profile.
            if (userAggregation.isNotEmpty()) {
                if (type == "userId") {
                    val convertor = ConvertOperators.ConvertOperatorFactory("_id").convertToString()
                    userAggregation.add(Aggregation.project().and(convertor).`as`("_id"))
                    userAggregation.add(Aggregation.group().addToSet("_id").`as`("userId"))
                }
            }
            val result = eventUserRepository.usersIdFromEventUser(userAggregation, clientId)
            Pair(emptyList(), result)
        } else {
            val result = eventUserRepository.usersProfileFromEventUser(userAggregation, clientId)
            Pair(result, emptyList())
        }
    }


    private fun retrieveUsers(queries: List<Aggregation>, conditionType: ConditionType, clientId: Long): MutableSet<String> {
        val userDidList = mutableListOf<Set<String>>()
        queries.forEach { aggregation ->
            val idList = eventRepository.usersFromEvent(aggregation, clientId)
            userDidList.add(idList.toSet())
        }

        val result = when (conditionType) {
            ConditionType.AnyOf -> if (userDidList.isNotEmpty()) userDidList.reduce { f, s -> f.union(s) } else emptySet()
            ConditionType.AllOf -> if (userDidList.isNotEmpty()) userDidList.reduce { f, s -> f.intersect(s) } else emptySet()
        }
        val mutableResult = mutableSetOf<String>()
        mutableResult.addAll(result)
        return mutableResult
    }

    private fun buildSegment(websegment: WebSegment, clientId: Long, userId: Long): Segment {
        val segment = Segment()
        with(segment) {
            id = websegment.id
            name = websegment.name
            type = websegment.type
            clientID = clientId
            appuserID = userId
            //FIXME create a separate class for json conversion
            data = objectMapper.writeValueAsString(websegment)


            //check here
        }
        return segment
    }

    private fun buildWebSegmentWithoutFilters(segment: Segment): WebSegment {
       val websegment = WebSegment()
        with(websegment) {
            id = segment.id
            name = segment.name
            type = segment.type
        }
        return websegment
    }

    private fun buildWebSegmentFilters(segment: Segment): WebSegment {
        return objectMapper.readValue(segment.data, WebSegment::class.java)

    }

    private fun buildWebSegmentWithFilters(segment: Segment): WebSegment {
        val websegment = objectMapper.readValue(segment.data, WebSegment::class.java)
        with(websegment) {
            id = segment.id
            name = segment.name
            type = segment.type
        }
        return websegment
    }

    private fun buildWebSegmentWithLive(segment: Segment, livSegment: LiveSegment?): WebSegment {
        val webSegment = buildWebSegmentWithoutFilters(segment)
        livSegment?.let {
            with(webSegment) {
                liveSegment = buildLiveSegmentForWeb(livSegment)
            }

        }
        return webSegment
    }

    private fun buildLiveSegmentForWeb(liveSegment: LiveSegment): WebLiveSegment {
        val webLiveSegment = WebLiveSegment()
        with(webLiveSegment) {
            id = liveSegment.id
            clientId = liveSegment.clientID
            liveSegmentType = liveSegment.liveSegmentType
            startEvent = liveSegment.startEvent
            endEvent = liveSegment.endEvent
            startEventFilters = objectMapper.readValue(liveSegment.startEventFilter)
            endEventFilters = objectMapper.readValue(liveSegment.endEventFilter)
            interval = liveSegment.interval
            endEventDone = liveSegment.endEventDone
        }
        return webLiveSegment
    }

    private fun buildEventUserList(eventUserList: List<EventUser>): List<EventUserMinimal> {

       return  eventUserList.map {eventUser ->   eventUserService.buildEventUserMinimal(eventUser)}

    }

    override fun segmentByClientId(clientId: Long): List<Segment> {
        //FIXED find only those segment which are behavioural type.
        return segmentRepository.findByClientIDAndType(clientId, "Behaviour")

    }
}