package com.und.livesegment.model

import com.und.model.JobDetailProperties
import javax.validation.constraints.NotBlank

class LiveSegmentJobDetailProperties: JobDetailProperties() {

    @NotBlank
    lateinit var segmentId: String

    @NotBlank
    lateinit var clientId: String

    @NotBlank
    lateinit var startEventId: String

    @NotBlank
    lateinit var startEventName: String

    @NotBlank
    lateinit var startEventTime: String

    @NotBlank
    lateinit var userId: String

}