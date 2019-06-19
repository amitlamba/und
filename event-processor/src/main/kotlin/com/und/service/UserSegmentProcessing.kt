package com.und.service

import com.und.model.mongo.Metadata
import com.und.model.web.EventUser
import com.und.utils.Constants
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service

/*
*
* */
@Service
class UserSegmentProcessing {

    @StreamListener(Constants.PROCESS_USER_SEGMENT)
    fun processSegment(eventUser:EventUser){
        //check all those segment which contain user properties.
        // create a funtion to match user property
        val liveSegments = listOf<Metadata>()
        val pastSegments = listOf<Metadata>()
        liveSegments.forEach {
            if(matchUserProperties(eventUser,it)){
                //check is this userId present or not if yes do nothing else compute
            }else{
                //compute

            }
        }
    }
    private fun matchUserProperties(eventUser: EventUser,metadata: Metadata):Boolean{

    }

    private fun compute(metadata: Metadata){
        when (metadata.criteriaGroup) {
            SegmentCriteriaGroup.DID -> {
                //if return true then add userID in list no need computation.
            }
            SegmentCriteriaGroup.DID_DIDNOT -> {
                //if return true then remove userID in list no need computation.
            }
            SegmentCriteriaGroup.EVENTPROP -> {
                //it return true then add userId
            }
            SegmentCriteriaGroup.USERPROP -> {
                //check it on push profile
            }
            else -> {

            }
        }
    }
}