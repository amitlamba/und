package com.und.service

import com.und.model.FixedDateCalendar
import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import com.und.util.loggerFor
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.slf4j.Logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class JobProcessService : AbstractJobService() {

    companion object {
        protected val logger: Logger = loggerFor(JobProcessService::class.java)
    }



    /**
     * {@inheritDoc}
     */
    override fun createJob(descriptor: JobDescriptor): JobActionStatus {
        val jobstatus = JobActionStatus()
        val jobDetail = descriptor.buildJobDetail()
        val triggersForJob = descriptor.buildTriggers()
        JobProcessService.logger.info("About to save job with key - ${jobDetail.key}")
        try {
            scheduler.addCalendarIfRequired(descriptor)
            scheduler.scheduleJob(jobDetail, triggersForJob, false)
            logger.info("Job with key - ${jobDetail.key} saved successfully")
        } catch (e: SchedulerException) {
            logger.error("Could not save job with key - ${jobDetail.key} due to error - ${e.localizedMessage}")
            jobstatus.status = JobActionStatus.Status.ERROR
            jobstatus.message = e.localizedMessage
        }

        return jobstatus
    }



    /**
     * {@inheritDoc}
     */
    override fun updateJob(group: String, name: String, descriptor: JobDescriptor):JobActionStatus {
        val jobstatus = JobActionStatus()
        try {
            val oldJobDetail = scheduler.getJobDetail(JobKey.jobKey(name, group))
            if (oldJobDetail != null) {
                val jobDataMap = oldJobDetail.jobDataMap
                val jb = oldJobDetail.jobBuilder
                val newJobDetail = jb.usingJobData(jobDataMap).storeDurably().build()
                scheduler.addJob(newJobDetail, true)
                logger.info("Updated job with key - ${newJobDetail.key}")
                return jobstatus
            }
            logger.warn("Could not find job with key - $group.$name  to update", group, name)
        } catch (e: SchedulerException) {
            logger.error("Could not find job with key - $group.$name due to error - ${e.localizedMessage}")
            jobstatus.status = JobActionStatus.Status.ERROR
            jobstatus.message = e.localizedMessage
        }
        return jobstatus

    }


    private fun Scheduler.addCalendarIfRequired(descriptor: JobDescriptor) {
        val fireTimes = descriptor.triggerDescriptors.map { it.fireTimes }
        if (fireTimes.isNotEmpty()) {
            val muiltipFireTimes = fireTimes.first()?.isNotEmpty()
            if (muiltipFireTimes != null && muiltipFireTimes) {
                val dates = fireTimes.first() ?: mutableListOf()
                val cal = FixedDateCalendar(dates)
                this.addCalendar(descriptor.calendarName(), cal, false, true)
            }
        }
    }



}