package com.und.campaign.repository.mongo

import com.und.model.mongo.EventUser
import com.und.model.utils.CampaignType
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class CustomEventUserRepositoryImpl: CustomEventUserRepository {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate


    override fun findByIdAndClientId(id: ObjectId, clientId: Long): EventUser? {
        val query=Query().addCriteria(Criteria.where("clientId").`is`(clientId).and("_id").`is`(id))
        return mongoTemplate.findOne(query,EventUser::class.java,"${clientId}_eventUser")
    }
    override fun findByEmail(clientId: Long, email: String): List<EventUser> {
        val query=Query().addCriteria(Criteria.where("clientId").`is`(clientId).and("identity.email").`is`(email))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findByUndId(clientId: Long, undId: String): List<EventUser> {
        val query=Query().addCriteria(Criteria.where("clientId").`is`(clientId).and("identity.undId").`is`(undId))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findByUid(clientId: Long, uid: String): List<EventUser> {
        val query=Query().addCriteria(Criteria.where("clientId").`is`(clientId).and("identity.uid").`is`(uid))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findByMobile(clientId: Long, mobile: Long): List<EventUser> {
        val query=Query().addCriteria(Criteria.where("clientId").`is`(clientId).and("identity.mobile").`is`(mobile.toString()))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findByAndroidFcmToken(clientId: Long, token: String): List<EventUser> {
        val query=Query().addCriteria(Criteria.where("clientId").`is`(clientId).and("identity.androidFcmToken").`is`(token))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findByWebFcmToken(clientId: Long, token: String): List<EventUser> {
        val query=Query().addCriteria(Criteria.where("clientId").`is`(clientId).and("identity.webFcmToken").`is`(token))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findByEmailIn(clientId: Long,email: Array<String>): List<EventUser> {
        val query=Query().addCriteria(Criteria.where("identity.email").`in`(*email))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findByUndIdIn(clientId: Long,undid: Array<String>): List<EventUser> {
        val query=Query().addCriteria(Criteria.where("identity.undId").`in`(*undid))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findByUidIn(clientId: Long,uid: Array<String>): List<EventUser> {
        val query=Query().addCriteria(Criteria.where("identity.uid").`in`(*uid))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findByMobileIn(clientId: Long,mobile: Array<String>): List<EventUser> {
        val query=Query().addCriteria(Criteria.where("identity.mobile").`in`(*mobile))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findByAndroidFcmTokenIn(clientId: Long,token: Array<String>): List<EventUser> {
        val query=Query().addCriteria(Criteria.where("identity.androidFcmToken").`in`(*token))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findByWebFcmTokenIn(clientId: Long,token: Array<String>): List<EventUser> {
//        val query=Query().addCriteria(Criteria().elemMatch(Criteria("identity.webFcmToken").`in`(*token)))
        val query=Query().addCriteria(Criteria.where("identity.webFcmToken").`in`(*token))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findAllById(clientId: Long, ids: List<ObjectId>):List<EventUser> {
        val query = Query().addCriteria(Criteria.where("_id").`in`(ids))
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }

    override fun findAllByIdAndByCampaignType(clientId: Long, ids: List<ObjectId>, type: CampaignType): List<EventUser> {
        var query = Query()
        query = when(type){
            CampaignType.EMAIL -> {
                query.addCriteria(Criteria.where("_id").`in`(ids).and("identity.email").exists(true))
            }
            CampaignType.SMS ->{
                query.addCriteria(Criteria.where("_id").`in`(ids).and("identity.mobile").exists(true))
            }
            CampaignType.PUSH_ANDROID -> {
                 query.addCriteria(Criteria.where("_id").`in`(ids).and("identity.androidFcmToken").exists(true))
            }
            CampaignType.PUSH_IOS ->{
                 query.addCriteria(Criteria.where("_id").`in`(ids).and("identity.iosFcmToken").exists(true))
            }
            CampaignType.PUSH_WEB -> {
                 query.addCriteria(Criteria.where("_id").`in`(ids).and("identity.webFcmToken").exists(true))
            }
        }
        return mongoTemplate.find(query,EventUser::class.java,"${clientId}_eventUser")
    }
    override  fun testSegmentUsers( clientId: Long): List<String> {
        val query = Query.query(Criteria.where("testUser").`is`(true).and("clientId").`is`(clientId))
        val output = mongoTemplate.find(query,  EventUser::class.java,"${clientId}_eventUser")
        if(output!=null)
            return output.mapNotNull { it.id }
        else
            return emptyList()
    }
}