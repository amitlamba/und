package com.und.model.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("segmentusers")
class SegmentUsers {
    @field:Id
    var segmentId:Long?=null
    var clientId:Long?=null
    var users:Set<String> = setOf()
}