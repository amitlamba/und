package com.und.repository.jpa

import com.und.model.jpa.Segment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SegmentRepository : JpaRepository<Segment, Long> {
    fun findByClientID(clientId : Long):List<Segment>?

    fun findByIdAndClientID(id:Long, clientId : Long): Optional<Segment>
}




