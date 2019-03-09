package com.und.livesegment.model.webmodel

import com.esotericsoftware.kryo.NotNull
import com.und.web.model.PropertyFilter
import com.und.web.model.Segment

class WebLiveSegment {

    @NotNull
    lateinit var segment:Segment
    @NotNull
    lateinit var liveSegmentType:String

    @NotNull
    var startEvent: String = ""

    var endEvent: String = ""

    var startEventFilter  = emptyList<PropertyFilter>()

    var endEventFilter  = emptyList<PropertyFilter>()

    var interval: Long = 0L
}