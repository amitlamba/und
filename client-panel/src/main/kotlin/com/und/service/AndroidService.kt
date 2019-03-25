package com.und.service
import com.und.model.jpa.Action
import com.und.model.jpa.AndroidTemplate
import com.und.web.model.AndroidTemplate as WebAndroidTemplate
import org.springframework.stereotype.Service

@Service
interface AndroidService {
    fun save(template:WebAndroidTemplate): WebAndroidTemplate
    fun getAllAndroidTemplate(clientId:Long):List<WebAndroidTemplate>
    fun getAndroidTemplateById(clientId: Long,id:Long):WebAndroidTemplate
    fun getAllAndroidAction(clientId: Long):List<com.und.web.model.Action>
    fun getAndroidTemplatesById(clientId: Long,id: Long):List<AndroidTemplate>
    fun buildJpaAndroidTemplate(template: WebAndroidTemplate): AndroidTemplate
}