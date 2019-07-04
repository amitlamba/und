package com.und.job

import com.und.model.JobAction
import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import com.und.service.JobService
import com.und.util.JobUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service


@Service
class SegmentJobService {

    @Autowired
    private lateinit var jobService:JobService

    @StreamListener("inSegmentScheduleJob")
    fun processSegment(jobDescriptor: JobDescriptor):JobActionStatus{
        return performAction(jobDescriptor,jobService::createJob)
    }

    private fun performAction(jobDescriptor: JobDescriptor,function:(JobDescriptor) -> JobActionStatus):JobActionStatus{
        val group: String = JobUtil.getGroupName(jobDescriptor)
        val name: String = JobUtil.getJobName(jobDescriptor)
        val jobs = jobService.findJob(group, name)
        val status = jobActionStatus(jobDescriptor, jobDescriptor.action)
        if (jobs.isPresent) {
            status.status = JobActionStatus.Status.DUPLICATE
            status.message = "Cant Perform ${jobDescriptor.action.name}  on schedule as it already exists."
        } else {
            try {
                val actionStatus = function(jobDescriptor)
                status.status = actionStatus.status
                status.message = actionStatus.message
            } catch (e: Exception) {
                status.status = JobActionStatus.Status.ERROR
                status.message = "Cant Perform ${jobDescriptor.action.name}  on schedule as ${e.localizedMessage}."
            }

        }
        return status
    }
    private fun jobActionStatus(jobDescriptor: JobDescriptor, action: JobDescriptor.Action): JobActionStatus {
        val jobAction = JobAction(
                campaignId = "",
                clientId = jobDescriptor.clientId,
                campaignName = "",
                action = action
        )
        val status = JobActionStatus()
        status.jobAction = jobAction
        return status
    }
}