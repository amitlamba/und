package com.und.livesegment.model.webmodel

import com.esotericsoftware.kryo.NotNull
import com.und.web.model.PropertyFilter
import com.und.web.model.Segment

class WebLiveSegment {

    var id:Long=0L
    var clientId:Long?=null
    @NotNull
    var segment:Segment?=null
    @NotNull
    lateinit var liveSegmentType:String

    @NotNull
    var startEvent: String = ""

    var endEvent: String = ""

    var startEventFilters  = emptyList<PropertyFilter>()

    var endEventFilters  = emptyList<PropertyFilter>()

    var interval: Long = 0L

    var endEventDone:Boolean=false
}


enum class LiveSegmentType(name:String){
    SINEGLE_ACTION("Single Action")
}