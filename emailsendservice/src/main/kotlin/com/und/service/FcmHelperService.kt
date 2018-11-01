package com.und.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.discovery.converters.Auto
import com.und.model.jpa.AndroidTemplate
import com.und.model.jpa.ServiceProviderCredentials
import com.und.model.jpa.WebAction
import com.und.model.mongo.*
import com.und.repository.jpa.AndroidRepository
import com.und.repository.jpa.WebPushRepository
import com.und.model.utils.FcmMessage as UtilFcmMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FcmHelperService {

    @Autowired
    private lateinit var service: ServiceProviderCredentialsService

    @Autowired
    private lateinit var objectMapper:ObjectMapper
    @Autowired
    private lateinit var androidRepository:AndroidRepository
    @Autowired
    private lateinit var webpushRepository:WebPushRepository

    fun getCredentials(clientId:Long): ServiceProviderCredentials? {
        var credential=service.findActiveNotificationServiceProvider(clientId)
        if(credential.id!=null) return credential else return null
    }

    fun buildFcmMessage(message:UtilFcmMessage):FcmMessage{
        var template=androidRepository.findByClientIdAndId(message.clientId,message.templateId)
        var fcmMessage: FcmMessage= FcmMessage()
        var notification=AndroidNotification()
        notification.title=template.title
        notification.body=template.body

        var data=HashMap<String,String>()
        if(template.channelId!=null) data.put("channel_id",template.channelId!!)
        if(template.channelName!=null) data.put("cname",template.channelName!!)
        if(template.imageUrl!=null) data.put("big_pic",template.imageUrl!!)
        if (template.largeIconUrl!=null) data.put("lg_icon",template.largeIconUrl!!)
        if(template.deepLink!=null) data.put("deep_link",template.deepLink!!)
        //ToDO dont put whole action group because in action group there are some irrevelant field like creation time
        if(template.actionGroup!=null) data.put("actions",objectMapper.writeValueAsString(template.actionGroup))
        if (template.sound!=null) data.put("sound",template.sound!!)
        //TODO check here enum is convert rightly
        if (template.badgeIcon!=null) data.put("badge_icon",template.badgeIcon.toString())
        if (template.fromUserNDot!=null) data.put("fromuserndot",template.fromUserNDot.toString())

        var android=AndroidConfig()
        if (template.collapse_key!=null) android.collapse_key=template.collapse_key
        if (template.timeToLive!=null) android.ttl=template.timeToLive
        android.notification=notification
        android.data=data
        //TODO check here
        android.priority= Priority.valueOf(template.priority.toString())

        with(fcmMessage){
            this.to=message.to
            this.android=android
        }
        return fcmMessage
    }
    fun buildWebFcmMessage(message:UtilFcmMessage):FcmMessage{
        var template=webpushRepository.findByClientIdAndId(message.clientId,message.templateId)
        var fcmMessage=FcmMessage()
        var webPushConfig=WebPushConfig()
        var headers=WebPushHeaders()
        headers.TTL=template.ttl
        if (template.urgency!=null) headers.Urgency=UrgencyOption.valueOf(template.urgency!!)
        webPushConfig.headers=headers
        var fcmOptions=WebPushFcmOptions()
        fcmOptions.link=template.link
        webPushConfig.fcm_options=fcmOptions
        var data=HashMap<String,String>()
        if(template.customDataPair!=null) data=parseStringToMap(template.customDataPair!!)
        webPushConfig.data=data
        var notification=WebPushNotification()
        notification.title=template.title
        notification.body=template.body
        notification.badge=template.badgeUrl
        notification.icon=template.iconUrl
        notification.image=template.imageUrl
        notification.lang=template.lang
        notification.requireInteraction=template.requireInteraction
        if (template.actionGroup!=null) notification.actions=buildWebNotificationaction(template.actionGroup!!)
        webPushConfig.notification=notification

        with(fcmMessage){
            to=message.to
            webpush=webPushConfig
        }
        return fcmMessage
    }

    private fun buildWebNotificationaction(actions:List<WebAction>):List<WebPushNotificationAction>{
        var list= mutableListOf<WebPushNotificationAction>()
        actions.forEach {
            var obj=WebPushNotificationAction()
            obj.title=it.title
            obj.action=it.action!!
            if (it.iconUrl!=null) obj.icon=it.iconUrl
            list.add(obj)
        }
        return list
    }
    fun saveInMongo(fcmMessage: FcmMessage){
        //save it to mongo
        //create status created
    }
    private fun parseStringToMap(jsonString: String): HashMap<String, String> {
        var hashMap = HashMap<String, String>()
        var jsonNode: JsonNode = objectMapper.readTree(jsonString)
        var entityMap = jsonNode.fields()
        entityMap.forEach {
            hashMap.put(it.key, it.value.toString())
        }
        return hashMap
    }
}