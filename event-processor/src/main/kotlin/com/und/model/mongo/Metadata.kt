package com.und.model.mongo

import com.und.model.Segment
import com.und.service.SegmentCriteriaGroup
import org.springframework.data.mongodb.core.mapping.Document

/*
* if a segment contain after,relative operator in any of did and did not then it will never end.
* segment which contain only event or user properties are never going to end.
*specify the segment which are affected by push proile only
* */
@Document("segment_metadata")
class Metadata {

     var id:Long?=null   // this is same as of segment id live_id and past_id
     var clientId:Long?=null
     var stopped:Boolean =false
     var segment: Segment?=null
     var criteriaGroup:SegmentCriteriaGroup = SegmentCriteriaGroup.DID_DIDNOT_EVENTPROP

}