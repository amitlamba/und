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
import java.time.LocalDate

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
        //|| eventUser.email!=null || eventUser.mobile!=null || eventUser.androidFcmToken!=null || eventUser.webFcmToken!=null || eventUser.iosFcmToken!=null
        return (eventUser.dob!=null || !eventUser.additionalInfo.isEmpty() || eventUser.gender!=null )
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
                //here we are computing segment only if uid is null
                eventUser.uid?.let {
                    if(it.isNotBlank()){
                        val result = didWeCompute(eventUser, metadata.userGlobalFilter)
                        if (result) {
                            val isPresent = segmentService.isUserPresentInSegmentWithoutUserProp(metadata.segment, clientId, IncludeUsers.ALL, null, userId)
                            if(isPresent)
                                segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = metadata.id!!)
                            else
                                segmentService.removeUserFromSegment(userId, clientId, metadata.id!!)
                        }
                    }
                }
            }
            else -> {
                    //by push profile only those segment are affected which contain userproperty.
            }
        }
    }

    fun didWeCompute(eventUser: EventUser,filter: Map<String, List<GlobalFilter>>):Boolean{
        val didWeCompute1 = filter["Demographics"]?.let {
            val result1 = if(eventUser.dob!=null){
                containAgeDemoFilter(it)
            }else false
            val result2 = if(eventUser.gender!=null){
                containGenderDemoFilter(it)
            }else false

            result1 || result2
        }?:false

        val didWeCompute2 = filter["UserProperties"]?.let {
             eventUser.additionalInfo.isNotEmpty()
        }?:false

        val didWeCompute3 = filter["Reachability"]?.let {
            val result1 = if(eventUser.email!=null){
                containEmailFilter(it)
            }else false
            val result2 = if(eventUser.mobile!=null){
                containMobileFilter(it)
            }else false
            val result3 = if(eventUser.androidFcmToken!=null){
                containAndroidTokenFilter(it)
            }else false
            val result4 = if(eventUser.webFcmToken!=null){
                containWebTokenFilter(it)
            }else false
            val result5 = if(eventUser.iosFcmToken!=null){
                containIosTokenFilter(it)
            }else false

            (result1 || result2 || result3 || result4 || result5)
        }?:false
        return (didWeCompute1 || didWeCompute2 || didWeCompute3)
    }

    private fun containAgeDemoFilter(filter: List<GlobalFilter>):Boolean{
        val result = filter.map {
            when(it.name){
                "age" -> {
                    true
                }
                else -> false
            }
        }
        return result.contains(true)
    }
    private fun containGenderDemoFilter(filter: List<GlobalFilter>):Boolean{
        val result = filter.map {
            when(it.name){
                "gender" -> {
                    true
                }
                else -> false
            }
        }
        return result.contains(true)
    }

    private fun containEmailFilter(filter: List<GlobalFilter>):Boolean{
        val result = filter.map {
            when(it.name){
                "hasEmailAddress","unsubscribedEmail" -> {
                    true
                }
                else -> false
            }
        }
        return result.contains(true)
    }

    private fun containMobileFilter(filter: List<GlobalFilter>):Boolean{
        val result = filter.map {
            when(it.name){
                "hasPhoneNumber","unsubscribedSms" -> {
                    true
                }
                else -> false
            }
        }
        return result.contains(true)
    }

    private fun containAndroidTokenFilter(filter: List<GlobalFilter>):Boolean{
        val result = filter.map {
            when(it.name){
                "hasAndroid","unsubscribedAndroidPush" -> {
                    true
                }
                else -> false
            }
        }
        return result.contains(true)
    }

    private fun containWebTokenFilter(filter: List<GlobalFilter>):Boolean{
        val result = filter.map {
            when(it.name){
                "hasWeb","unsubscribedWebPush" -> {
                    true
                }
                else -> false
            }
        }
        return result.contains(true)
    }

    private fun containIosTokenFilter(filter: List<GlobalFilter>):Boolean{
        val result = filter.map {
            when(it.name){
                "hasIos","unsubscribedIosPush" -> {
                    true
                }
                else -> false
            }
        }
        return result.contains(true)
    }

}