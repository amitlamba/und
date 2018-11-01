package com.und.service

import com.und.model.jpa.WebPushTemplate
import com.und.web.model.WebPushTemplate as WebTemplate
import org.springframework.stereotype.Service

@Service
interface WebPushService {
    fun saveTemplate(template:WebTemplate):WebPushTemplate?
    fun getTemplate(id:Long):WebPushTemplate?
    fun getAllTemplate():List<WebPushTemplate>
    fun findExistsTemplate(id:Long):List<WebPushTemplate>
    fun isTemplateExists(clientId:Long,templateName:String):Boolean
}