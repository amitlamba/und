package com.und.repository.mongo

import com.und.model.mongo.EventUser
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.stereotype.Repository
import java.util.*

//@Repository
interface EventUserCustomRepository {

//    fun findUserById(id: String, clientId: Long): Optional<EventUser>
    fun findUserByIds(id: Set<String>, clientId: Long): List<EventUser>
//    fun findUserByGoogleId(id: String, clientId: Long): Optional<EventUser>
//    fun findUserByFbId(id: String, clientId: Long): Optional<EventUser>
//    fun findUserBySysId(id: String, clientId: Long): Optional<EventUser>
//    fun findUserByEmail(id: String, clientId: Long): Optional<EventUser>
//    fun findUserByMobile(id: String, clientId: Long): Optional<EventUser>
//    fun testUserProfile(id: String, clientId: Long, isTestUser: Boolean)
//
//    fun findUsersNotIn(ids: Set<String>, clientId: Long): List<String>
    fun testSegmentUsers( clientId: Long): List<String>
    fun usersFromUserProfile(query: Aggregation, clientId: Long): List<String>
    fun usersProfileFromEventUser(query:List<AggregationOperation>,clientId: Long):List<EventUser>
    fun usersIdFromEventUser(query:List<AggregationOperation>,clientId: Long):List<String>

//    fun findAll(clientId: Long):List<EventUser>

}