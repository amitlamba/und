package com.und.livesegment.repository.jpa

import com.und.livesegment.model.jpa.LiveSegment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LiveSegmentRepository: JpaRepository<LiveSegment, Long> {

    fun findByClientIDAndStartEvent(clientId : Long, startEvent: String): List<LiveSegment>?

    fun findByClientIDAndEndEvent(clientId : Long, startEvent: String): List<LiveSegment>?

    fun findByClientID(clientId: Long):Optional<List<LiveSegment>>

    fun findByClientIDAndId(clientId:Long,id:Long):Optional<LiveSegment>
}