package com.und.repository.mongo

import com.und.model.mongo.eventapi.EventUser
import com.und.security.utils.TenantProvider
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import java.util.*

class EventUserCustomRepositoryImpl : EventUserCustomRepository {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Autowired
    lateinit var tenantProvider: TenantProvider

    override fun findUserById(id: String, clientId: Long): Optional<EventUser> {
        val q = Query(Criteria.where("id").`is`(id))
        return queryEventUser(q, clientId)
    }

    override fun findUserByGoogleId(id: String, clientId: Long): Optional<EventUser> {
        val q = Query(Criteria.where("identity.googleId").`is`(ObjectId(id)))
        return queryEventUser(q, clientId)

    }





    private fun queryEventUser(q: Query, clientId: Long): Optional<EventUser> {
        val eventUser = mongoTemplate.findOne(q, EventUser::class.java, "${clientId}_eventUser")
        return if (eventUser == null) {
            Optional.empty()
        } else {
            Optional.of(eventUser)
        }
    }

}