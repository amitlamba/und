package com.und.model

import javax.validation.constraints.NotBlank

class JobDetail {
    @NotBlank
    lateinit var jobName: String

    @NotBlank
    lateinit var jobGroupName: String

    var jobType: JobType = JobType.CAMPAIGN

    var properties: Map<String, String> = emptyMap()

    enum class JobType {
        CAMPAIGN, LIVESEGMENT
    }
}