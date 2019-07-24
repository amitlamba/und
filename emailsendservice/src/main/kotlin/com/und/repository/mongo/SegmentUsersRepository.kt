package com.und.repository.mongo

import com.und.model.mongo.SegmentUsers
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SegmentUsersRepository:MongoRepository<SegmentUsers,Long> {

    fun findBySegmentIdAndClientId(id:Long,clientId:Long): Optional<SegmentUsers>
}