package com.und.repository.mongo

import com.und.model.mongo.eventapi.EventUser
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
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
        val q = Query(Criteria.where("identity.clientUserId").`is`(id))
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
        return extractids(output)
    }

    private fun updateEventUser(q: Query, update: Update, clientId: Long) {
        mongoTemplate.updateFirst(q, update, "${clientId}_eventUser")
    }

    override fun findUsersNotIn(ids: Set<String>, clientId: Long): List<String> {

        val project = Aggregation.project("_id")
        val match = Aggregation.match(Criteria.where("_id").nin(ids.map { id -> ObjectId(id) }))
        val group = Aggregation.group("_id")
        val q = Aggregation.newAggregation(project, match, group)
        val output = mongoTemplate.aggregate(q, "${clientId}_eventUser", Document::class.java)
        return extractids(output)
    }

    private fun extractids(output: AggregationResults<Document>): List<String> = output.map { it["_id"].toString() }

}