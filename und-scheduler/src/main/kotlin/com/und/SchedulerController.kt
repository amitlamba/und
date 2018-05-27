package com.und


import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import com.und.service.JobService
import com.und.util.JobUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/schedule/v1")
class SchedulerController {

    @Autowired
    lateinit var jobService: JobService

    /**
     * POST /jobs
     * @param descriptor
     * @return
     */
    @PostMapping(path = ["/jobs"])
    fun createJob(@Valid @RequestBody descriptor: JobDescriptor): ResponseEntity<JobActionStatus> {
        val job = jobService.findJob(descriptor)
        val status = JobActionStatus()
        if (job.isPresent) {
            status.message = "Job doesn't exists "
            status.status = JobActionStatus.Status.NOTFOUND
            return ResponseEntity(status, NOT_ACCEPTABLE)
        }
        val jobStatus = jobService.createJob(descriptor)
        if (jobStatus.status != JobActionStatus.Status.OK) {
            return ResponseEntity(jobStatus, NOT_ACCEPTABLE)
        }
        return ResponseEntity(jobStatus, CREATED)
    }


    /**
     * GET /jobs/clientId/campaignId/campaignName
     *
     * @param clientId
     * @param campaignId
     * @param campaignName
     * @return
     */
    @GetMapping(path = ["/jobs/{clientId}/{campaignId}/{campaignName}"])
    fun findJob(@PathVariable clientId: String, @PathVariable campaignId: String, @PathVariable campaignName: String): ResponseEntity<JobDescriptor> {
        val group: String = JobUtil.getGroupName(clientId)
        val name: String = JobUtil.getJobName(campaignId, campaignName)
        return jobService.findJob(group, name).map { ResponseEntity.ok(it) }.orElse(ResponseEntity.notFound().build())
    }

    /**
     * PUT /jobs
     *
     * @param descriptor
     * @return
     */
    @PutMapping(path = ["/jobs/"])
    fun updateJob(@Valid @RequestBody descriptor: JobDescriptor): ResponseEntity<JobActionStatus> {

        val group: String = JobUtil.getGroupName(descriptor)
        val name: String = JobUtil.getJobName(descriptor)
        val job = jobService.findJob(group, name)

        if (!job.isPresent) {
            val status = JobActionStatus()
            status.message = "Job doesn't exists "
            status.status = JobActionStatus.Status.NOTFOUND
            return ResponseEntity(status, NOT_ACCEPTABLE)
        }
        val status = jobService.updateJob(group, name, descriptor)
        if (status.status != JobActionStatus.Status.OK) {
            return ResponseEntity(status, NOT_ACCEPTABLE)
        }
        return ResponseEntity(status, ACCEPTED)

    }

    /**
     * DELETE /jobs/clientId/campaignId/campaignName
     *
     * @param clientId
     * @param campaignId
     * @param campaignName
     * @return
     */
    @DeleteMapping(path = ["/jobs/delete/{clientId}/{campaignId}/{campaignName}"])
    fun deleteJob(@PathVariable clientId: String, @PathVariable campaignId: String, @PathVariable campaignName: String): ResponseEntity<JobActionStatus> {
        return performAction(clientId, campaignId, campaignName, jobService::deleteJob)
    }


    /**
     * PATCH /jobs/pause/clientId/campaignId/campaignName
     *
     * @param clientId
     * @param campaignId
     * @param campaignName
     * @return
     */
    @PatchMapping(path = ["/jobs/pause/{clientId}/{campaignId}/{campaignName}"])
    fun pauseJob(@PathVariable clientId: String, @PathVariable campaignId: String, @PathVariable campaignName: String): ResponseEntity<JobActionStatus> {
        return performAction(clientId, campaignId, campaignName, jobService::pauseJob)
    }

    /**
     * PATCH /jobs/resume/clientId/campaignId/campaignName
     *
     * @param clientId
     * @param campaignId
     * @param campaignName
     * @return
     */
    @PatchMapping(path = ["/jobs/resume/{clientId}/{campaignId}/{campaignName}"])
    fun resumeJob(@PathVariable clientId: String, @PathVariable campaignId: String, @PathVariable campaignName: String): ResponseEntity<JobActionStatus> {
        return performAction(clientId, campaignId, campaignName, jobService::resumeJob)
    }

    private fun performAction(clientId: String, campaignId: String, campaignName: String, action: (String, String) -> JobActionStatus): ResponseEntity<JobActionStatus> {
        val group: String = JobUtil.getGroupName(clientId)
        val name: String = JobUtil.getJobName(campaignId, campaignName)
        val job = jobService.findJob(group, name)
        if (!job.isPresent) {
            val status = JobActionStatus()
            status.message = "Job doesn't exists "
            status.status = JobActionStatus.Status.NOTFOUND
            return ResponseEntity(status, NOT_ACCEPTABLE)
        }
        val status = action(group, name)
        if (status.status != JobActionStatus.Status.OK) {
            return ResponseEntity(status, NOT_ACCEPTABLE)
        }
        return ResponseEntity(status, ACCEPTED)
    }


}
