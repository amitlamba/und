package com.und.model.utils

data class FcmMessage(
        var clientId:Long,
        var templateId:Long,
        var to:String,
        var type:String
)