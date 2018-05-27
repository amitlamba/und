package com.und.model.jpa.security

class EmailMessage(
        val from: String,
        val to: String,
        var subject: String = "",
        var body: String = ""

)