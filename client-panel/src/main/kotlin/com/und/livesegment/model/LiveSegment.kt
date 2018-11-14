package com.und.livesegment.model

import java.util.*

data class EventMessage(
        var eventId: String,
        var clientId: Long,
        var userId: String,
        var name: String,
        var creationTime: Date
)

data class LiveSegmentUser(
        var liveSegmentId: Long,
        var clientId: Long,
        var userId: String,
        var creationTime: Date
)

data class LiveSegmentUserCheck(
        var clientId: String,
        var segmentId: String,
        var startEventId: String,
        var startEventName : String,
        var startEventTime : String,
        var userId: String
)