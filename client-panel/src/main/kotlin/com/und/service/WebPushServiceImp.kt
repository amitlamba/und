package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.und.model.jpa.WebAction
import com.und.web.model.WebAction as Action
import com.und.model.jpa.WebPushTemplate
import com.und.web.model.WebPushTemplate as WebTemplate
import com.und.repository.jpa.WebPushRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

@Component
class WebPushServiceImp : WebPushService {

    @Autowired
    private lateinit var webPushRepository: WebPushRepository
    @Autowired
    private lateinit var objectmapper: ObjectMapper

    override fun saveTemplate(template: WebTemplate): WebTemplate {
        var jpaWebPushTemplate = buildJpaWebPushTemplate(template)
        return buildWebWebPushTempalte(webPushRepository.save(jpaWebPushTemplate))
    }

    override fun getTemplate(id: Long): WebTemplate {
        var clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
        var jpaWebTemplate = webPushRepository.findByClientIdAndId(clientId, id)
        return if (jpaWebTemplate != null) buildWebWebPushTempalte(jpaWebTemplate) else throw CustomException("Template with id $id not exists")
    }

    override fun getAllTemplate(): List<WebTemplate> {
        var clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
        var webTemplateList = mutableListOf<WebTemplate>()
        webPushRepository.findByClientId(clientId).forEach {
            webTemplateList.add(buildWebWebPushTempalte(it))
        }
        return webTemplateList
    }

    override fun isTemplateExists(clientId: Long, templateName: String): Boolean {
        var result = webPushRepository.findByClientIdAndName(clientId, templateName)
        return !result.isEmpty()
    }

    override fun findExistsTemplate(id: Long): List<WebPushTemplate> {
        var clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
        return webPushRepository.findTemplateExistsForThisId(clientId, id)
    }

    private fun buildJpaWebPushTemplate(template: WebTemplate): WebPushTemplate {
        var jpaTemplate = WebPushTemplate()
        with(jpaTemplate) {
            clientId = AuthenticationUtils.clientID
            appUserId = AuthenticationUtils.principal.id
            name = template.name
            title = template.title
            body = template.body
            lang = template.lang
            badgeUrl = template.badgeUrl
            iconUrl = template.iconUrl
            imageUrl = template.imageUrl
            tag = template.tag
            requireInteraction = template.requireInteraction
            if (template.actionGroup != null) actionGroup = buildJpaWebAction(template.actionGroup!!)
            urgency = template.urgency
            ttl = template.ttl
            link = template.link
            if (template.customDataPair != null) customDataPair = objectmapper.writeValueAsString(template.customDataPair)
            creationTime = template.creationTime
            modifiedTime = template.modifiedTime
            fromUserndot = template.fromUserndot
        }
        return jpaTemplate
    }

    private fun buildJpaWebAction(actionGroup: List<Action>): List<WebAction> {
        var jpaWebAction = mutableListOf<WebAction>()

        actionGroup.forEach {
            var action = WebAction()
            action.action = it.action
            action.title = it.title
            action.iconUrl = it.iconUrl
            action.creationTime = it.creationTime
            action.modifiedTime = it.modifiedTime
            jpaWebAction.add(action)
        }
        return jpaWebAction
    }

    private fun buildWebWebPushTempalte(template: WebPushTemplate): WebTemplate {
        var webTemplate: WebTemplate = WebTemplate()
        with(webTemplate) {
            id = template.id
            name = template.name
            title = template.title
            body = template.body
            lang = template.lang
            badgeUrl = template.badgeUrl
            iconUrl = template.iconUrl
            imageUrl = template.imageUrl
            tag = template.tag
            requireInteraction = template.requireInteraction
            if (template.actionGroup != null) {
                var actions = mutableListOf<Action>()
                template.actionGroup!!.forEach {
                    actions.add(buildWebAction(it))
                }
                actionGroup = actions
            }
            urgency = template.urgency
            ttl = template.ttl
            link = template.link
            if (template.customDataPair != null) customDataPair = objectmapper.readValue(template.customDataPair!!)
            creationTime = template.creationTime
            modifiedTime = template.modifiedTime
            fromUserndot = template.fromUserndot
        }
        return webTemplate
    }

    private fun buildWebAction(webAction: WebAction): Action {
        var waction = Action()
        with(waction) {
            action = webAction.action
            title = webAction.title
            iconUrl = webAction.iconUrl
            creationTime = webAction.creationTime
            modifiedTime = webAction.modifiedTime
        }
        return waction
    }
}