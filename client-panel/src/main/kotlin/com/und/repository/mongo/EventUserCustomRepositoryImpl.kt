package com.und.repository.mongo

import com.und.model.mongo.eventapi.EventUser
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.AggregationResults
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
@Repository
class EventUserCustomRepositoryImpl : EventUserCustomRepository {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate


    override fun findUserByIds(ids:Set<String>, clientId: Long): List<EventUser> {

        var query=Query()
        query.addCriteria(Criteria("_id").`in`(ids.map { ObjectId(it) }))
        var result=mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
        return result
    }


    override fun findUserById(id: String, clientId: Long): Optional<EventUser> {
        val q = Query(Criteria.where("_id").`is`(ObjectId(id)))
        return queryEventUser(q, clientId)
    }

    override fun findUserByGoogleId(id: String, clientId: Long): Optional<EventUser> {
        val q = Query(Criteria.where("identity.googleId").`is`(id))
        return queryEventUser(q, clientId)

    }

    override fun findUserByFbId(id: String, clientId: Long): Optional<EventUser> {
        val q = Query(Criteria.where("identity.fbId").`is`(id))
        return queryEventUser(q, clientId)
    }

    override fun findUserBySysId(id: String, clientId: Long): Optional<EventUser> {
        val q = Query(Criteria.where("identity.uid").`is`(id))
        return queryEventUser(q, clientId)

    }

    override fun findUserByEmail(id: String, clientId: Long): Optional<EventUser> {
        val q = Query(Criteria.where("identity.email").`is`(id))
        return queryEventUser(q, clientId)

    }

    override fun findUserByMobile(id: String, clientId: Long): Optional<EventUser> {
        val q = Query(Criteria.where("identity.mobile").`is`(id))
        return queryEventUser(q, clientId)

    }

    override fun testUserProfile(id: String, clientId: Long, isTestUser: Boolean) {
        val q = Query(Criteria.where("_id").`is`(ObjectId(id)))
        val update = Update()
        update.set("testUser", isTestUser)
        updateEventUser(q, update, clientId)

    }

    private fun queryEventUser(q: Query, clientId: Long): Optional<EventUser> {
        val eventUser = mongoTemplate.findOne(q, EventUser::class.java, "${clientId}_eventUser")
        return Optional.ofNullable(eventUser)
    }

    override fun usersFromUserProfile(query: Aggregation, clientId: Long): List<String> {
        val output = mongoTemplate.aggregate(query, "${clientId}_eventUser", Document::class.java)
        if(output!=null)
            return extractids(output)
        else
            return emptyList<String>()
    }

    private fun updateEventUser(q: Query, update: Update, clientId: Long) {
        mongoTemplate.updateFirst(q, update, "${clientId}_eventUser")
    }

    override fun findUsersNotIn(ids: Set<String>, clientId: Long): List<String> {

        //val project = Aggregation.project("_id")
        val match = Aggregation.match(Criteria.where("_id").nin(ids.map { id -> ObjectId(id) }))
        //val group = Aggregation.group("_id")
        val project=Aggregation.project("_id")
        val query = Aggregation.newAggregation( match, project)
        val output = mongoTemplate.aggregate(query, "${clientId}_eventUser", Document::class.java)
        if(output!=null)
            return extractids(output)
        else
            return emptyList<String>()
    }

    override  fun testSegmentUsers( clientId: Long): List<String> {
        val query = Query.query(Criteria.where("testUser").`is`(true))
        val output = mongoTemplate.find(query,  EventUser::class.java,"${clientId}_eventUser")
        if(output!=null)
            return output.mapNotNull { it.id }
        else
            return emptyList()
    }


    private fun extractids(output: AggregationResults<Document>): List<String> = output.map { it["_id"].toString() }

    override fun usersProfileFromEventUser(query: List<AggregationOperation>, clientId: Long): List<EventUser> {
        if(query.isNotEmpty()){
            val agg=Aggregation.newAggregation(query)
            return mongoTemplate.aggregate(agg, "${clientId}_eventUser",EventUser::class.java).mappedResults
        }else {
            return emptyList()
        }
    }

    override fun usersIdFromEventUser(query: List<AggregationOperation>, clientId: Long): List<String> {
        data class Result(var userId:List<String> = emptyList())
        if(query.isNotEmpty()){
            val agg=Aggregation.newAggregation(query)
            var result=mongoTemplate.aggregate(agg, "${clientId}_eventUser",Result::class.java).mappedResults

            return if(result.isNotEmpty()) result[0].userId else emptyList()
        }
        return emptyList()
    }
}