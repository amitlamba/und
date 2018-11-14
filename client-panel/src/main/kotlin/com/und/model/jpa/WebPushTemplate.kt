package com.und.model.jpa

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "notification_template_webpush")
class WebPushTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,generator = "webpush_template_id_seq")
    @SequenceGenerator(name = "webpush_template_id_seq",sequenceName = "webpush_template_id_seq",allocationSize = 1)
    var id:Long?=null
    @Column(name="client_id")
    var clientId:Long?=null
    @Column(name = "appuser_id")
    var appUserId:Long?=null
    @NotNull
    @Column(name="name")
    lateinit var name:String
    @NotNull
    @Column(name = "title")
    lateinit var title:String
    @NotNull
    @Column(name = "body")
    lateinit var body:String
    @Column(name="language")
    var lang:String?=null
    @Column(name = "badge_url")
    var badgeUrl:String?=null      //url of badge icon
    @Column(name = "icon_url")
    var iconUrl:String?=null       //url if icon
    @Column(name = "image_url")
    var imageUrl:String?=null      //url of image in notification
    @Column(name = "tag")
    var tag:String?=null        //used to group notification
    @Column(name = "require_interaction")
    var requireInteraction:Boolean=false
    @OneToMany(cascade=arrayOf(CascadeType.ALL))
    @JoinColumn(name="template_id")
    var actionGroup:List<WebAction>?=null
//    header field
    @Column(name = "urgency")
    var urgency:String?=null
    @Column(name="time_to_live")
    var ttl:Long?=null
//    fcm specific
    @Column(name = "deep_link")
    var link:String?=null
//    custom key value pair
    @Column(name = "custom_data_pair")
    var customDataPair:String?=null
    @field:CreationTimestamp
    @Column(name = "creation_date")
    lateinit var creationTime:LocalDateTime
    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var modifiedTime:LocalDateTime
    @Column(name = "from_userndot")
    var fromUserndot:Boolean=true
}

@Entity
@Table(name = "webpush_notification_action")
class WebAction{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,generator = "webpush_action_id_seq")
    @SequenceGenerator(name = "webpush_action_id_seq",sequenceName = "webpush_action_id_seq",allocationSize = 1)
    var id:Long?=null
    @Column(name = "action")
    lateinit var action:String  //action id unique used to determine which action is clicked
    @NotNull
    @Column(name = "title")
    lateinit var title:String
    @Column(name = "icon_url")
    var iconUrl:String?=null //url of icon
    @field:CreationTimestamp
    @Column(name = "creation_date")
    lateinit var creationTime:LocalDateTime
    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var modifiedTime:LocalDateTime
}