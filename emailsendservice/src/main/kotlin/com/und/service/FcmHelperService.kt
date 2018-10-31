package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.discovery.converters.Auto
import com.und.model.jpa.AndroidTemplate
import com.und.model.jpa.ServiceProviderCredentials
import com.und.model.mongo.AndroidConfig
import com.und.model.mongo.AndroidNotification
import com.und.model.mongo.FcmMessage
import com.und.model.mongo.Priority
import com.und.repository.jpa.AndroidRepository
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
        if(template.channelId!=null) data.put("cid",template.channelId!!)
        if(template.channelName!=null) data.put("cname",template.channelName!!)
        if(template.imageUrl!=null) data.put("imageUrl",template.imageUrl!!)
        if (template.largeIconUrl!=null) data.put("largeIconUrl",template.largeIconUrl!!)
        if(template.deepLink!=null) data.put("deep_link",template.deepLink!!)
        if(template.actionGroup!=null) data.put("actions",objectMapper.writeValueAsString(template.actionGroup))
        if (template.sound!=null) data.put("sound",template.sound!!)
        //TODO check here
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

    fun saveInMongo(fcmMessage: FcmMessage){
        //save it to mongo
        //create status created
    }
}