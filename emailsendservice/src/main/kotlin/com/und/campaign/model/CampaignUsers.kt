package com.und.campaign.model

import com.und.sms.listner.GroupStatus
import org.bson.Document
import java.time.LocalDateTime
import javax.persistence.*

@org.springframework.data.mongodb.core.mapping.Document("campaign_users")
class CampaignUsers{
    val campaignId:Long
    val clientId:Long
    @org.springframework.data.annotation.Id
    val executionId:String
    val isAbType:Boolean
    val usersPartOfAbTest:Boolean
    val segmentId:Long
    val templateId:Long
    val groupId:Long
    var groupStatus: GroupStatus = GroupStatus.UNDELIVERED
    var deliveryTime: LocalDateTime?=null
    var users:List<Document> = emptyList()

    constructor(campaignId: Long,clientId: Long,executionId:String,segmentId: Long,groupId:Long,templateId:Long,users: List<Document>,usersPartOfAbTest:Boolean = false,isAbType:Boolean = false){
        this.campaignId = campaignId
        this.templateId = templateId
        this.clientId= clientId
        this.executionId = executionId
        this.groupId = groupId
        this.segmentId = segmentId
        this.users = users
        this.usersPartOfAbTest = usersPartOfAbTest
        this.isAbType = isAbType
    }
    constructor(campaignId: Long, clientId: Long, executionId:String, templateId: Long,segmentId: Long, groupId:Long, groupStatus: GroupStatus,
                deliveryTime: LocalDateTime, users: List<Document>,partOfAbTest: Boolean=false): this(campaignId,clientId,executionId,segmentId,groupId,templateId,users,partOfAbTest){
        this.groupStatus = groupStatus
        this.deliveryTime = deliveryTime
    }
}

enum class CampaignUserStatus{
    DELIVERED,
    UNDELIVERED
}

@Entity
@Table(name = "campaignTriggerInfo")
class CampaignTriggerInfo{
    @Id
    @Column(name = "campaign_id")
    var campaignId:Long?=null
    @Column(name = "client_id")
    var clientId:Long?=null
    @Column(name = "error")
    var error:Boolean = false
    @OneToMany(cascade = [CascadeType.ALL],fetch = FetchType.EAGER)
    @JoinColumn(name = "campaign_id")
    var executionStatus:List<ExecutionStatus> = emptyList()

    override fun toString(): String {
        return "campaignId $campaignId clientId $clientId error $error execution status ${executionStatus.toTypedArray()}"
    }
}

@Entity
@Table(name = "executionStatus")
class ExecutionStatus{
    @Id
    @Column(name = "execution_id")
    lateinit var executionId:String
    @Column(name = "execution_time")
    lateinit var executionTime: LocalDateTime

    override fun toString(): String {
        return "execution id $executionId execution time $executionTime"
    }
}