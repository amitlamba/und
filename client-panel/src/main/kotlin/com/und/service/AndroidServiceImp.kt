package com.und.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.und.model.jpa.Action
import com.und.web.model.Action as WebAndroidAction
import com.und.model.jpa.AndroidTemplate
import com.und.model.jpa.BadgeIconType
import com.und.model.jpa.Priority
import com.und.repository.jpa.AndroidActionRepository
import com.und.repository.jpa.AndroidRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.CustomException
import org.hibernate.exception.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import com.und.web.model.AndroidTemplate as WebAndroidTemplate

@Component
class AndroidServiceImp : AndroidService {

    @Autowired
    private lateinit var objectMapper: ObjectMapper
    @Autowired
    private lateinit var androidRepository: AndroidRepository
    @Autowired
    private lateinit var androidActionRepository: AndroidActionRepository

    override fun save(template: com.und.web.model.AndroidTemplate): WebAndroidTemplate {
        var jpaAndroidTemplate = buildJpaAndroidTemplate(template)
        jpaAndroidTemplate.addActionGroups(jpaAndroidTemplate.actionGroup)
        var persistedTemplate=AndroidTemplate()
        try{
            persistedTemplate=androidRepository.save(jpaAndroidTemplate)
        }catch(ex: ConstraintViolationException){
            throw CustomException("Template with this name already exists.")
        }catch(ex: DataIntegrityViolationException){
            throw CustomException("Template with this name already exists.")
        }
        return buildWebAndroidTemplate(persistedTemplate)
    }

    override fun getAllAndroidTemplate(clientId: Long): List<WebAndroidTemplate> {
        var persistedModel = androidRepository.findByClientId(clientId)
        var webAndroidTemplateList = mutableListOf<WebAndroidTemplate>()
        persistedModel.forEach {
            var webAndroidTemplate = buildWebAndroidTemplate(it)
            webAndroidTemplateList.add(webAndroidTemplate)
        }
        return webAndroidTemplateList
    }

    override fun getAndroidTemplateById(clientId: Long, id: Long): WebAndroidTemplate {
        var persistedAndroidTemplate = androidRepository.findByClientIdAndId(clientId, id)
        return if (persistedAndroidTemplate != null) buildWebAndroidTemplate(persistedAndroidTemplate) else throw CustomException("Template with id $id not exists")
    }

    override fun getAllAndroidAction(clientId: Long): List<WebAndroidAction> {
        var listOfJpaAndroidAction = androidActionRepository.findByClientId(clientId)
        var listOfWebAndroidAction = mutableListOf<WebAndroidAction>()
        listOfJpaAndroidAction.forEach {
            listOfWebAndroidAction.add(buildWebAndroidAction(it))
        }
        return listOfWebAndroidAction
    }

    override fun getAndroidTemplatesById(clientId: Long, id: Long): List<AndroidTemplate> {
        return androidRepository.isExistsByClientIdAndId(clientId, id)
    }

    override fun buildJpaAndroidTemplate(template: WebAndroidTemplate): AndroidTemplate {

        var androidTemplate = AndroidTemplate()
        with(androidTemplate) {
            name = template.name
            clientId = AuthenticationUtils.clientID
            appuserId = AuthenticationUtils.principal.id
            title = template.title
            body = template.body
            channelId = template.channelId
            channelName = template.channelName
            imageUrl = template.imageUrl
            largeIconUrl = template.largeIconUrl
            deepLink = template.deepLink
            var actionGroups=template.actionGroup
            if (actionGroups != null) {
                var jpaAndroidAction = buildJpaAndroidAction(actionGroups)
                actionGroup = jpaAndroidAction
            }
            sound = template.sound
            badgeIcon = BadgeIconType.valueOf("${template.badgeIcon}")
            collapse_key = template.collapse_key
            priority = Priority.valueOf("${template.priority}")
            timeToLive = template.timeToLive
            fromUserNDot = template.fromUserNDot
            if(template.customKeyValuePair!=null)
            customKeyValuePair = objectMapper.writeValueAsString(template.customKeyValuePair)
            creationTime = template.creationTime

        }
        return androidTemplate
    }

    private fun buildJpaAndroidAction(actionGroup: List<WebAndroidAction>): List<Action> {
        var jpaAction = mutableListOf<Action>()
        actionGroup.forEach {
            var action = Action()
            action.actionId = it.actionId
            action.label = it.label
            action.deepLink = it.deepLink
            action.icon = it.icon
            action.clientId=AuthenticationUtils.clientID
            action.autoCancel = it.autoCancel
            action.creationTime = it.creationTime
            jpaAction.add(action)
        }
        return jpaAction
    }

    private fun buildWebAndroidTemplate(template: AndroidTemplate): WebAndroidTemplate {
        var webAndroidTemplate = WebAndroidTemplate()
        with(webAndroidTemplate) {
            id = template.id
            name = template.name
            title = template.title
            body = template.body
            channelId = template.channelId
            channelName = template.channelName
            imageUrl = template.imageUrl
            largeIconUrl = template.largeIconUrl
            deepLink = template.deepLink
            var actionGroups=template.actionGroup
            if (actionGroups != null) {
                var list = mutableListOf<WebAndroidAction>()
                actionGroups.forEach {
                    list.add(buildWebAndroidAction(it))
                }
                actionGroup = list
            }
            sound = template.sound
            badgeIcon = com.und.web.model.BadgeIconType.valueOf("${template.badgeIcon}")
            collapse_key = template.collapse_key
            priority = com.und.web.model.Priority.valueOf("${template.priority}")
            timeToLive = template.timeToLive
            fromUserNDot = template.fromUserNDot
            var customPair=template.customKeyValuePair
            if (customPair != null) customKeyValuePair = parseStringToMap(customPair)
            creationTime = template.creationTime
        }
        return webAndroidTemplate
    }

    private fun buildWebAndroidAction(actions: Action): WebAndroidAction {
        var webAndroidAction = WebAndroidAction()
        with(webAndroidAction) {
            id = actions.id
            actionId = actions.actionId
            label=actions.label
            deepLink = actions.deepLink
            icon = actions.icon
            autoCancel = actions.autoCancel
        }
        return webAndroidAction
    }

    private fun parseStringToMap(jsonString: String): HashMap<String, String> {
//        var hashMap = HashMap<String, String>()
//        var jsonNode: JsonNode = objectMapper.readTree(jsonString)
//        var entityMap = jsonNode.fields()
//        entityMap.forEach {
//            hashMap.put(it.key, it.value.toString())
//        }
//        return hashMap
//        parseStringToMapByTypeReference(jsonString)
        return objectMapper.readValue(jsonString)
    }
}