package com.und.service

import com.mongodb.MongoClient
import com.und.model.mongo.SegmentReachability
import com.und.model.mongo.eventapi.Event
import org.bson.types.ObjectId
import org.junit.Test
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.time.LocalDateTime
import java.time.ZoneId

class Migration {


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            var migration = Migration()
            //Migrating event

            migration.migrateEvent(1026)
            //Migrate eventUser
//            migration.migrateEventUser(1026)
            //Migrate SegmentReport
//            migration.migrateSegmentReport(1026)

            migration.migrateEvent(1018)
            //Migrate eventUser
//            migration.migrateEventUser(1018)
            //Migrate SegmentReport
//            migration.migrateSegmentReport(1018)

        }
    }

    fun migrateEventUser(clientId: Long){
        var mongoTemplate = mongoClient()
        var query=Query.query(Criteria().and("identity.uid").exists(true))
        var update = Update().set("identity.identified",true).unset("identity.identified")
        mongoTemplate.updateMulti(query,update,"${clientId}_eventUser")

        var query1=Query.query(Criteria().and("identity.uid").exists(false))
        var update1 = Update().set("identity.identified",false).unset("identity.identified")
        mongoTemplate.updateMulti(query1,update1,"${clientId}_eventUser")

    }

    fun migrateEvent(clientId:Long){
        //fetch all event where user id not present
        var mongoTemplate = mongoClient()
        var events = mongoTemplate.find<Event>(Query.query(Criteria.where("userId").exists(false)),"${clientId}_event")
        //group them by deviceId and sessionId
        var groupByEvent = mutableMapOf<String,List<Event>>()
        events.forEach {
            var key=it.deviceId+it.sessionId
            if(groupByEvent.containsKey(key)){
                var eventList = groupByEvent[key]?.toMutableList()
                eventList?.add(it)
                groupByEvent.put(key,eventList?.toList()?: emptyList())
            }else{
                groupByEvent.put(key, listOf(it))
            }
        }

        groupByEvent.forEach { t, u ->
            //create an event user for each group and assign that event user id to those event.
            var userId = ObjectId().toString()

            val eventUser = com.und.model.mongo.eventapi.EventUser()
            val identity_ = com.und.model.mongo.eventapi.Identity()

            with(identity_) {
                undId = userId
            }
            with(eventUser) {
                this.id = userId
                this.identity = identity_
                this.clientId = u[0].clientId
            }

//            u.forEach {
//                it.userId =userId
//                mongoTemplate.save(it,"${clientId}_event")
//            }

            var query = Query.query(Criteria.where("deviceId").`is`(u[0].deviceId).and("sessionId").`is`(u[0].sessionId))
            //if a user is already present with this deviceid and sessionid use that user instead of new user
            //arrayOf(find all event with this deviceid and session id where userid is exists)
            val query1= Query.query(Criteria.where("deviceId").`is`(u[0].deviceId).and("sessionId").`is`(u[0].sessionId).and("userId").exists(true))
            val eventsWithuser=mongoTemplate.find<Event>(query1,"${clientId}_event")
            if(eventsWithuser.isNotEmpty()){
                userId = eventsWithuser[0].userId!!
                var update=Update().set("userId",userId).set("userIdentified",true)
                mongoTemplate.updateMulti(query,update,"${clientId}_event")
            }else{
                mongoTemplate.insert(eventUser,"${clientId}_eventUser")
                var update=Update().set("userId",userId).set("userIdentified",false)
                mongoTemplate.updateMulti(query,update,"${clientId}_event")
            }

        }
    }

    fun migrateSegmentReport(clientId: Long){
        var mongoTemplate = mongoClient()
        var segments = mongoTemplate.find<SegmentReachability1>(Query.query(Criteria.where("clientId").`is`(clientId)),"segmentreachability")
        segments.forEach {
            var newSegmentReachability = SegmentReachability()
            with(newSegmentReachability) {
                id = it.id
                timeZone = it.timeZone
                this.clientId = it.clientId
                lastModifiedTime = it.lastModifiedTime
                dates = buildDate(it.dates)
            }
            mongoTemplate.save(newSegmentReachability,"segmentreachability")
        }
    }

    private fun buildDate(dates:Map<Int,Int>):MutableMap<Int,Map<String,Int>>{
          val newDates = mutableMapOf<Int,Map<String,Int>>()
        dates.forEach {
            val known=it.value
            val unknown=0
            val count = mutableMapOf<String,Int>()
            count.put("known",known)
            count.put("unknown",unknown)
            val key=it.key
            newDates.put(key,count)
        }
        return newDates
    }

    private fun mongoClient():MongoTemplate{
        var mongoClient = MongoClient("192.168.0.109",27017)
//        var mongoTemplate=MongoTemplate(mongoClient,"eventdbstaging")
        var mongoClientprod = MongoClient("172.31.22.48",27017)
        var mongoTemplate=MongoTemplate(mongoClientprod,"eventdb")
        return mongoTemplate
    }

}

class SegmentReachability1 {
    var id:Long?=null
    var timeZone:String="UTC"
    var clientId:Long?=null
    var  dates :Map<Int,Int> = mutableMapOf()
    var lastModifiedTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
}