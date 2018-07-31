package com.und.service


import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import com.und.util.JobUtil
import com.und.util.loggerFor
import org.quartz.JobKey
import org.quartz.JobKey.jobKey
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.impl.matchers.StringMatcher
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Abstract implementation of JobService
 *
 * @author Shiv Pratap
 * @since February 2018
 */
abstract class AbstractJobService : JobService {

    companion object {
        protected val logger: Logger = loggerFor(AbstractJobService::class.java)
    }

    @Autowired
    protected lateinit var scheduler: Scheduler

    /**
     * {@inheritDoc}
     */
    abstract override fun createJob(descriptor: JobDescriptor): JobActionStatus

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    override fun findJob(group: String, name: String): Optional<JobDescriptor> {
        val jobKey = jobKey(name, group)
        return findJob(jobKey)
    }

    private fun findJob(jobKey: JobKey): Optional<JobDescriptor> {
        try {
            val jobDetail = scheduler.getJobDetail(jobKey)
            if (Objects.nonNull(jobDetail))
                return Optional.of(
                        JobDescriptor.buildDescriptor(jobDetail,
                                scheduler.getTriggersOfJob(jobKey)))
        } catch (e: SchedulerException) {
            logger.error("Could not find job with key - ${jobKey.group}.${jobKey.name} due to error - ${e.localizedMessage}")
        }

        logger.warn("Could not find job with key - ${jobKey.group}.${jobKey.name}")
        return Optional.empty()
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    override fun findJob(jobDescriptor: JobDescriptor): List<JobDescriptor> {
        val descriptors = mutableListOf<JobDescriptor>()
        val groupName = JobUtil.getGroupName(jobDescriptor)
        val jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(groupName))
        val jobs = jobKeys.asSequence()
                .mapNotNull { key -> findJob(key) }
                .filter { job -> job.isPresent }
                .map { job -> job.get() }.toList()
        descriptors.addAll(jobs)

        return descriptors

    }

    override  fun jobGroupNextDate(groupName:String):List<Date>{
        val jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(groupName)).sortedBy { key->key.name.split("-").last().toLong() }

        return jobKeys.filterNotNull()
                .flatMap { key ->  scheduler.getTriggersOfJob(key).filterNotNull() }.takeWhile { trigger->trigger.nextFireTime != null}.map{it.nextFireTime}.sortedByDescending { it }

    }


    /**
     * {@inheritDoc}
     */
    abstract override fun updateJob(group: String, name: String, descriptor: JobDescriptor): JobActionStatus

    /**
     * {@inheritDoc}
     */
    override fun deleteJob(group: String, name: String): JobActionStatus = takeAction(group, name, "delete", { jobKey -> scheduler.deleteJob(jobKey) })

    /**
     * {@inheritDoc}
     */
    override fun pauseJob(group: String, name: String): JobActionStatus = takeAction(group, name, "pause", scheduler::pauseJob)

    /**
     * {@inheritDoc}
     */
    override fun resumeJob(group: String, name: String): JobActionStatus = takeAction(group, name, "resume", scheduler::resumeJob)


    private fun takeAction(group: String, name: String, actionName: String, action: (JobKey) -> Unit): JobActionStatus {
        val status = JobActionStatus()
        try {
            action(jobKey(name, group))
            status.status = JobActionStatus.Status.OK
            logger.info("$actionName  for job with key - $group.$name")
        } catch (e: SchedulerException) {
            logger.error("Could not execute $actionName job with key - $group.$name due to error - ${e.localizedMessage}")
            status.status = JobActionStatus.Status.ERROR

        }
        return status

    }


    /**
     * {@inheritDoc}
     */
    override fun deleteJobs(group: String): JobActionStatus {
        val keys = scheduler.getJobKeys(GroupMatcher.groupEquals(group))

        val status = JobActionStatus()
        try {
            //val atcher = GroupMatcher.groupEquals(group)
            scheduler.deleteJobs(keys.toList())
            status.status = JobActionStatus.Status.OK
            logger.info("delete for jobs with group - $group")
        } catch (e: SchedulerException) {
            logger.error("Could not execute delete jobs with key - $group due to error - ${e.localizedMessage}")
            status.status = JobActionStatus.Status.ERROR

        }
        return status
    }


    /**
     * {@inheritDoc}
     */
    override fun pauseJobs(group: String): JobActionStatus = takeActionOnGroup(group, "pause", scheduler::pauseJobs)

    /**
     * {@inheritDoc}
     */
    override fun resumeJobs(group: String): JobActionStatus = takeActionOnGroup(group, "resume", scheduler::resumeJobs)

    private fun takeActionOnGroup(group: String, actionName: String, action: (GroupMatcher<JobKey>) -> Unit): JobActionStatus {
        val status = JobActionStatus()
        try {
            //val atcher = GroupMatcher.groupEquals(group)
            action(GroupMatcher.groupEquals(group))
            status.status = JobActionStatus.Status.OK
            logger.info("$actionName  for jobs with group - $group")
        } catch (e: SchedulerException) {
            logger.error("Could not execute $actionName jobs with key - $group due to error - ${e.localizedMessage}")
            status.status = JobActionStatus.Status.ERROR

        }
        return status

    }


}
