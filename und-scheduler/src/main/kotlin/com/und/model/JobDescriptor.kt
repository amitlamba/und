package com.und.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.und.scheduler.CampaignJob
import com.und.util.JobUtil
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Trigger
import java.time.ZoneId
import java.util.*
import javax.validation.constraints.NotBlank

class JobDescriptor {

    @NotBlank
    lateinit var clientId: String

    var fireIndex: String = "0"

    @NotBlank
    lateinit var jobDetail: com.und.model.JobDetail

    @JsonProperty("triggers")
    var triggerDescriptors: List<TriggerDescriptor> = listOf()

    var action: Action = Action.NOTHING

    var timeZoneId:ZoneId = ZoneId.of("UTC")

    enum class Action {
        PAUSE, CREATE, RESUME, DELETE, STOP, NOTHING, COMPLETED,FORCE_PAUSE
    }

    /**
     * Convenience method for building Triggers of Job
     *
     * @return Triggers for this JobDetail
     */
    @JsonIgnore
    fun buildTriggers(): Set<Trigger> {
        val triggers = LinkedHashSet<Trigger>()
        triggerDescriptors.forEach {
            triggers.add(it.buildTrigger(this))
        }
        return triggers
    }

    /**
     * Convenience method that builds a JobDetail
     *
     * @return the JobDetail built from this descriptor
     */
    fun buildJobDetail(): JobDetail {
        return JobBuilder.newJob(CampaignJob::class.java)
                .withIdentity(JobUtil.getJobName(this), JobUtil.getGroupName(this))
                .usingJobData("clientId", clientId)
                .usingJobData("fireIndex", fireIndex)
                .usingJobData(JobDataMap(jobDetail.properties))
                .build()
    }

    fun calendarName() = "${this.jobDetail.jobName}"

    companion object {
        /**
         * Convenience method that builds a descriptor from JobDetail and Trigger(s)
         *
         * @param jobDetail
         * the JobDetail instance
         * @param triggersOfJob
         * the Trigger(s) to associate with the Job
         * @return the JobDescriptor
         */
        fun buildDescriptor(quartzJobDetail: JobDetail, triggersOfJob: List<Trigger>): JobDescriptor {
            val triggerDescriptors = arrayListOf<TriggerDescriptor>()

            triggersOfJob.forEach { triggerDescriptors.add(TriggerDescriptor.buildDescriptor(it)) }

            val jobDescriptor = JobDescriptor()
            val jobDetail = com.und.model.JobDetail()
            jobDetail.jobName = quartzJobDetail.key.name
            jobDetail.jobGroupName = quartzJobDetail.key.group
            jobDetail.properties = quartzJobDetail.jobDataMap.filter { it -> !setOf<String>("clientId", "fireIndex").contains(it.key) }.mapValues { it -> it.value.toString() }

            with(jobDescriptor) {
                this.clientId = quartzJobDetail.jobDataMap["clientId"] as String
                this.fireIndex = quartzJobDetail.jobDataMap["fireIndex"] as String
                this.jobDetail = jobDetail
                this.triggerDescriptors = triggerDescriptors

            }
            return jobDescriptor
        }

    }
}