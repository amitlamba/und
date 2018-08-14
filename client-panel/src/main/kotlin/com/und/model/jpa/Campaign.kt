package com.und.model.jpa


import com.und.model.CampaignStatus
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
    var status: CampaignStatus = CampaignStatus.SCHEDULE_PENDING

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

    @field:CreationTimestamp
    @Column(name = "date_created", updatable = false)
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime

    //TODO add sms, and push campaign later
}

enum class CampaignType {
    EMAIL,
    SMS,
    MOBILE_PUSH_NOTIFICATION
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
enum class Now {
    Now,
    Later
}

enum class AmPm {
    AM,
    PM
}
