package com.und.utils

import com.und.model.mongo.*
import com.und.model.mongo.SystemDetails
import com.und.model.mongo.Event as MongoEvent
import com.und.model.mongo.EventUser as MongoEventUser
import java.util.*
import java.util.regex.Pattern
import com.und.model.web.EventUser


fun MongoEvent.parseUserAgentString(data:String?):MongoEvent {
     val mongoEvent = this
    val agentString = data ?: ""
    var pattern = Pattern.compile("^(Mobile-Agent).*")
    var matcher = pattern.matcher(agentString)
    if (!matcher.matches() && agentString.isNotEmpty()) {
        mongoEvent.agentString = agentString
        val sysDetail = systemDetails(agentString)
        val system = System()
        mongoEvent.system = system
        with(system) {
            os = SystemDetails(name = sysDetail.OS ?: "", version = sysDetail.osVersion ?: "")
            if (sysDetail.browser != null) {
                browser = SystemDetails(sysDetail.browser!!, sysDetail.browserVersion ?: "")
            }
            application = SystemDetails(name = "", version = "")
            device = SystemDetails(name = sysDetail.deviceType
                    ?: "", version = sysDetail.deviceVersion ?: "")
        }
    }else{
        val system = System()
        mongoEvent.system = system
        var agent = agentString.split("/")
        with(system) {
            os = SystemDetails(name = agent[1], version = agent[2])
            browser = SystemDetails(name = agent[3], version = agent[4])
            device = SystemDetails(name = agent[5], version = agent[6])
            application = SystemDetails(name = agent[7], version = agent[8])
        }

        mongoEvent.system = system

        var appFileds = AppField()
        with(appFileds) {
            make = agent[9]
            model = agent[6]
            sdkversion = agent[10]
            appversion = agent[8]
            os = agent[1]
        }
        mongoEvent.appfield = appFileds
    }
     return mongoEvent
}


fun com.und.model.mongo.EventUser.copyNonNullMongo(eventUser: com.und.model.mongo.EventUser): com.und.model.mongo.EventUser {
    fun unchanged(new: String?, old: String?): String? = when {
        new == old -> old
        old == null -> new
        new == null -> old
        else -> new
    }

    val copyEventUser = com.und.model.mongo.EventUser()

    copyEventUser.id = id
    copyEventUser.additionalInfo.putAll(additionalInfo)
    copyEventUser.additionalInfo.putAll(eventUser.additionalInfo)
    copyEventUser.clientId = if (id == null) eventUser.clientId else clientId
    copyEventUser.creationTime = creationTime

    copyEventUser.identity = Identity()
    copyEventUser.identity.uid = identity.uid
    copyEventUser.identity.fbId = unchanged(eventUser.identity.fbId, identity.fbId)
    copyEventUser.identity.googleId = unchanged(eventUser.identity.googleId, identity.googleId)
    copyEventUser.identity.mobile = unchanged(eventUser.identity.mobile, identity.mobile)
    copyEventUser.identity.email = unchanged(eventUser.identity.email, identity.email)
//    copyEventUser.identity.undId = unchanged(eventUser.identity.undId, identity.undId)
    copyEventUser.identity.androidFcmToken=unchanged(eventUser.identity.androidFcmToken,identity.androidFcmToken)
    copyEventUser.identity.webFcmToken= addWebFcmToken(this,eventUser)
    copyEventUser.identity.iosFcmToken=unchanged(eventUser.identity.iosFcmToken,identity.iosFcmToken)

    copyEventUser.standardInfo = StandardInfo()
    copyEventUser.standardInfo.firstname = unchanged(eventUser.standardInfo.firstname, standardInfo.firstname)
    copyEventUser.standardInfo.lastname = unchanged(eventUser.standardInfo.lastname, standardInfo.lastname)
    copyEventUser.standardInfo.gender = unchanged(eventUser.standardInfo.gender, standardInfo.gender)
    copyEventUser.standardInfo.dob = if(standardInfo.dob==null && eventUser.standardInfo.dob!=null) eventUser.standardInfo.dob
    else if (standardInfo.dob!=null) standardInfo.dob else null

    copyEventUser.standardInfo.country = unchanged(eventUser.standardInfo.country, standardInfo.country)
    copyEventUser.standardInfo.city = unchanged(eventUser.standardInfo.city, standardInfo.city)
    copyEventUser.standardInfo.state= unchanged(eventUser.standardInfo.state,standardInfo.state)
    copyEventUser.standardInfo.address = unchanged(eventUser.standardInfo.address, standardInfo.address)
    copyEventUser.standardInfo.countryCode = unchanged(eventUser.standardInfo.countryCode, standardInfo.countryCode)
    copyEventUser.communication=getCommunication(this,eventUser)
    return copyEventUser
}
/*
* here we adding the webFcmToken into list of webFcmToken
* */
private fun addWebFcmToken(existingEventUser:MongoEventUser,newEventUser:EventUser):ArrayList<String>?{
    var webtoken=existingEventUser.identity.webFcmToken
    var newwebtoken=newEventUser.webFcmToken
    if(webtoken==null && newwebtoken!=null){
        webtoken = ArrayList()
    }
    newwebtoken?.let {
        if(webtoken!=null && webtoken.isNotEmpty()){
            var result=webtoken.find { it.equals(newwebtoken) }
            if(result==null) {webtoken.add(newwebtoken)}else{
                return webtoken
            }
        }else{
            webtoken?.add(it)
        }
    }
    return webtoken
}
private fun addWebFcmToken(existingEventUser:MongoEventUser,newEventUser:MongoEventUser):ArrayList<String>?{
    var webtoken=existingEventUser.identity.webFcmToken
    var newwebtoken=newEventUser.identity.webFcmToken
    if(webtoken==null && newwebtoken!=null){
        webtoken = newwebtoken
    }else if(webtoken!=null && newwebtoken!=null){
        newwebtoken.forEach {
            val token=it
            var result=webtoken.find {
                it.equals(token)
            }
            if(result==null){
                webtoken.add(token)
            }
        }
    }
    return webtoken
}

