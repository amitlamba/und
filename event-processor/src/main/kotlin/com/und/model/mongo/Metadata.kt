package com.und.model.mongo

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.TextNode
import com.sun.org.apache.xpath.internal.operations.Bool
import com.und.model.*
import com.und.model.Unit
import com.und.service.SegmentCriteriaGroup
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.ZoneId
import javax.xml.soap.Text

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
    @JsonDeserialize(using = CustomDeserializer::class)
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
    var whereCount:Int = 0
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

//
//class CustomSerializer : JsonSerializer<TriggerInfo>(){
//    override fun serialize(value: TriggerInfo?, gen: JsonGenerator?, serializers: SerializerProvider?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//}

class CustomDeserializer(vc: Class<*>?) : StdDeserializer<TriggerInfo>(vc) {

    constructor():this(null){

    }
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): TriggerInfo? {
            //var c = p!!.codec.readValues(p,TriggerInfo::class.java)
        var a = p!!.codec.readTree<TreeNode>(p)
        var t =a["timeZoneId"] as TextNode
        var trig = a["triggerPoint"] as ArrayNode
        var s =a["previousTriggerPoint"] as? TextNode
        var next = a["nextTriggerPoint"] as TextNode
        var error  = a["error"] as? TextNode
        var success = a["successful"] as BooleanNode
        val previousTriggerPoint = s?.let { LocalDateTime.parse(s.asText()) }
        var trigger  = TriggerInfo(previousTriggerPoint, LocalDateTime.parse(next.asText()),error?.textValue(),success.asBoolean())
            trigger.timeZoneId = ZoneId.of(t.asText())

        trigger.triggerPoint = trig.map {
            val triggerPoint = TriggerPoint()
            with(triggerPoint){
                name = it["name"].asText()
                unit = Unit.valueOf(it["unit"].asText())
                interval = it["interval"].asInt()
                lastExecutionPoint = LocalDateTime.parse(it["lastExecutionPoint"].asText())
            }
            triggerPoint
        }
        return trigger
    }
}