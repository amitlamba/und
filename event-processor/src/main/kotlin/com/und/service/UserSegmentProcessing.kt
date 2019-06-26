package com.und.service

import com.netflix.discovery.converters.Auto
import com.und.model.GlobalFilter
import com.und.model.GlobalFilterType
import com.und.model.IncludeUsers
import com.und.model.UpdateIdentity
import com.und.model.mongo.Metadata
import com.und.model.web.EventUser
import com.und.repository.mongo.MetadataRepository
import com.und.service.segmentquerybuilder.SegmentService
import com.und.utils.Constants
import com.und.utils.copyNonNullMongo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service

/* computation procss
*find eventuser for this userId
*check is there any property changed
*if yes then check did new prop match segment user filter if yes then do nothing else remove user.
* */
@Service
class UserSegmentProcessing {


    @Autowired
    private lateinit var segmentService:SegmentService

    @Autowired
    private lateinit var metadataRepository:MetadataRepository
    //changed stopped to considerable
    @StreamListener(Constants.PROCESS_USER_SEGMENT)
    fun processSegment(eventUser:EventUser) {
        val liveSegments = getMetadataOfLiveSegment(eventUser.identity.clientId!!.toLong(), "live", false)
        val pastSegments = getMetadataOfLiveSegment(eventUser.identity.clientId!!.toLong(), "past", false)
        pastSegments.forEach {
            if(isUserPropertiesChanged(eventUser)) computeSegment(metadata = it,eventUser = eventUser)
        }
        liveSegments.forEach {
            if(isUserPropertiesChanged(eventUser)) computeSegment(metadata = it,eventUser = eventUser)
        }
    }
    private fun isUserPropertiesChanged(eventUser: EventUser):Boolean{
        return (eventUser.dob!=null || !eventUser.additionalInfo.isEmpty() || eventUser.gender!=null)
    }

    private fun getMetadataOfLiveSegment(clientId: Long, status: String, stopped: Boolean): List<Metadata> {
        return metadataRepository.findByClientIdAndTypeAndStopped(clientId, status, stopped)
    }
    private fun computeSegment(metadata: Metadata,eventUser: EventUser){
        when (metadata.criteriaGroup) {
            SegmentCriteriaGroup.DID_USERPROP,
            SegmentCriteriaGroup.DID_DIDNOT_USERPROP,
            SegmentCriteriaGroup.DIDNOT_USERPROP,
            SegmentCriteriaGroup.USERPROP,
            SegmentCriteriaGroup.DID_DIDNOT_EVENTPROP_USERPROP,
            SegmentCriteriaGroup.EVENTPROP_USERPROP,
            SegmentCriteriaGroup.DID_EVENTPROP_USERPROP -> {
                val userId = eventUser.identity.userId!!
                val clientId = metadata.clientId!!
                //check segment without userprop and match userprop custom
                val isPresent = segmentService.isUserPresentInSegmentWithoutUserProp(metadata.segment,clientId,IncludeUsers.ALL,null,userId)
                if(isPresent) {
                    //check user prop
                    val result = checkUserProp(metadata.userGlobalFilter,eventUser)
                    if(result) segmentService.addUserInSegment(clientId = clientId,userId = userId,segmentId = metadata.id!!)
                }else{
                    //remove  chek in detail this case
                    segmentService.removeUserFromSegment(userId,clientId,metadata.id!!)
                }
            }
            else -> {
                    //by push profile only those segment are affected which contain userproperty.
            }
        }
    }

    private fun checkUserProp(filter : Map<String,List<GlobalFilter>>,eventUser: EventUser):Boolean{
        val resultList = mutableListOf<Boolean>()
        filter.forEach { key, value ->
            val result = value.map {filter ->
                when(filter.globalFilterType){
                    GlobalFilterType.UserProperties -> true
                    GlobalFilterType.Demographics -> {
                        when(filter.name){
                            "age" -> {

                            }
                            "gender" -> {

                            }
                        }
                    }
                    GlobalFilterType.Reachability -> true
                }
            }
            resultList.add(result.contains(true))
        }
        return !resultList.contains(false)
    }
}