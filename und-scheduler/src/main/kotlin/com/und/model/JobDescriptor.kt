package com.und.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.und.scheduler.MessageJob
import com.und.util.JobUtil
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Trigger
import java.util.*
import javax.validation.constraints.NotBlank

class JobDescriptor {

    @NotBlank
    lateinit var clientId: String
    @NotBlank
    lateinit var campaignId : String

    @NotBlank
    lateinit var campaignName: String

    @JsonProperty("triggers")
    var triggerDescriptors: List<TriggerDescriptor> = listOf()

    var action:Action = Action.NOTHING

    enum class Action {
        PAUSE,CREATE,RESUME,DELETE,STOP,NOTHING,
    }

    /**
     * Convenience method for building Triggers of Job
     *
     * @return Triggers for this JobDetail
     */
    @JsonIgnore
    fun buildTriggers(): Set<Trigger> {
        val triggers = LinkedHashSet<Trigger>()
        triggerDescriptors.forEach{
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
        return JobBuilder.newJob(MessageJob::class.java)
                .withIdentity(JobUtil.getJobName(this), JobUtil.getGroupName(this))
                .usingJobData("clientId", clientId)
                .usingJobData("campaignId", campaignId)
                .usingJobData("campaignName",campaignName)
                .build()
    }

    fun calendarName() = "${this.campaignId}_${this.campaignName}"

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
        fun buildDescriptor(jobDetail: JobDetail, triggersOfJob: List<Trigger>): JobDescriptor {
            val triggerDescriptors = arrayListOf<TriggerDescriptor>()

            triggersOfJob.forEach{triggerDescriptors.add(TriggerDescriptor.buildDescriptor(it))}

            val jobDescriptor = JobDescriptor()
            with(jobDescriptor) {
                clientId = jobDetail.jobDataMap["clientId"] as String
                campaignId = jobDetail.jobDataMap["campaignId"] as String
                campaignName = jobDetail.jobDataMap["campaignName"] as String
                this.triggerDescriptors = triggerDescriptors

            }
            return jobDescriptor
        }

    }
}