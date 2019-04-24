package com.und.model

import java.util.*

class JobActionStatus {
    var status: Status = Status.OK
    var message: String = ""
    lateinit var jobAction:JobAction

    enum class Status {
        DUPLICATE,
        NOTFOUND,
        ERROR,
        OK,
        COMPLETED,
        AB_COMPLETED
    }
}

class JobAction(
        val clientId: String,
        val campaignId: String,
        val campaignName: String,
        val action: JobDescriptor.Action,
        val nextTimeStamp: Date?=null
)