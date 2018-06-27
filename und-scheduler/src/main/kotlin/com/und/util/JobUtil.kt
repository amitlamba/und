package com.und.util

import com.und.model.JobDescriptor
object JobUtil {

    fun getJobName(job: JobDescriptor): String {
        return getJobName(job.campaignId, job.campaignName, job.fireIndex)

    }

    fun getGroupName(job: JobDescriptor): String {
        return getGroupName(job.clientId, job.campaignId)
    }

    fun getJobName(campaignId:String,campaignName:String, fireIndex:String): String {
        return "$campaignId-$campaignName-$fireIndex"
    }

    fun getGroupName(clientId:String, campaignId:String): String {
        return "$clientId-$campaignId"
    }


}

