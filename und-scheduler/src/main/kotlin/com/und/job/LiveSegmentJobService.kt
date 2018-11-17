package com.und.job

import com.und.config.EventStream
import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import com.und.scheduler.LiveSegmentUserCheck
import com.und.service.JobService
import com.und.util.JobUtil
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class LiveSegmentJobService(private val jobService: JobService, private val eventStream: EventStream) {

    private val logger = LoggerFactory.getLogger(javaClass)

//    @StreamListener("scheduleJobReceive")
    fun scheduleJob(job: JobDescriptor){
        logger.info("Scheduling job- [name: " + job.jobDetail.jobName + ", group: " + job.jobDetail.jobGroupName + "]")

        try {
            jobService.createJob(job)
            logger.info("Scheduled successfully job- [name: " + job.jobDetail.jobName + ", group: " + job.jobDetail.jobGroupName + "]")
        } catch (e: Exception) {
            logger.error("Error in scheduling job- [name: " + job.jobDetail.jobName + ", group: " + job.jobDetail.jobGroupName + "]", e)
        }
    }

    fun executeJob(jobParam: LiveSegmentUserCheck) {
        logger.info("Executing live-segment job: " + jobParam.toString())
        eventStream.outJobForLiveSegment().send(MessageBuilder.withPayload(jobParam).build())
        logger.info("Pushed live-segment job: " + jobParam.toString())
    }

}