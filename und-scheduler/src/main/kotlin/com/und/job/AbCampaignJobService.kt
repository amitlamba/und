package com.und.job

import com.und.config.EventStream
import com.und.service.JobService
import org.quartz.Scheduler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class AbCampaignJobService(private val jobService: JobService, private val eventStream: EventStream) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    protected lateinit var scheduler: Scheduler

    fun executeJob(pair: Pair<String, String>): String {

        logger.info("The job has begun...")
        try {
            eventStream.let { stream ->
                stream.abCampaignTrigger().send(MessageBuilder.withPayload(pair).build())
            }

        } catch (e: Exception) {
            logger.error("Error while executing job", e)
        } finally {
            logger.info("job has finished...")
        }
        return pair.first
    }
}