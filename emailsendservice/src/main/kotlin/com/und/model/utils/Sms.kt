package com.und.model.utils

import com.und.model.mongo.EventUser
import java.util.*

data class Sms(
    var clientID: Long,
    var fromSmsAddress: String?,
    var toSmsAddresses: String?,
    var smsBody: String?,
    var smsTemplateId: Long,
    var smsTemplateName: String?,
    var data: MutableMap<String, Any> = mutableMapOf(),
    var eventUser: EventUser? = null

)

//implement  hash method

{
    override fun equals(other: Any?): Boolean {
        if(other===this)return true
        if (javaClass != other?.javaClass) return false
        other as Sms

        if(clientID!=other.clientID)return false
        if(fromSmsAddress!=other.fromSmsAddress) return false
        if(!smsBody.equals(other.smsBody)) return false
        if(smsTemplateId!=other.smsTemplateId)return false
        if(smsTemplateName!=other.smsTemplateName) return false
        if(toSmsAddresses==other.toSmsAddresses)return false
        if (eventUser!=other.eventUser) return false
//        if (data != other.data) return false

        return true
    }
}