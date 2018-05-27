package com.und.model

import java.time.LocalDateTime
import javax.validation.constraints.NotNull

class RecurrenceMessage {

    @NotNull
    var campaignId: Long = 0

    @NotNull
    lateinit var startDateTime: LocalDateTime

    @NotNull
    lateinit var endDateTime: LocalDateTime

    @NotNull
    lateinit var cron: String

    @NotNull
    lateinit var action: String

    @NotNull
    val clientId: Long = -1

}