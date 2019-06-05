package com.und.model.mongo

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import javax.persistence.Id

@Document
class EventUserRecord {

    @Id
    var id:String?=null
    var clientId:Long?=null
    var campaignId:Long?=null
    var usersId:List<ObjectId> = emptyList()
}

