package com.und.livesegment.repository.jpa

import com.und.livesegment.model.jpa.LiveSegment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LiveSegmentRepository: JpaRepository<LiveSegment, Long> {

    @Query(value = "select s.id, s.name from live_segment s where s.client_id = :clientId", nativeQuery = true)
    fun findIdNameByClientID(@Param("clientId") clientId: Long): List<LiveSegment>

    fun findByClientIDAndStartEvent(clientId : Long, startEvent: String): List<LiveSegment>

    fun findByClientIDAndEndEvent(clientId : Long, startEvent: String): List<LiveSegment>

    fun findByClientID(clientId: Long): List<LiveSegment>

    fun findByClientIDAndId(clientId:Long,id:Long):Optional<LiveSegment>

    fun findByClientIDAndSegmentId(clientId: Long,segmentId:Long):Optional<LiveSegment>
}