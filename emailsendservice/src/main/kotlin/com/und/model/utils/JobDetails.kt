package com.und.model.utils

import javax.validation.constraints.NotBlank


class JobDetail {

    @NotBlank
    lateinit var jobName: String

    @NotBlank
    lateinit var jobGroupName: String

    @NotBlank
    lateinit var properties: JobDetailProperties

    var jobType: JobType = JobType.CAMPAIGN

    enum class JobType {
        CAMPAIGN, LIVESEGMENT
    }
}


abstract class JobDetailProperties {

}