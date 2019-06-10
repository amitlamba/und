package com.und.model.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.jpa.repository.Temporal
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.persistence.TemporalType

//@Document(collection = "#{tenantProvider.getTenant()}_livesegmenttrack")
data class LiveSegmentTrack(
        @Id
        var id: String? = null, //Mongo Auto-generated Document id
        var clientID: Long,
        var date: LocalDate = LocalDate.now(ZoneId.of("UTC")),
        var time: LocalTime = LocalTime.now(ZoneId.of("UTC")),
        var segmentId: Long,
        var liveSegmentId: Long,
        var userId: String
)