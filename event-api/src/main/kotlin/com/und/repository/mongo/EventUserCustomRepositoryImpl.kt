package com.und.repository.mongo

import com.und.model.mongo.eventapi.EventUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.util.*

class EventUserCustomRepositoryImpl:EventUserCustomRepository {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    override fun findByIdOrIdentityUid(id: String, uid: String, clientId: Long): Optional<EventUser> {
        val eventUser =
                mongoTemplate.findOne(Query().addCriteria(
                        Criteria().orOperator(
                                Criteria("_id").`is`(id),
                                Criteria("identity.uid").`is`(uid)
                        )), EventUser::class.java, "${clientId}_eventUser")
        return if (eventUser==null) Optional.empty() else Optional.of(eventUser)
    }

    override fun findByIdAndIdentityUid(id: String, uid: String, clientId: Long): Optional<EventUser> {
        val eventUser = mongoTemplate.findOne(Query.query(Criteria.where("_id").`is`(id).and("identity.uid").`is`(uid)),
                EventUser::class.java,"${clientId}_eventUser")
        return if (eventUser==null) Optional.empty() else Optional.of(eventUser)
    }

    override fun findByIdentityUid(uid: String, clientId: Long): Optional<EventUser> {
        val eventUser = mongoTemplate.findOne(Query.query(Criteria.where("identity.uid").`is`(uid)),
                EventUser::class.java,"${clientId}_eventUser")
        return if (eventUser==null) Optional.empty() else Optional.of(eventUser)
    }

    override fun findById(id: String, clientId: Long): Optional<EventUser> {
        val eventUser = mongoTemplate.findOne(Query.query(Criteria.where("_id").`is`(id)), EventUser::class.java,"${clientId}_eventUser")
        return if (eventUser==null) Optional.empty() else Optional.of(eventUser)
    }

    override fun save(eventUser: EventUser): EventUser {
        return mongoTemplate.save(eventUser,"${eventUser.clientId}_eventUser")
    }

    override fun deleteById(id: String, clientId: Long) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(id)),"${clientId}_eventUser")
    }
}