package com.und.service

import com.und.model.mongo.MetaEvent
import com.und.model.mongo.Metadata
import com.und.model.mongo.SegmentCriteriaGroup
import com.und.web.model.*
import org.springframework.stereotype.Component

@Component
class CreateMetadataService {

    fun createSegmentMetadata(segment: Segment, segmentId: Long, clientId: Long, status: String): Metadata {


        fun buildMetaEvent(events:List<Event>, condition: ConditionType):List<MetaEvent>{

            val result:List<MetaEvent?> = events.map {
                if(it.dateFilter.operator.name == "After" || it.dateFilter.operator.name == "Today"){
                    val metaEvent = MetaEvent()
                    metaEvent.name = it.name
                    metaEvent.operator = it.dateFilter.operator.name
                    metaEvent.size = events.size
                    metaEvent.date = it.dateFilter.values
                    metaEvent.property = it.propertyFilters
                    metaEvent.conditionalOperator = condition
                    metaEvent
                } else null
            }

            return result.filterNotNull()
        }

        fun buildGlobalFilter(globalFilters:List<GlobalFilter>):Map<String,List<GlobalFilter>>{
            return globalFilters.groupBy { it.name }
        }

        fun findCriteriaGroup(): SegmentCriteriaGroup {
            val map = mutableMapOf<Boolean, MutableSet<Int>>()
//            val did = 1
//            val didnot = 2
//            val event = 3
//            val user = 4
            when {
                segment.didEvents!!.events.isNotEmpty() -> map.put(true, mutableSetOf(1))
                segment.didNotEvents!!.events.isNotEmpty() -> {
                    val v = map[true] ?: mutableSetOf(2)
                    v.add(2)
                    map.put(true, v)
                }
                segment.geographyFilters.isNotEmpty() -> {
                    val v = map[true] ?: mutableSetOf(3)
                    v.add(3)
                    map.put(true, v)
                }
                segment.globalFilters.isNotEmpty() -> {
                    val userGlobalFilter: GlobalFilter? = segment.globalFilters.find { (it.globalFilterType == GlobalFilterType.UserProperties || it.globalFilterType == GlobalFilterType.Demographics || it.globalFilterType == GlobalFilterType.Reachability) }
                    val eventGlobalFilter: GlobalFilter? = segment.globalFilters.find { (it.globalFilterType == GlobalFilterType.Technographics || it.globalFilterType == GlobalFilterType.AppFields) }
                    if (eventGlobalFilter != null) {
                        val v = map[true] ?: mutableSetOf(3)
                        v.add(3)
                        map.put(true, v)
                    }
                    if (userGlobalFilter != null) {
                        val v = map[true] ?: mutableSetOf(4)
                        v.add(4)
                        map.put(true, v)
                    }
                }
            }
            var group: SegmentCriteriaGroup = SegmentCriteriaGroup.NONE
            map[true]?.let {
                it.forEach {
                    when (it) {
                        1 -> group = SegmentCriteriaGroup.DID
                        2 -> {
                            if (group == SegmentCriteriaGroup.NONE) {
                                group = SegmentCriteriaGroup.DIDNOT
                            } else {
                                group = SegmentCriteriaGroup.valueOf(group.name + "_DIDNOT")
                            }
                        }
                        3 -> {
                            if (group == SegmentCriteriaGroup.NONE) {
                                group = SegmentCriteriaGroup.EVENTPROP
                            } else {
                                group = SegmentCriteriaGroup.valueOf(group.name + "_EVENTPROP")
                            }
                        }
                        4 -> {
                            if (group == SegmentCriteriaGroup.NONE) {
                                group = SegmentCriteriaGroup.USERPROP
                            } else {
                                group = SegmentCriteriaGroup.valueOf(group.name + "_USERPROP")
                            }
                        }
                    }
                }
            }

            return group
        }
        fun isContainRelativeDate(events:List<Event>):Boolean{
            events.forEach {
                val operator = it.dateFilter.operator
                if( operator== DateOperator.After || operator == DateOperator.Today){
                    return true
                }
            }
            return false
        }
        val group = findCriteriaGroup()
        fun isComputable(): Boolean {
            return when(group){
                SegmentCriteriaGroup.DID_DIDNOT, SegmentCriteriaGroup.DID_DIDNOT_EVENTPROP -> {
                    if(isContainRelativeDate(segment.didEvents!!.events)){
                        true
                    }else{
                        isContainRelativeDate(segment.didNotEvents!!.events)
                    }
                }
                SegmentCriteriaGroup.DIDNOT,SegmentCriteriaGroup.DIDNOT_EVENTPROP -> isContainRelativeDate(segment.didNotEvents!!.events)
                SegmentCriteriaGroup.DID,SegmentCriteriaGroup.DID_EVENTPROP -> isContainRelativeDate(segment.didEvents!!.events)
                SegmentCriteriaGroup.NONE -> false
                else -> true
            }
        }


        val metadata = Metadata()
        with(metadata) {
            id = segmentId
            this.clientId = clientId
            this.status = status
            stopped = isComputable()
            this.segment = segment
            criteriaGroup = findCriteriaGroup()
            didEvents = buildMetaEvent(segment.didEvents!!.events,segment.didEvents!!.joinCondition.conditionType)
            didNotEvents = buildMetaEvent(segment.didNotEvents!!.events,segment.didNotEvents!!.joinCondition.conditionType)
            globalFilter = buildGlobalFilter(segment.globalFilters)
            geoFilter = segment.geographyFilters
        }
        return metadata
    }
}