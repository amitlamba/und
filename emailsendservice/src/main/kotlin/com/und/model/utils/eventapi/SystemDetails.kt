package com.und.eventapi.model

data class SystemDetails(
        var OS: String = "",
        var browser: String = "",
        var browserVersion: String = "",
        var deviceType: String = "", //mobile, tablet, laptop etc
        var appId: String = "",
        var agentString:String = ""

)