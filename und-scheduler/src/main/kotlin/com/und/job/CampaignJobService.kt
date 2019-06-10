package com.und.job

import com.und.config.EventStream
import com.und.model.JobAction
import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import com.und.model.TriggerDescriptor
import com.und.service.JobService
import com.und.util.JobUtil
import org.quartz.Scheduler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CampaignJobService(private val jobService: JobService, private val eventStream: EventStream) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    protected lateinit var scheduler: Scheduler

    fun executeJob(pair: Pair<String, String>): String {

        logger.info("The job has begun...")
        try {
            //eventStream.campaignTriggerEvent()
            //below null pointer has been added because it doesnt get initiated before execuite job gets called, may be constructor should have been used here
            eventStream.let { stream ->
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
                val trigger = jobDescriptor.triggerDescriptors.first()
                val fireTime = trigger.fireTime
                val cron = trigger.cron
                val fireTimes = trigger.fireTimes
                if (cron === null && fireTime == null && fireTimes != null && fireTimes.isNotEmpty()) {
                    multipleDateTrigger(fireTimes, jobDescriptor)

                } else {
                    performAction(jobDescriptor, jobService::createJob)
                }
            }
            JobDescriptor.Action.PAUSE , JobDescriptor.Action.FORCE_PAUSE  -> {
                performActionGroup(jobDescriptor, jobService::pauseJobs)
            }
            JobDescriptor.Action.RESUME -> {
                performActionGroup(jobDescriptor, jobService::resumeJobs)
            }
            JobDescriptor.Action.DELETE -> {
                performActionGroup(jobDescriptor, jobService::deleteJobs)
            }

            JobDescriptor.Action.STOP -> {
                performActionGroup(jobDescriptor, jobService::deleteJobs)
            }
            JobDescriptor.Action.COMPLETED ,  JobDescriptor.Action.NOTHING-> {
                val status = jobActionStatus(jobDescriptor, action)
                status.status = JobActionStatus.Status.NOTFOUND
                status.message = "No action with this name can be performed"
                status
            }
        }


    }
    @StreamListener("scheduleLiveJobReceive")
    fun saveLiveSegmentJob(jobDescriptor: JobDescriptor){
        val action = jobDescriptor.action
        when (action) {
            JobDescriptor.Action.CREATE -> {
                val trigger = jobDescriptor.triggerDescriptors.first()
                val fireTime = trigger.fireTime
                val cron = trigger.cron
                val fireTimes = trigger.fireTimes
                if (cron === null && fireTime == null && fireTimes != null && fireTimes.isNotEmpty()) {
                    multipleDateTrigger(fireTimes, jobDescriptor)

                } else {
                    performAction(jobDescriptor, jobService::createJob)
                }
            }
            JobDescriptor.Action.NOTHING-> {
                val status = jobActionStatus(jobDescriptor, action)
                status.status = JobActionStatus.Status.NOTFOUND
                status.message = "No action with this name can be performed"

            }
            else ->{
                logger.error("schedule  live job can't receive ${jobDescriptor.action}")
            }
        }
    }
    private fun multipleDateTrigger(fireTimes: List<LocalDateTime>, jobDescriptor: JobDescriptor): JobActionStatus {
        val trigger: TriggerDescriptor = jobDescriptor.triggerDescriptors.first()
        val actions = mutableListOf<JobActionStatus>()
        for (i in 0 until fireTimes.size) {
            trigger.fireTime = fireTimes[i]
            jobDescriptor.fireIndex = i.toString()
            val action = performAction(jobDescriptor, jobService::createJob)
            actions.add(action)
        }
        val status = jobActionStatus(jobDescriptor, jobDescriptor.action)
        for (action in actions) {
            if (action.status != JobActionStatus.Status.OK) {
                status.status = action.status
                status.message = action.message
                break
            }
        }

        return status
    }

    private fun performAction(jobDescriptor: JobDescriptor, perform: (JobDescriptor) -> JobActionStatus): JobActionStatus {
        val group: String = JobUtil.getGroupName(jobDescriptor)
        val name: String = JobUtil.getJobName(jobDescriptor)
        val jobs = jobService.findJob(group, name)
        val status = jobActionStatus(jobDescriptor, jobDescriptor.action)
        if (jobs.isPresent) {
            status.status = JobActionStatus.Status.DUPLICATE
            status.message = "Cant Perform ${jobDescriptor.action.name}  on schedule as it already exists."
        } else {
            try {
                val actionStatus = perform(jobDescriptor)
                status.status = actionStatus.status
                status.message = actionStatus.message
            } catch (e: Exception) {
                status.status = JobActionStatus.Status.ERROR
                status.message = "Cant Perform ${jobDescriptor.action.name}  on schedule as ${e.localizedMessage}."
            }

        }
        return status
    }


    private fun performActionGroup(jobDescriptor: JobDescriptor, perform: (String) -> JobActionStatus): JobActionStatus {
        val jobs = jobService.findJob(jobDescriptor)
        val status = jobActionStatus(jobDescriptor, jobDescriptor.action)

        if (jobs.isNotEmpty()) {
            val group: String = JobUtil.getGroupName(jobDescriptor)
            val actionStatus = perform(group)
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
                campaignId = jobDescriptor.jobDetail.properties["campaignId"].toString(),
                clientId = jobDescriptor.clientId,
                campaignName = jobDescriptor.jobDetail.properties["campaignName"].toString(),
                action = action
        )
        val status = JobActionStatus()
        status.jobAction = jobAction
        return status
    }
}
