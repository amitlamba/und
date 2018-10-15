package com.und.util

import com.und.model.JobDescriptor
object JobUtil {

    fun getJobName(job: JobDescriptor): String {
        return "${job.jobDetail.jobName}-${job.fireIndex}"
    }

    fun getGroupName(job: JobDescriptor): String {
        return job.jobDetail.jobGroupName
    }

}

