package com.und.service
import com.und.model.jpa.Action
import com.und.model.jpa.AndroidTemplate
import com.und.web.model.AndroidTemplate as WebAndroidTemplate
import org.springframework.stereotype.Service

@Service
interface AndroidService {
    fun save(template:WebAndroidTemplate): AndroidTemplate
    fun getAllAndroidTemplate(clientId:Long):List<AndroidTemplate>
    fun getAndroidTemplateById(clientId: Long,id:Long):AndroidTemplate
    fun getAllAndroidAction(clientId: Long):List<Action>
    fun getAndroidTemplatesById(clientId: Long,id: Long):List<AndroidTemplate>
}