package com.und.service.segmentquerybuilder


import com.und.model.ConditionType
import com.und.model.GlobalFilter
import com.und.model.IncludeUsers
import com.und.model.Segment
import com.und.model.mongo.Event
import com.und.model.mongo.EventUser
import com.und.repository.EventUserRepository
import com.und.repository.mongo.EventRepository
import com.und.repository.mongo.SegmentUsersRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.und.service.segmentquerybuilder.UserSettingsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.ConvertOperators
import org.springframework.stereotype.Service

@Service
class SegmentServiceImpl : SegmentService {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(SegmentServiceImpl::class.java)
    }

    @Autowired
    private lateinit var segmentUsersRepository: SegmentUsersRepository

    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var userSettingsService: UserSettingsService
//
//    @Autowired
//    private lateinit var objectMapper: ObjectMapper
//
//
    @Autowired
    private lateinit var segmentParserCriteria: SegmentParserCriteria
//
//    @Autowired
//    lateinit var mongoTemplate: MongoTemplate
//
//    @Autowired
//    private lateinit var liveSegmentRepository: LiveSegmentRepository
//
//


//     fun getSegmentUsersList(segment: Segment, clientId: Long, includeUsers: IncludeUsers, campaign: String?): List<EventUser> {
//        return getSegmentUsers(segment, clientId, includeUsers = includeUsers, campaign = campaign).first
//    }
    override fun isUserPresentInSegment(segment: Segment, clientId: Long, includeUsers: IncludeUsers, campaign: String?,userId: String): Boolean{
        return getSegmentUsers(segment, clientId, includeUsers = includeUsers, campaign = campaign,userId = userId).second.isNotEmpty()
    }

    override fun addUserInSegment(userId: String, clientId: Long,segmentId:Long) {
        segmentUsersRepository.addUserInSegment(clientId, userId, segmentId)
    }

    override fun removeUserFromSegment(userId: String, clientId: Long,segmentId:Long) {
        segmentUsersRepository.removeUserFromSegment(clientId,userId,segmentId)
    }

    override fun isUserPresentInSegmentWithoutUserProp(segment: Segment, clientId: Long, includeUsers: IncludeUsers, campaign: String?, userId: String): Boolean {
        val userIdentified = when (includeUsers) {
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val tz = userSettingsService.getTimeZoneByClientId(clientId)
        segment.userId = userId
        val eventAggregation = segmentParserCriteria.getEventSpecificAggOperation(segment, tz, userIdentified)
        val idList = eventRepository.usersFromEvent(eventAggregation.first, clientId)
        val didNotIdList = eventRepository.usersFromEvent(eventAggregation.second, clientId)
        return !(idList.isNotEmpty() && didNotIdList.isNotEmpty())
    }

    override fun isUserPresentInSegmentWithUserPropOnly(segment: Segment, clientId: Long, includeUsers: IncludeUsers, campaign: String?, userId: String): Boolean {
        val tz = userSettingsService.getTimeZoneByClientId(clientId)
        segment.userId =userId
        val  userAggregation = segmentParserCriteria.getUserSpecificAggOperation(segment, tz, emptyList(), fromCampaign = null)
        val result = eventUserRepository.usersIdFromEventUser(userAggregation, clientId)
        return result.isNotEmpty()
    }


    override fun isUserPresent(userId: String, clientId: Long, segmentId: Long): Boolean {
        return segmentUsersRepository.isUserPresent(userId, clientId, segmentId)
    }

    override fun isEventExists(id: String): Boolean {
        return eventRepository.existsById(id)
    }

    override fun saveEvent(event: Event,clientId: Long) {
        eventRepository.save(event,clientId)
    }

    /*
    * TODO performance improvement  we can add project aggregation stage to remove that part of document which is not used in next stage.
    * eg. we add geo filter in first stage it mean after this stage we are not performing geo specific match so we can drop that field here.
    * Its decrease the size of document for next stage.
    * */
//    private fun getSegmentUsers(segment: Segment, clientId: Long, type: String = "eventuser", includeUsers: IncludeUsers, campaign: String?): Pair<List<EventUser>, List<String>> {
//        val websegment = buildWebSegmentFilters(segment)
//
//        return getSegmentUsers(websegment, clientId, type, includeUsers, campaign)
//    }

    private fun getSegmentUsers(websegment: Segment, clientId: Long, type: String = "eventuser", includeUsers: IncludeUsers, campaign: String?,userId: String): Pair<List<EventUser>, List<String>> {
        val userIdentified = when (includeUsers) {
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val tz = userSettingsService.getTimeZoneByClientId(clientId)
        websegment.userId=userId
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

    override fun isUserPropertyMatch(userId: String, clientId: Long, filter: List<GlobalFilter>, userIdentified: Boolean, timezne: String): Boolean {
        val criteria = segmentParserCriteria.isUserPropertiesMatch(filter,userId,timezne,userIdentified)
        return criteria?.let {
            segmentUsersRepository.findUserByUserProperties(it,clientId).isNotEmpty()
        }?:true
    }
}