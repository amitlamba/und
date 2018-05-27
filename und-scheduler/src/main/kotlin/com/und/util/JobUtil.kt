package com.und.util

import com.und.model.JobDescriptor
object JobUtil {

    fun getJobName(job: JobDescriptor): String {
        return " ${job.campaignId} - ${job.campaignName}"
    }

    fun getGroupName(job: JobDescriptor): String {
        return " ${job.clientId}"
    }

    fun getJobName(campaignId:String,campaignName:String): String {
        return " ${campaignId} - ${campaignName}"
    }

    fun getGroupName(clientId:String): String {
        return " ${clientId}"
    }


}

