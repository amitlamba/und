package com.und.scheduler

import com.und.config.EventStream
import com.und.model.ComputeSegment
import com.und.model.JobDescriptor
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.util.*

@Component
class SegmentJob:Job {

    @Autowired
    private lateinit var eventStream: EventStream

    override fun execute(context: JobExecutionContext?) {
        val result = context?.jobDetail
        val computeSegment = ComputeSegment()
        with(computeSegment){

        }
        eventStream.computeSegment().send(MessageBuilder.withPayload(computeSegment).build())
    }
}