private fun getCommunication(existingEventUser: MongoEventUser,newEventUser: EventUser):Communication{
    var communication = Communication()
    var existingCommunication=existingEventUser.communication
    if(existingCommunication==null){
        newEventUser.email?.let {
            communication.email= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.mobile?.let {
            communication.mobile= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.androidFcmToken?.let {
            communication.android= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.webFcmToken?.let {
            communication.webpush= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.iosFcmToken?.let {
            communication.ios= CommunicationDetails(value = it,dnd = false)
        }
    }else{
        newEventUser.email?.let {
            communication.email= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.mobile?.let {
            communication.mobile= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.androidFcmToken?.let {
            communication.android= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.webFcmToken?.let {
            communication.webpush= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.iosFcmToken?.let {
            communication.ios= CommunicationDetails(value = it,dnd = false)
        }
    }
    return communication
}

private fun getCommunication(existingEventUser: MongoEventUser,newEventUser: MongoEventUser):Communication{
    var communication = Communication()
    var existingCommunication=existingEventUser.communication
    if(existingCommunication==null){
        newEventUser.identity.email?.let {
            communication.email= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.identity.mobile?.let {
            communication.mobile= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.identity.androidFcmToken?.let {
            communication.android= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.identity.webFcmToken?.let {
            communication.webpush= CommunicationDetails(value = it.last(),dnd = false)
        }
        newEventUser.identity.iosFcmToken?.let {
            communication.ios= CommunicationDetails(value = it,dnd = false)
        }
    }else{
        newEventUser.identity.email?.let {
            communication.email= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.identity.mobile?.let {
            communication.mobile= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.identity.androidFcmToken?.let {
            communication.android= CommunicationDetails(value = it,dnd = false)
        }
        newEventUser.identity.webFcmToken?.let {
            communication.webpush= CommunicationDetails(value = it.last(),dnd = false)
        }
        newEventUser.identity.iosFcmToken?.let {
            communication.ios= CommunicationDetails(value = it,dnd = false)
        }
    }
    return communication
}

fun com.und.model.mongo.EventUser.copyNonNull(eventUser: EventUser): com.und.model.mongo.EventUser {
    fun unchanged(new: String?, old: String?): String? = when {
        new == old -> old
        old == null -> new
        new == null -> old
        else -> new
    }

    val copyEventUser = com.und.model.mongo.EventUser()

    copyEventUser.id = id?:eventUser.identity.userId
    copyEventUser.additionalInfo.putAll(additionalInfo)
    copyEventUser.additionalInfo.putAll(eventUser.additionalInfo)
    copyEventUser.clientId = if (id == null) eventUser.clientId else clientId
    copyEventUser.creationTime = creationTime


    copyEventUser.identity = Identity()
    copyEventUser.identity.identified=if(eventUser.identity.idf==1) true else false
    copyEventUser.identity.uid = unchanged(eventUser.uid, identity.uid)
    copyEventUser.identity.fbId = unchanged(eventUser.fbId, identity.fbId)
    copyEventUser.identity.googleId = unchanged(eventUser.googleId, identity.googleId)
    copyEventUser.identity.mobile = unchanged(eventUser.mobile, identity.mobile)
    copyEventUser.identity.email = unchanged(eventUser.email, identity.email)
    copyEventUser.identity.undId = unchanged(eventUser.undId, identity.undId)
    copyEventUser.identity.androidFcmToken=unchanged(eventUser.androidFcmToken,identity.androidFcmToken)
    copyEventUser.identity.webFcmToken= addWebFcmToken(this,eventUser)
    copyEventUser.identity.iosFcmToken=unchanged(eventUser.iosFcmToken,identity.iosFcmToken)

    copyEventUser.standardInfo = StandardInfo()
    copyEventUser.standardInfo.firstname = unchanged(eventUser.firstName, standardInfo.firstname)
    copyEventUser.standardInfo.lastname = unchanged(eventUser.lastName, standardInfo.lastname)
    copyEventUser.standardInfo.gender = unchanged(eventUser.gender, standardInfo.gender)
//    copyEventUser.standardInfo.dob = unchanged(eventUser.dob, standardInfo.dob)
    if(eventUser.dob!=null) {
        var date= java.time.LocalDate.parse(eventUser.dob)
        copyEventUser.standardInfo.dob=date
        copyEventUser.standardInfo.age=date.year
    }
    else {
        copyEventUser.standardInfo.dob= this.standardInfo.dob
        copyEventUser.standardInfo.age= this.standardInfo.age
    }
    copyEventUser.standardInfo.country = unchanged(eventUser.country, standardInfo.country)
    copyEventUser.standardInfo.city = unchanged(eventUser.city, standardInfo.city)
    copyEventUser.standardInfo.state= unchanged(eventUser.state,standardInfo.state)
    copyEventUser.standardInfo.address = unchanged(eventUser.address, standardInfo.address)
    copyEventUser.standardInfo.countryCode = unchanged(eventUser.countryCode, standardInfo.countryCode)
    copyEventUser.communication=getCommunication(this,eventUser)
    return copyEventUser
}