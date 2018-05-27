package com.und.service


import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import com.und.util.JobUtil
import com.und.util.loggerFor
import org.quartz.JobKey
import org.quartz.JobKey.jobKey
import org.quartz.Scheduler
import org.quartz.SchedulerException
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
        try {
            val jobDetail = scheduler.getJobDetail(jobKey(name, group))
            if (Objects.nonNull(jobDetail))
                return Optional.of(
                        JobDescriptor.buildDescriptor(jobDetail,
                                scheduler.getTriggersOfJob(jobKey(name, group))))
        } catch (e: SchedulerException) {
            logger.error("Could not find job with key - $group.$name due to error - ${e.localizedMessage}")
        }

        logger.warn("Could not find job with key - $group.$name")
        return Optional.empty()
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    override fun findJob(jobDescriptor: JobDescriptor): Optional<JobDescriptor> {
        val name = JobUtil.getJobName(jobDescriptor)
        val group = JobUtil.getGroupName(jobDescriptor)
        return findJob(group, name)

    }

    /**
     * {@inheritDoc}
     */
    abstract override fun updateJob(group: String, name: String, descriptor: JobDescriptor):JobActionStatus

    /**
     * {@inheritDoc}
     */
    override fun deleteJob(group: String, name: String):JobActionStatus = takeAction(group, name, "delete", { jobKey -> scheduler.deleteJob(jobKey) })

    /**
     * {@inheritDoc}
     */
    override fun pauseJob(group: String, name: String):JobActionStatus = takeAction(group, name, "pause", scheduler::pauseJob)

    /**
     * {@inheritDoc}
     */
    override fun resumeJob(group: String, name: String):JobActionStatus = takeAction(group, name, "resume", scheduler::resumeJob)


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


}
