package com.und.model.jpa

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.*
import javax.validation.constraints.NotNull

//@Entity
//@Table(name = "campaign")
//class Campaign {
//
//    @Id
//    @Column(name = "id")
//    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "campaign_id_seq")
//    @SequenceGenerator(name = "campaign_id_seq", sequenceName = "campaign_id_seq", allocationSize = 1)
//    var campaignId: Long = 0
//
//    @Column(name = "segmentation_id")
//    var segmentId: Long = 0
//    @Column(name = "campaign_type")
//    lateinit var campaignType: String
//    @Column(name = "email_template_id")
//    var emailTemplateId: Long? = null
//    @Column(name = "email_template_name")
//    var emailTemplateName: String? = null
//    @Column(name = "sms_template_id")
//    var smsTemplateId: Long? = null
//    @Column(name = "email_from_user")
//    var fromEmailAddress: String? = null
//    @Column(name = "sms_from_user")
//    var fromSMSUser: String? = null
//    @Column(name = "client_id")
//    var clientId: Long = 0
//    @Column(name = "android_template_id")
//    var androidTemplateId: Long? = null
//    @Column(name = "web_template_id")
//    var webTemplateId: Long? = null
//    @Column(name = "service_provider_id")
//    var serviceProviderId: Long? = null
////    @Column(name = "ios_template_id")
////    var iosTemplateId:Long?=null
//}

@Entity
@Table(name = "campaign")
class Campaign {

    @Id
    @Column(name = "id")
    var id: Long? = null

    @Column(name = "client_id")
    var clientID: Long? = null

    @Column(name = "campaign_type") //Email / SMS / Notifications etc
    lateinit var campaignType: String

    @Column(name = "segmentation_id") //TODO Foreign Key
    var segmentationID: Long?=null

    @Column(name = "conversion_event")
    var conversionEvent: String? = null

    @Column(name = "service_provider_id")
    var serviceProviderId: Long? = null
    //TODO add sms, and push campaign later

    @Column(name = "from_user")
    var fromUser: String? = null

    @Column(name = "schedule")
    @NotNull
    var schedule: String? = null

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name="campaign_status")
    var status:CampaignStatus = CampaignStatus.SCHEDULE_PENDING


    @Column(name="start_date")
    var startDate:LocalDateTime?=null

    @Column(name="end_date")
    var endDate:LocalDateTime?=null

    @Column(name="type_campaign",nullable = false)  //live,normal,split,ab_test
    var typeOfCampaign:TypeOfCampaign?=null

    @OneToOne(mappedBy = "campaign",cascade = [CascadeType.ALL])  //bi directional
    @Column(name="ab_campaign")
    var abCampaign:AbCampaign?=null

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name="campaign_id")
    var variants:List<Variant>? = null


}

enum class TypeOfCampaign {
    NORMAL,
    LIVE,
    SPLIT,
    AB_TEST
}

@Entity
@Table(name="ab_campaign")
class AbCampaign {

    @Id
    var id:Long?=null
    @Column(name="campaign_id",nullable = false)

    @JoinColumn(name = "campaign_id")
    lateinit var campaign:Campaign  //one to one
    @Column(name = "run_type")
    var runType:RunType = RunType.AUTO
    @Column(name="rewind")
    var remind:Boolean =false
    @Column(name="wait_time")
    var waitTime:Int?=null     //in minutes
    @Column(name="sample_size")
    var sampleSize:Int?=null
//    @Column(name="live_sample_size")
//    var liveSampleSize:Int?=null   //optional we are taking this info in variant also

}

@Entity
@Table(name="variant")
class Variant {
    @Id
    var id:Long?=null
    @Column(name="percentage",nullable = false)
    var percentage:Int?=null
    @Column(name="name")
    var name:String?=null
//    @Column(name="counter")
//    var counter:Int?=null
    @Column(name="users")
    var users:Int?=null
    @Column(name="winner")
    var winner:Boolean=false
    @Column(name="template_id",nullable = false)
    var templateId:Int?=null
}

enum class RunType{
    MANUAL,
    AUTO
}

enum class CampaignStatus {
    PAUSED,
    RESUMED,
    CREATED,
    ERROR,
    SCHEDULE_PENDING,
    SCHEDULE_ERROR,
    DELETED,
    STOPPED,
    COMPLETED,
    FORCE_PAUSED,
    AB_COMPLETED
}


class Schedule {
    var oneTime: ScheduleOneTime? = null
    var multipleDates: ScheduleMultipleDates? = null
    var recurring: ScheduleRecurring? = null
}

class ScheduleOneTime {
    var nowOrLater: Now? = Now.Later
    var campaignDateTime: CampaignTime? = null
}

class ScheduleMultipleDates {
    var campaignDateTimeList: List<CampaignTime> = mutableListOf()
}

class ScheduleRecurring {
    lateinit var cronExpression: String
    var scheduleStartDate: LocalDate? = null
    var scheduleEnd: ScheduleEnd? = null
}

class CampaignTime {
    lateinit var date: LocalDate
    var hours: Int? = 0
    var minutes: Int? = 0
    lateinit var ampm: AmPm

    fun toLocalDateTime(): LocalDateTime {

        val minutes = minutes ?: 0
        val hour = ampm.let { amPm ->

            hours?.let {h->
                when (amPm) {
                    AmPm.PM -> {
                        if (h in 1..11) h + 12 else h
                    }
                    AmPm.AM -> {
                        if (h == 12) h - 12 else h
                    }
                }
            }

        } ?: 0
        val localTime = LocalTime.of(hour, minutes)
        return LocalDateTime.of(date, localTime)
    }
}

class ScheduleEnd {
    var endType: ScheduleEndType? = null
    var endsOn: LocalDate? = null
    var occurrences: Int = 0
}

enum class ScheduleEndType {
    NeverEnd,
    EndsOnDate,
    Occurrences
}

enum class Now {
    Now,
    Later
}

enum class AmPm {
    AM,
    PM
}