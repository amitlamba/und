package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.jpa.Action
import com.und.web.model.Action as WebAndroidAction
import com.und.model.jpa.AndroidTemplate
import com.und.model.jpa.BadgeIconType
import com.und.model.jpa.Priority
import com.und.repository.jpa.AndroidActionRepository
import com.und.repository.jpa.AndroidRepository
import com.und.security.utils.AuthenticationUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import com.und.web.model.AndroidTemplate as WebAndroidTemplate

@Component
class AndroidServiceImp:AndroidService {

    @Autowired
    private lateinit var objectMapper: ObjectMapper
    @Autowired
    private lateinit var androidRepository: AndroidRepository
    @Autowired
    private lateinit var androidActionRepository:AndroidActionRepository

    override fun save(template: com.und.web.model.AndroidTemplate): AndroidTemplate {
        var jpaAndroidTemplate=buildJpaAndroidTemplate(template)
        jpaAndroidTemplate.addActionGroups(jpaAndroidTemplate.actionGroup)
        return androidRepository.save(jpaAndroidTemplate)
    }

    override fun getAllAndroidTemplate(clientId: Long): List<AndroidTemplate> {
            return androidRepository.findByClientId(clientId)
    }

    override fun getAndroidTemplateById(clientId: Long, id: Long): AndroidTemplate {
        return androidRepository.findByClientIdAndId(clientId,id)
    }

    override fun getAllAndroidAction(clientId: Long): List<Action> {
        return androidActionRepository.findByClientId(clientId)
    }

    private fun buildJpaAndroidTemplate(template: WebAndroidTemplate): AndroidTemplate {
        var androidTemplate=AndroidTemplate()
        with(androidTemplate){
            name=template.name
            clientId=AuthenticationUtils.clientID
            appuserId=AuthenticationUtils.principal.id
            title=template.title
            body=template.body
            channelId=template.channelId
            channelName=template.channelName
            imageUrl = template.imageUrl
            largeIconUrl = template.largeIconUrl
            deepLink = template.deepLink
            if (template.actionGroup != null) {
                var jpaAndroidAction = buildJpaAndroidAction(template.actionGroup!!)
                actionGroup = jpaAndroidAction
            }
            sound = template.sound
            badgeIcon=BadgeIconType.valueOf("${template.badgeIcon}")
            collapse_key=template.collapse_key
            priority=Priority.valueOf("${template.priority}")
            timeToLive=template.timeToLive
            fromUserNDot=template.fromUserNDot
            customKeyValuePair=objectMapper.writeValueAsString(template.customKeyValuePair)
            creationTime=template.creationTime

        }
        return androidTemplate
    }

    private fun buildJpaAndroidAction(actionGroup:List<WebAndroidAction>):List<Action>{
        var jpaAction= mutableListOf<Action>()
        actionGroup.forEach {
            var action=Action()
            action.actionId=it.actionId
            action.label=it.label
            action.deepLink=it.deepLink
            action.icon=it.icon
            action.autoCancel=it.autoCancel
            action.creationTime=it.creationTime
            jpaAction.add(action)
        }
        return jpaAction
    }
}