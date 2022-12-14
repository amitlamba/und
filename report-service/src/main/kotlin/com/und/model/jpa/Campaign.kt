package com.und.model.jpa

import com.und.model.CampaignStatus
import com.und.web.model.TypeOfCampaign
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "campaign")
class Campaign {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "campaign_id_seq")
    @SequenceGenerator(name = "campaign_id_seq", sequenceName = "campaign_id_seq", allocationSize = 1)
    var id: Long? = null

    @Column(name = "client_id")
    @NotNull
    var clientID: Long? = null

    @Column(name = "appuser_id")
    @NotNull
    var appuserID: Long? = null

    @Column(name = "name")
    @NotNull
    var name: String = ""

    @Column(name = "campaign_type") //Email / SMS / Notifications etc
    @NotNull
    @Enumerated(EnumType.STRING)
    lateinit var campaignType: CampaignType

    @Column(name = "segmentation_id") //TODO Foreign Key
    @NotNull
    var segmentationID: Long?=null


    @Column(name = "schedule")
    @NotNull
    var schedule: String? = null

    @Column(name = "campaign_status", updatable = false, insertable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    var status:CampaignStatus = CampaignStatus.SCHEDULE_PENDING

    @OneToOne(mappedBy = "campaign", fetch = FetchType.LAZY,
            cascade = arrayOf(CascadeType.ALL),
            orphanRemoval = true)
    var emailCampaign: EmailCampaign? = null
        set(value) {
            field = value
            field?.campaign = this
        }

    @OneToOne(mappedBy = "campaign", fetch = FetchType.LAZY,
            cascade = arrayOf(CascadeType.ALL),
            orphanRemoval = true)
    var smsCampaign: SmsCampaign? = null
        set(value) {
            field = value
            field?.campaign = this
        }

    @OneToOne(mappedBy = "campaign", fetch = FetchType.LAZY,
            cascade = arrayOf(CascadeType.ALL),
            orphanRemoval = true)
    var androidCampaign:AndroidCampaign?=null
        set(value){
            field=value
            field?.campaign=this
        }
    @OneToOne(mappedBy = "campaign", fetch = FetchType.LAZY,
            cascade = arrayOf(CascadeType.ALL),
            orphanRemoval = true)
    var webCampaign:WebPushCampaign?=null
        set(value){
            field=value
            field?.campaign=this
        }
    //    @OneToOne(mappedBy = "campaign", fetch = FetchType.LAZY,
//            cascade = arrayOf(CascadeType.ALL),
//            orphanRemoval = true)
//    var iosCampaign:AndroidCampaign?=null
//        set(value){
//            field=value
//            field?.campaign=this
//        }
    @field:CreationTimestamp
    @Column(name = "date_created", updatable = false)
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime

    @Column(name="conversion_event")
    var conversionEvent:String?=null

    @Column(name="service_provider_id")
    var serviceProviderId:Long?=null
    //TODO add sms, and push campaign later

    @Column(name="from_user")
    var fromUser:String?=null

    @Column(name="start_date")
    var startDate: LocalDateTime?=null

    @Column(name="end_date")
    var endDate: LocalDateTime?=null

    @Column(name="type_campaign",nullable = false)  //live,normal,split,ab_test
    @Enumerated(EnumType.STRING)
    lateinit var typeOfCampaign: TypeOfCampaign

    @OneToOne(mappedBy = "campaign",cascade = [CascadeType.ALL])
    var abCampaign:AbCampaign?=null
        set(value) {
            field = value
            field?.campaign = this
        }

    @OneToMany(cascade = [CascadeType.ALL],fetch = FetchType.EAGER)
    @JoinColumn(name="campaign_id")
    var variants:List<Variant> ?= null
}

enum class CampaignType {
    EMAIL,
    SMS,
    PUSH_ANDROID,
    PUSH_WEB,
    PUSH_IOS
}


class Schedule {
    var oneTime: ScheduleOneTime? = null
    var multipleDates: ScheduleMultipleDates? = null
    var recurring: ScheduleRecurring? = null
}

class LiveSchedule {
    var nowOrLater: Now? = Now.Later
    var startTime: CampaignTime? = null
    var endTime: CampaignTime? = null
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

fun toCampaignTime(date: LocalDateTime?):CampaignTime?{
    date?.let {
        val minutes = date.minute
        val hours = date.hour //24 hours
        val dates = date.toLocalDate()
        var aMpM = when (hours) {
            in 0..11 -> AmPm.AM
            else -> AmPm.PM
        }

        var hours1 = when (hours) {
            0,12 -> 12
            in 1..11 -> hours
            else -> hours-12
        }

        val campaignTime = CampaignTime()
        campaignTime.ampm = aMpM
        campaignTime.hours = hours1
        campaignTime.minutes = minutes
        campaignTime.date = dates
        print("minutes $minutes hours $hours date $dates")
        return campaignTime
    }
    return null
}

enum class Now {
    Now,
    Later
}

enum class AmPm {
    AM,
    PM
}
