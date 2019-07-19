package com.und.campaign.model

import com.und.sms.listner.GroupStatus
import org.bson.Document
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id

@org.springframework.data.mongodb.core.mapping.Document("campaign_users")
class CampaignUsers{
    val campaignId:Long
    val clientId:Long
    @org.springframework.data.annotation.Id
    val executionId:String
    val segmentId:Long
    val templateId:Long
    val groupId:Long
    var partOfAbTest:Boolean = false
    var groupStatus: GroupStatus = GroupStatus.UNDELIVERED
    var deliveryTime: LocalDateTime?=null
    var users:List<Document> = emptyList()

    constructor(campaignId: Long,clientId: Long,executionId:String,segmentId: Long,groupId:Long,templateId:Long,users: List<Document>,partOfAbTest:Boolean = false){
        this.campaignId = campaignId
        this.templateId = templateId
        this.clientId= clientId
        this.executionId = executionId
        this.groupId = groupId
        this.segmentId = segmentId
        this.users = users
        this.partOfAbTest = partOfAbTest
    }
    constructor(campaignId: Long, clientId: Long, executionId:String, templateId: Long,segmentId: Long, groupId:Long, groupStatus: GroupStatus,
                deliveryTime: LocalDateTime, users: List<Document>,partOfAbTest: Boolean=false): this(campaignId,clientId,executionId,segmentId,groupId,templateId,users,partOfAbTest){
        this.groupStatus = groupStatus
        this.deliveryTime = deliveryTime
    }
}


@Entity
class CampaignTriggerInfo(
    val clientId:Long,
    @Id
    val campaignId:Long,
    val executionStatus:List<ExecutionStatus>
    )

data  class ExecutionStatus(val executionId:String,val executionTime: LocalDateTime)