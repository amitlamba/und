package com.und.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.netflix.discovery.converters.Auto
import com.und.model.jpa.AndroidTemplate
import com.und.model.jpa.ServiceProviderCredentials
import com.und.model.jpa.WebAction
import com.und.model.jpa.WebPushTemplate
import com.und.model.mongo.*
import com.und.repository.jpa.AndroidRepository
import com.und.repository.jpa.WebPushRepository
import com.und.repository.mongo.FcmCustomRepository
import com.und.repository.mongo.FcmRepository
import freemarker.cache.StringTemplateLoader
import com.und.model.utils.FcmMessage as UtilFcmMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils

@Service
class FcmHelperService {

    @Autowired
    private lateinit var service: ServiceProviderCredentialsService
    @Autowired
    private lateinit var repository: FcmRepository

    @Autowired
    private lateinit var templateContentCreationService: TemplateContentCreationService

    @Autowired
    private lateinit var objectMapper: ObjectMapper
    @Autowired
    private lateinit var androidRepository: AndroidRepository
    @Autowired
    private lateinit var webpushRepository: WebPushRepository

    fun getCredentials(clientId: Long): ServiceProviderCredentials? {
        var credential = service.findActiveAndroidServiceProvider(clientId)
        if (credential.id != null) return credential else return null
    }

    fun buildFcmAndroidMessage(message: UtilFcmMessage): LegacyFcmMessage {

        var template = fetchAndroidTemplate(message.clientId, message.templateId)
        var fcmMessage: LegacyFcmMessage = LegacyFcmMessage()

        var model = message.data
        message.eventUser?.let {
            model["user"] = it
        }
        var body = updateTemplateBody(template, model)

        var data = HashMap<String, String>()

        data.put("title", template.title)

        data.put("body", body)
        if (!template.channelId.isNullOrBlank()) data.put("channel_id",template.channelId!!)
        if (!template.channelName.isNullOrBlank()) data.put("channel_name", template.channelName!!)
        if (!template.imageUrl.isNullOrBlank()) data.put("big_pic", template.imageUrl!!)
        if (!template.largeIconUrl.isNullOrBlank()) data.put("lg_icon", template.largeIconUrl!!)
        if (!template.deepLink.isNullOrBlank()) data.put("deepLink", template.deepLink!!)
        if (template.actionGroup != null) data.put("actions", objectMapper.writeValueAsString(template.actionGroup))
        if (!template.sound.isNullOrBlank()) data.put("sound", template.sound!!)
        if (template.badgeIcon != null) data.put("badge_icon", template.badgeIcon.toString())
        if (template.fromUserNDot != null) data.put("fromuserndot", template.fromUserNDot.toString())
        data.put("priority", template.priority.toString())

        var collapse_key: String? = null
        if (!template.collapse_key.isNullOrBlank()) collapse_key = template.collapse_key
        var timeToLive: Long? = null
        if (template.timeToLive != null) timeToLive = template.timeToLive
        var priority = Priority.valueOf(template.priority.toString())

        with(fcmMessage) {
            this.to = message.to
            this.collapse_key = collapse_key
            time_to_live = timeToLive
            this.data = data
            this.priority = priority
        }
        return fcmMessage
    }

    private fun updateTemplateBody(template: AndroidTemplate, model: MutableMap<String, Any>): String {
        var body = templateContentCreationService.getAndroidBody(template, model)
        return body
    }

    private fun updateWebTemplateBody(webtemplate:WebPushTemplate,model: MutableMap<String, Any>):String{
        return templateContentCreationService.getWebpushBody(webtemplate,model)
    }
    @Cacheable("androidTemplate", key = "'client_'+#clientId+'_template_'+#templateId")
    private fun fetchAndroidTemplate(clientId: Long, templateId: Long): AndroidTemplate {
        return androidRepository.findByClientIdAndId(clientId, templateId)
    }
    @Cacheable(value = "webpushTemplate",key = "'client_'+#message.clientId+'_template'+#message.templateId")
    private fun fetchWebpushTemplate(message:UtilFcmMessage):WebPushTemplate{
        return webpushRepository.findByClientIdAndId(message.clientId, message.templateId)
    }
    fun buildWebFcmMessage(message: UtilFcmMessage): LegacyFcmMessage {
        var template = fetchWebpushTemplate(message)
        var fcmMessage = LegacyFcmMessage()
        var data=HashMap<String,String>()
        var model=message.data
        message.eventUser?.let {
            model["user"] = it
        }
        var body=updateWebTemplateBody(template,model)

        data.put("title",template.title)
        data.put("body",template.body)
        if(!template.badgeUrl.isNullOrBlank()) data.put("badge",template.badgeUrl!!)
        if(!template.customDataPair.isNullOrBlank()) data.put("data",template.customDataPair!!)
        if(!template.link.isNullOrBlank()) data.put("click_action",template.link!!)
        if(template.requireInteraction !=null )data.put("requireInteraction",template.requireInteraction.toString())
        if (template.fromUserndot!=null) data.put("fromuserndot",template.fromUserndot.toString())
        if(!template.iconUrl.isNullOrBlank()) data.put("icon",template.iconUrl!!)
        if(!template.lang.isNullOrBlank()) data.put("lang",template.lang!!)
        if(!template.imageUrl.isNullOrBlank()) data.put("image",template.imageUrl!!)
        if(!template.tag.isNullOrBlank()) data.put("tag",template.tag!!)

        var actionGroup = template.actionGroup
        if (actionGroup != null) data.put("actions", objectMapper.writeValueAsString(buildWebNotificationaction(actionGroup)))

        var collapse_key: String? = null
        if (!template.collapse_key.isNullOrBlank()) collapse_key = template.collapse_key
        var timeToLive: Long? = null
        if (template.ttl != null) timeToLive = template.ttl
        var priority:String?= null
        if(!template.urgency.isNullOrBlank()) priority=template.urgency

        with(fcmMessage) {
            to = message.to
            this.data=data
            this.collapse_key=collapse_key
            this.priority=Priority.valueOf(priority?:"NORMAL")
            time_to_live=timeToLive
        }
        return fcmMessage
    }

    private fun buildWebNotificationaction(actions: List<WebAction>): List<WebPushNotificationAction> {
        var list = mutableListOf<WebPushNotificationAction>()
        actions.forEach {
            var obj = WebPushNotificationAction()
            obj.title = it.title
            obj.action = it.action
            if (it.iconUrl != null) obj.icon = it.iconUrl
            list.add(obj)
        }
        return list
    }

    fun saveInMongo(fcmMessage: UtilFcmMessage, status: FcmMessageStatus, mongoId: String, serviceProvider: String) {
        var analyticFcmMessage = AnalyticFcmMessage(
                id = mongoId,
                clientId = fcmMessage.clientId,
                templateId = fcmMessage.templateId,
                status = status,
                campaignId = fcmMessage.campaignId,
                userId = fcmMessage.userId,
                serviceProvider = serviceProvider
        )
        repository.saveAnalyticMessage(analyticFcmMessage, clientId = fcmMessage.clientId)
    }

    fun updateStatus(mongoId: String, status: FcmMessageStatus, clientId: Long) {
        repository.updateStatus(mongoId, status, clientId, null)
    }

    private fun parseStringToMap(jsonString: String): HashMap<String, String> {
        var hashMap = HashMap<String, String>()
        var jsonNode: JsonNode = objectMapper.readTree(jsonString)
        var entityMap = jsonNode.fields()
        entityMap.forEach {
            hashMap.put(it.key, it.value.textValue())
        }
        return hashMap
    }
}