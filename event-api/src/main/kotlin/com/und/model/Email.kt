package com.und.model

import com.und.model.mongo.EmailStatus

data class EmailUpdate(
        var clientID: Long,
        var mongoEmailId: String,
        val emailStatus: EmailStatus,
        val eventId: String? = null
)

data class EmailRead(
        val clientID: Long,
        var mongoEmailId: String
)