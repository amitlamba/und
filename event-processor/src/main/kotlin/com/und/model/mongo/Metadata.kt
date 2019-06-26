package com.und.model.mongo

import com.sun.org.apache.xpath.internal.operations.Bool
import com.und.model.*
import com.und.model.Unit
import com.und.service.SegmentCriteriaGroup
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.ZoneId

/*
* if a segment contain after,relative operator in any of did and did not then it will never end.
* segment which contain only event or user properties are never going to end.
*specify the segment which are affected by push proile only
* */
@Document("segment_metadata")
class Metadata {

    var id: Long? = null   // this is same as of segment id
    var clientId: Long? = null
    var type: String = "past" //live
    var stopped: Boolean = false
    lateinit var segment: Segment
    var triggerInfo: TriggerInfo? = null               //it will be null for those segment which are past computed(dead)
    var criteriaGroup: SegmentCriteriaGroup = SegmentCriteriaGroup.DID_DIDNOT_EVENTPROP
    var conditionalOperator: ConditionType = ConditionType.AllOf
    var didEventsSize: Int = 0
    var didNotEventSize: Int = 0
    var didEvents: List<MetaEvent> = listOf()
    var didNotEvents: List<MetaEvent> = listOf()
    var eventGlobalFilter: Map<String, List<GlobalFilter>> = mapOf()
    var userGlobalFilter: Map<String, List<GlobalFilter>> = mapOf()
    var geoFilter: List<Geography> = listOf()
    lateinit var creationTime: LocalDateTime
}

class MetaEvent {
    lateinit var name: String
    lateinit var operator: String
    var consider: Boolean = true
    lateinit var date: List<String>
    var property: List<PropertyFilter> = listOf()
}

/**
 * we scheduled another job which check all segment where error occurred.
 * @param previousTriggerPoint its store the date which is passed(for this date segment should be competed without error if there is error then recompute). it may be null
 * @param nextTriggerPoint here we store the date for which segment computation is scheduled.
 * @param error it store any error which occur during segmentation computation.
 * @param successful true when segment computed successfully.
 */
class TriggerInfo(val previousTriggerPoint: LocalDateTime?, val nextTriggerPoint: LocalDateTime, val error: String?, val successful: Boolean) {
    var timeZoneId: ZoneId = ZoneId.systemDefault()
    var triggerPoint: List<TriggerPoint> = listOf()
}

class TriggerPoint {
    lateinit var name: String
    var unit = Unit.NONE
    var interval = 0
    var lastExecutionPoint: LocalDateTime? = null

    override fun equals(other: Any?): Boolean {
        return other?.let {
            val obj = it as TriggerPoint
            if (obj.unit == this.unit && obj.interval == this.interval) true else false
        } ?: false
    }
}