package com.und.repository.jpa

import com.und.model.jpa.LiveSegment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LiveSegmentRepository: JpaRepository<LiveSegment, Long> {

    fun findByClientIDAndSegmentId(clientId: Long,segmentId:Long):Optional<LiveSegment>
}