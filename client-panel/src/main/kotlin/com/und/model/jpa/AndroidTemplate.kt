package com.und.model.jpa

import com.fasterxml.jackson.annotation.*
import com.und.security.utils.AuthenticationUtils
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import kotlin.math.min

@Entity
@Table(name = "notification_template_android")
//@JsonIdentityInfo(
//        property = "id",generator = ObjectIdGenerators.PropertyGenerator::class
//                )
class AndroidTemplate{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,generator = "android_template_id_seq")
    @SequenceGenerator(name="android_template_id_seq",sequenceName = "android_template_id_seq" ,allocationSize = 1)
    @Column(name="id")
    var id:Long?=null
    @NotNull
    @Column(name="name")
    lateinit var name:String
    @Column(name = "client_id")
    var clientId:Long?=null
    @Column(name = "appuser_id")
    var appuserId:Long?=null
    @NotNull
//    @Size(min=8,max = 45)
    @Column(name = "title")
    lateinit var title:String
    @NotNull
    @Column(name = "body")
    lateinit var body:String
    @Column(name = "channel_id")
    var channelId:String?=null           //mandatory for api 28 sdk 26+
    @Column(name = "channel_name")
    var channelName:String?=null        //mandatory for api 28 sdk 26+
    @Column(name = "image_url")
    @Pattern(regexp = "^http.{0,1}://.*$")
    var imageUrl:String?=null
    @Column(name = "large_icon_url")
    @Pattern(regexp = "^http.{0,1}://.*$")
    var largeIconUrl:String?=null
    @Column(name = "deep_link")
    var deepLink:String?=null
//    @JsonBackReference
//    @JsonIgnore
    @OneToMany(mappedBy = "androidTemplateId",cascade = arrayOf(CascadeType.ALL))//try here coloum name
    var actionGroup:List<Action>?=null
    @Column(name = "sound")
    @Pattern(regexp = "^.*(.mp3)$")
    var sound:String?=null
    @Column(name = "badge_icon")
    var badgeIcon=BadgeIconType.BADGE_ICON_NONE
    @Column(name = "collapse_key")
    var collapse_key:String?=null
    @Column(name = "priority")
    var priority= Priority.NORMAL
    @Column(name = "time_to_live")
    var timeToLive:Long?=null                //seconds
    @Column(name = "from_userndot")
    var fromUserNDot:Boolean=true
    @Column(name = "custom_key_value_pair")
    var customKeyValuePair:String?=null
    @field:CreationTimestamp
    @Column(name="creation_date",updatable = false)
    lateinit var creationTime:LocalDateTime
    @field:UpdateTimestamp
    @Column(name="date_modified")
    var dateModified:LocalDateTime= LocalDateTime.now()

    fun addActionGroups(actionGroup:List<Action>?){
        actionGroup?.forEach{
            it.androidTemplateId=this
            it.clientId=AuthenticationUtils.clientID
        }
    }

}

@Entity
@Table(name = "notification_template_android_action")
//@JsonIdentityInfo(
//       property = "id", generator = ObjectIdGenerators.PropertyGenerator::class)
class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,generator = "android_action_id_seq")
    @SequenceGenerator(name="android_action_id_seq",sequenceName = "android_action_id_seq" ,allocationSize = 1)
    var id:Long?=null
//    @JsonManagedReference
    @JsonIgnore
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "android_template_id")
    var androidTemplateId:AndroidTemplate?=null// add an long field instread of andoid template
    @Column(name="client_id")
    var clientId:Long?=null
    @NotNull
    @Column(name = "action_id")
    lateinit var actionId: String
    @NotNull
    @Column(name = "label")
    var label: String? = null
    @Column(name = "deep_link")
    var deepLink: String? = null
    @Column(name = "icon")
    var icon: String? = null
    @Column(name = "auto_cancel")
    var autoCancel: Boolean = true
    @field:CreationTimestamp
    @Column(name="creation_date",updatable = false)
    lateinit var creationTime:LocalDateTime
    @field:UpdateTimestamp
    @Column(name="date_modified")
    lateinit var dataModified:LocalDateTime
}

enum class Priority(name:String){
    NORMAL("normal"),
    HIGH("high")
}

enum class BadgeIconType(name:String){
    BADGE_ICON_SMALL("small"),
    BADGE_ICON_NONE("none"),
    BADGE_ICON_LARGE("large")
}
