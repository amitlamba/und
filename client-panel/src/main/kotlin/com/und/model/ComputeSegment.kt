package com.und.model

import java.time.ZoneId

class ComputeSegment {
    var segmentId:Long?=null
    var clientId:Long?=null
    var timeZoneId: ZoneId = ZoneId.of("UTC")
    var segmentName:String?=null
}