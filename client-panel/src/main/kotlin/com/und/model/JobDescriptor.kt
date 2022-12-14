package com.und.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZoneId
import javax.validation.constraints.NotBlank

class JobDescriptor {

    @NotBlank
    lateinit var clientId: String

    @NotBlank
    lateinit var jobDetail: JobDetail

    @JsonProperty("triggers")
    var triggerDescriptors: List<TriggerDescriptor> = arrayListOf()

    var action:Action = Action.NOTHING

    var timeZoneId: ZoneId = ZoneId.of("UTC")

    enum class Action {
        PAUSE,CREATE,RESUME,DELETE,STOP,NOTHING,COMPLETED, FORCE_PAUSE,AB_COMPLETED
    }
}