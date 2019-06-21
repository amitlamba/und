package com.und.model.mongo

import com.und.web.model.*
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime


/*
* if a segment contain after,relative operator in any of did and did not then it will never end.
* segment which contain only event or user properties are never going to end.
*specify the segment which are affected by push proile only
* */
@Document("segment_metadata")
class Metadata {

     var id:Long?=null   // this is same as of segment id
     var clientId:Long?=null
     var status:String = "past" //live
     var stopped:Boolean =false
     var segment: Segment?=null
     var triggerInfo:TriggerInfo? = null                    //it will be null for those segment which are past computed(dead)
     var criteriaGroup:SegmentCriteriaGroup = SegmentCriteriaGroup.DID_DIDNOT_EVENTPROP
     var didEvents:List<MetaEvent> = listOf()
     var didNotEvents:List<MetaEvent> = listOf()
     var globalFilter:Map<String,List<GlobalFilter>> = mapOf()
     var geoFilter:List<Geography> = listOf()
}

class MetaEvent{
     lateinit var name:String
     lateinit var operator:String
     var size:Int = 0
     lateinit var date:List<String>
     var property:List<PropertyFilter> = listOf()
     var conditionalOperator: ConditionType = ConditionType.AllOf
}

/**
 * we scheduled another job which check all segment where error occurred.
 * @param previousTriggerPoint its store the date which is passed(for this date segment should be competed without error if there is error then recompute). it may be null
 * @param nextTriggerPoint here we store the date for which segment computation is scheduled.
 * @param error it store any error which occur during segmentation computation.
 * @param successful true when segment computed successfully.
 */
data class TriggerInfo(val previousTriggerPoint: LocalDateTime?, val nextTriggerPoint:LocalDateTime, val error:String?, val successful:Boolean)


enum class SegmentCriteriaGroup {
     DID,
     DIDNOT,
     DID_DIDNOT,
     EVENTPROP,
     USERPROP,
     DID_EVENTPROP,
     DIDNOT_EVENTPROP,
     DID_USERPROP,
     DIDNOT_USERPROP,
     DID_DIDNOT_EVENTPROP,
     EVENTPROP_USERPROP,
     DID_DIDNOT_USERPROP,
     DID_DIDNOT_EVENTPROP_USERPROP,
     NONE
}