package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.jpa.WebAction
import com.und.web.model.WebAction as Action
import com.und.model.jpa.WebPushTemplate
import com.und.web.model.WebPushTemplate as WebTemplate
import com.und.repository.jpa.WebPushRepository
import com.und.security.utils.AuthenticationUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

@Component
class WebPushServiceImp:WebPushService {

    @Autowired
    private lateinit var webPushRepository:WebPushRepository
    @Autowired
    private lateinit var objectmapper:ObjectMapper

    override fun saveTemplate(template: WebTemplate): WebPushTemplate? {
        var jpaWebPushTemplate=buildJpaWebPushTemplate(template)
        return webPushRepository.save(jpaWebPushTemplate)
    }

    override fun getTemplate(id: Long): WebPushTemplate? {
        var clientId = AuthenticationUtils.clientID
        if (clientId != null)
            return webPushRepository.findByClientIdAndId(clientId, id)
        else
            throw AccessDeniedException("")
    }

    override fun getAllTemplate(): List<WebPushTemplate> {
        var clientId = AuthenticationUtils.clientID
        if (clientId != null)
            return webPushRepository.findByClientId(clientId)
        else
            return emptyList() //throw exception
    }

    override fun isTemplateExists(clientId: Long, templateName:String): Boolean {
        var result=webPushRepository.findByClientIdAndName(clientId,templateName)
        return !result.isEmpty()
    }

    private fun buildJpaWebPushTemplate(template:WebTemplate):WebPushTemplate{
        var jpaTemplate=WebPushTemplate()
        with(jpaTemplate){
            clientId=AuthenticationUtils.clientID
            appUserId=AuthenticationUtils.principal.id
            name=template.name
            title=template.title
            body=template.body
            lang=template.lang
            badgeUrl=template.badgeUrl
            iconUrl=template.iconUrl
            imageUrl=template.imageUrl
            tag=template.tag
            requireInteraction=template.requireInteraction
            if(template.actionGroup!=null) actionGroup=buildJpaWebAction(template.actionGroup!!)
            urgency=template.urgency
            ttl=template.ttl
            link=template.link
            customDataPair=objectmapper.writeValueAsString(template.customDataPair)
            creationTime=template.creationTime
            modifiedTime=template.modifiedTime
            fromUserndot=template.fromUserndot
        }
        return jpaTemplate
    }
    private fun buildJpaWebAction(actionGroup:List<Action>):List<WebAction>{
        var jpaWebAction= mutableListOf<WebAction>()

        actionGroup.forEach {
            var action=WebAction()
            action.action=it.action
            action.title=it.title
            action.iconUrl=it.iconUrl
            action.creationTime=it.creationTime
            action.modifiedTime=it.modifiedTime
            jpaWebAction.add(action)
        }
        return jpaWebAction
    }
}