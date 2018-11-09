package com.und.service

import com.und.model.jpa.WebPushTemplate
import com.und.web.model.WebPushTemplate as WebTemplate
import org.springframework.stereotype.Service

@Service
interface WebPushService {
    fun saveTemplate(template:WebTemplate):WebTemplate
    fun getTemplate(id:Long):WebTemplate
    fun getAllTemplate():List<WebTemplate>
    fun findExistsTemplate(id:Long):List<WebPushTemplate>
    fun isTemplateExists(clientId:Long,templateName:String):Boolean
}