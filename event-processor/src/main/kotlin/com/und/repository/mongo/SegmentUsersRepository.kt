package com.und.repository.mongo

import com.und.model.SegmentUsers
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface SegmentUsersRepository:MongoRepository<SegmentUsers,Long>,CustomSegmentUsersRepository {
    fun findByClientId(clientId:Long):List<SegmentUsers>
}