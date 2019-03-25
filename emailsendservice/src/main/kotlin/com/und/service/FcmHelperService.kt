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

    fun getCredentials(clientId: Long,id:Long?,type:String): ServiceProviderCredentials? {
        var credential:ServiceProviderCredentials=ServiceProviderCredentials()
        when(type){
            "android"->credential = service.findActiveAndroidServiceProvider(clientId,id)
            "web" ->credential = service.findActiveWebServiceProvider(clientId,id)
            "ios" -> credential = service.findActiveIosServiceProvider(clientId,id)
        }
        if (credential.id != null) return credential else return null
    }

    fun buildFcmAndroidMessage(message: UtilFcmMessage): LegacyFcmMessage {

        var model = message.data
        message.eventUser?.let {
            model["user"] = it
        }

        val alreadyExistTemplate=message.androidTemplate
        var templateAndBody = if(alreadyExistTemplate==null) {
            val template=fetchAndroidTemplate(message.clientId, message.templateId)
            val body = updateTemplateBody(template, model)
            Pair(template,body)
        } else {
            Pair(alreadyExistTemplate,updateTestAndroidTemplateBody(alreadyExistTemplate.body,model))
        }
        var fcmMessage: LegacyFcmMessage = LegacyFcmMessage()


        val template=templateAndBody.first
        val body=templateAndBody.second

        var data = HashMap<String, String>()

        data.put("title", template.title)
        data.put("body", body)

        template.channelId?.let {
            if(it.isNotBlank()){
                data.put("channelId",it)
            }
        }
        template.channelName?.let {
            if(it.isNotBlank()){
                data.put("channel_name",it)
            }
        }
        template.imageUrl?.let {
            if(it.isNotBlank()){
                data.put("big_pic",it)
            }
        }
        template.largeIconUrl?.let {
            if(it.isNotBlank()){
                data.put("lg_icon",it)
            }
        }
        template.deepLink?.let {
            if(it.isNotBlank()){
                data.put("deepLink",it)
            }
        }
        template.sound?.let {
            if(it.isNotBlank()){
                data.put("sound",it)
            }
        }
        if (template.actionGroup != null) data.put("actions", objectMapper.writeValueAsString(template.actionGroup))
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
    private fun updateTestAndroidTemplateBody(body: String, model: MutableMap<String, Any>): String {
        var body = templateContentCreationService.getTestAndroidBody(body, model)
        return body
    }

    private fun updateWebTemplateBody(webtemplate:WebPushTemplate,model: MutableMap<String, Any>):String{
        return templateContentCreationService.getWebpushBody(webtemplate,model)
    }

    private fun updateTestWebTemplateBody(body:String,model: MutableMap<String, Any>):String{
        return templateContentCreationService.getTestWebPushBody(body,model)
    }

    @Cacheable("androidTemplate", key = "'client_'+#clientId+'_template_'+#templateId")
    fun fetchAndroidTemplate(clientId: Long, templateId: Long): AndroidTemplate {
        return androidRepository.findByClientIdAndId(clientId, templateId)
    }
    @Cacheable(value = "webpushTemplate",key = "'client_'+#message.clientId+'_template'+#message.templateId")
    fun fetchWebpushTemplate(message:UtilFcmMessage):WebPushTemplate{
        return webpushRepository.findByClientIdAndId(message.clientId, message.templateId)
    }
    fun buildWebFcmMessage(message: UtilFcmMessage): LegacyFcmMessage {

        var data=HashMap<String,String>()
        var model=message.data
        message.eventUser?.let {
            model["user"] = it
        }

        val alreadyExistTemplate=message.webPushTemplate
        var templateAndBody = if(alreadyExistTemplate==null) {
            val template=fetchWebpushTemplate(message)
            val body = updateWebTemplateBody(template,model)
            Pair(template,body)
        } else {
            Pair(alreadyExistTemplate,updateTestWebTemplateBody(alreadyExistTemplate.body,model))
        }

        val template=templateAndBody.first
        val body=templateAndBody.second

        var fcmMessage = LegacyFcmMessage()

        data.put("title",template.title)
        data.put("body",body)
        var badgeUrl=template.badgeUrl
        if(badgeUrl!=null&& badgeUrl.isNotBlank()) data.put("badge",badgeUrl)
        var customDataPair=template.customDataPair
        if(customDataPair!=null&& customDataPair.isNotBlank()) data.put("data",customDataPair)
        var link=template.link
        if(link!=null && link.isNotBlank()) data.put("click_action",link)
        if(template.requireInteraction !=null )data.put("requireInteraction",template.requireInteraction.toString())
        if (template.fromUserndot!=null) data.put("fromuserndot",template.fromUserndot.toString())

        var iconUrl=template.iconUrl

        if(iconUrl!=null && iconUrl.isNotBlank()) data.put("icon",iconUrl)
        template.lang?.let {
            if(it.isNotBlank()){
                data.put("lang",it)
            }
        }
        template.imageUrl?.let {
            if(it.isNotBlank()){
                data.put("image",it)
            }
        }
        template.tag?.let {
            if(it.isNotBlank()){
                data.put("tag",it)
            }
        }

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

        fcmMessage.type
        var analyticFcmMessage = AnalyticFcmMessage(
                id = mongoId,
                clientId = fcmMessage.clientId,
                templateId = fcmMessage.templateId,
                status = status,
                type = fcmMessage.type,
                campaignId = fcmMessage.campaignId,
                userId = fcmMessage.userId,
                serviceProvider = serviceProvider,
                segmentId = fcmMessage.segmentId
        )
        repository.saveAnalyticMessage(analyticFcmMessage, clientId = fcmMessage.clientId)
    }

    fun updateStatus(mongoId: String, status: FcmMessageStatus, clientId: Long,type:String) {
        repository.updateStatus(mongoId, status, clientId, null,type)
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