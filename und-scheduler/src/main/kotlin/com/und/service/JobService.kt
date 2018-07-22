package com.und.service

import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import java.util.*

/**
 * An object that defines the contract for defining dynamic jobs
 *
 * @author Shiv Pratap
 * @since February 2018
 */
interface JobService {

    /**
     * Create and schedule a job by abstracting the Job and Triggers in the
     * JobDescriptor. name and group for this job are determined by campaign id,
     * client id and campaign name uniquely @see com.und.util.JobUtil
     * which uniquely identifies the job. If you specify values that is not
     * unique, the scheduler will silently ignore the job
     *
     * @param descriptor
     * the payload containing the Job and its associated Trigger(s).
     * The name and group uniquely identifies the job.
     * @return JobActionStatus this contains the status of the job creation request
     * newly created job
     */
    fun createJob(descriptor: JobDescriptor): JobActionStatus

    /**
     * Searches for a Job identified by the given `JobKey`
     *
     * @param group
     * the group a job belongs to
     * @param name
     * the name of the dynamically scheduled job
     * @return the jobDescriptor if found or an empty Optional
     */
    fun findJob(group: String, name: String): Optional<JobDescriptor>

    /**
     * Searches for a Job identified by the given `JobKey`
     *
     * @param jobDescriptor
     * @return the list of jobDescriptor if found or an empty list
     */
    fun findJob(jobDescriptor: JobDescriptor):  List<JobDescriptor>


    /**
     * Updates the Job that matches the given `JobKey` with new
     * information
     *
     * @param group
     * the group a job belongs to
     * @param name
     * the name of the dynamically scheduled job
     * @param descriptor
     * the payload containing the updates to the JobDetail
     */
    fun updateJob(group: String, name: String, descriptor: JobDescriptor):JobActionStatus

    /**
     * Deletes the Job identified by the given `JobKey`
     *
     * @param group
     * the group a job belongs to
     * @param name
     * the name of the dynamically scheduled job
     */
    fun deleteJob(group: String, name: String): JobActionStatus

    /**
     * Pauses the Job identified by the given `JobKey`
     *
     * @param group
     * the group a job belongs to
     * @param name
     * the name of the dynamically scheduled job
     */
    fun pauseJob(group: String, name: String):JobActionStatus


    /**
     * Resumes the Job identified by the given `JobKey`
     *
     * @param group
     * the group a job belongs to
     * @param name
     * the name of the dynamically scheduled job
     */
    fun resumeJob(group: String, name: String):JobActionStatus

    /**
     * Resumes the Jobs identified by the given `group`
     *
     * @param group
     * the group a job belongs to

     */
    fun resumeJobs(group: String):JobActionStatus

    /**
     * Deletes the Jobs identified by the given `group`
     *
     * @param group
     * the group a job belongs to
     */
    fun deleteJobs(group: String): JobActionStatus

    /**
     * Pauses the Jobs identified by the given `group`
     *
     * @param group
     * the group a job belongs to
     */
    fun pauseJobs(group: String):JobActionStatus


    /**
     * find next occurrences of the job in a group
     * the group name
     */
    fun jobGroupNextDate(groupName:String):List<Date>
}
