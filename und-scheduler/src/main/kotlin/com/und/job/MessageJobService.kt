package com.und.job

import com.und.config.EventStream
import com.und.model.JobAction
import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import com.und.service.JobService
import com.und.util.JobUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class MessageJobService {

    @Autowired
    lateinit var jobService: JobService

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var eventStream: EventStream

    fun executeJob(pair: Pair<String, String>): String {

        logger.info("The job has begun...")
        try {
            //eventStream.campaignTriggerEvent()
            //below null pointer has been added because it doesnt get initiated before execuite job gets called, may be constructor should have been used here
            eventStream?.let { stream ->
                stream.campaignTriggerEvent().send(MessageBuilder.withPayload(pair).build())
            }

        } catch (e: Exception) {
            logger.error("Error while executing job", e)
        } finally {
            logger.info("job has finished...")
        }
        return pair.first
    }


    @StreamListener("scheduleJobReceive")
    @SendTo("scheduleJobAckSend")
    fun save(jobDescriptor: JobDescriptor): JobActionStatus {
        val action = jobDescriptor.action
        return when (action) {
            JobDescriptor.Action.CREATE -> {
                performAction(jobDescriptor, jobService::createJob)
            }
            JobDescriptor.Action.PAUSE -> {
                performAction(jobDescriptor, jobService::pauseJob)
            }
            JobDescriptor.Action.RESUME -> {
                performAction(jobDescriptor, jobService::resumeJob)
            }
            JobDescriptor.Action.DELETE -> {
                performAction(jobDescriptor, jobService::deleteJob)
            }

            JobDescriptor.Action.STOP -> {
                performAction(jobDescriptor, jobService::deleteJob)
            }
            JobDescriptor.Action.NOTHING -> {
                val status = jobActionStatus(jobDescriptor, action)
                status.status = JobActionStatus.Status.NOTFOUND
                status.message = "No action with this name can be performed"
                status
            }
        }


    }

    private fun performAction(jobDescriptor: JobDescriptor, perform: (JobDescriptor) -> JobActionStatus): JobActionStatus {
        val job = jobService.findJob(jobDescriptor)
        val status = jobActionStatus(jobDescriptor, jobDescriptor.action)
        if (job.isPresent) {
            status.status = JobActionStatus.Status.DUPLICATE
            status.message = "Cant Perform ${jobDescriptor.action.name}  on schedule as it already exists."
        } else {
            val actionStatus =try {
                  val actionStatus = perform(jobDescriptor)
                status.status = actionStatus.status
                status.message = actionStatus.message
                status
            } catch (e: Exception) {
                status.status = JobActionStatus.Status.ERROR
                status.message = "Cant Perform ${jobDescriptor.action.name}  on schedule as ${e.localizedMessage}."
            }

        }
        return status
    }


    private fun performAction(jobDescriptor: JobDescriptor, perform: (String, String) -> JobActionStatus): JobActionStatus {
        val job = jobService.findJob(jobDescriptor)
        val status = jobActionStatus(jobDescriptor, jobDescriptor.action)
        if (job.isPresent) {
            val group: String = JobUtil.getGroupName(jobDescriptor.clientId)
            val name: String = JobUtil.getJobName(jobDescriptor.campaignId, jobDescriptor.campaignName)
            val actionStatus = perform(group, name)
            status.status = actionStatus.status
            status.message = actionStatus.message
        } else {
            status.status = JobActionStatus.Status.NOTFOUND
            status.message = "Cant Perform ${jobDescriptor.action.name}  on schedule as it doesn't exists."
        }
        return status
    }

    private fun jobActionStatus(jobDescriptor: JobDescriptor, action: JobDescriptor.Action): JobActionStatus {
        val jobAction = JobAction(
                campaignId = jobDescriptor.campaignId,
                clientId = jobDescriptor.clientId,
                campaignName = jobDescriptor.campaignName,
                action = action
        )
        val status = JobActionStatus()
        status.jobAction = jobAction
        return status
    }
}
