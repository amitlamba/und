package com.und.scheduler

import com.und.config.EventStream
import com.und.model.ComputeSegment
import com.und.model.JobDescriptor
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.impl.matchers.GroupMatcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.util.*

@Component
class SegmentJob:Job {

    @Autowired
    private lateinit var eventStream: EventStream

    companion object {
        val logger = LoggerFactory.getLogger(SegmentJob::class.java)
    }

    override fun execute(context: JobExecutionContext?) {
        val clientId  =context?.jobDetail?.jobDataMap?.get("clientId")
        val segmentId = context?.jobDetail?.jobDataMap?.get("segmentId")
        val segmentName = context?.jobDetail?.jobDataMap?.get("segmentName")
        val timeZoneId = context?.trigger?.jobDataMap?.get("timezone")

        val computeSegment = ComputeSegment()
        with(computeSegment){
            this.clientId = clientId.toString().toLong()
            this.segmentId = segmentId.toString().toLong()
            this.segmentName = segmentName.toString()
            this.timeZoneId = ZoneId.of(timeZoneId.toString())

        }
        logger.info("segment(cid $clientId - $segmentName - sid $segmentId) job completed successfully.")
        eventStream.computeSegment().send(MessageBuilder.withPayload(computeSegment).build())
    }
}