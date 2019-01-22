package com.und.model

import com.und.util.JobUtil
import org.quartz.CronExpression.isValidExpression
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.DateBuilder
import org.quartz.JobDataMap
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.Trigger
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.TriggerUtils
import org.quartz.impl.calendar.BaseCalendar
import org.quartz.spi.OperableTrigger
import org.springframework.util.StringUtils.isEmpty
import java.sql.Date
import java.time.*
import java.time.ZoneId.systemDefault
import java.time.temporal.Temporal
import java.util.*


class TriggerDescriptor {

    lateinit var name: String
    lateinit var group: String
    var startDate: LocalDate? = null
    var endDate: LocalDate? = null
    var countTimes: Int = 0
    //@JsonSerialize(using = LocalDateTimeSerializer::class)
    var fireTime: LocalDateTime? = null
    var fireTimes: List<LocalDateTime>? = null
    var cron: String? = null


    private fun buildName(jobDescriptor: JobDescriptor): String {
        return JobUtil.getJobName(jobDescriptor)
    }

    private fun buildGroupName(jobDescriptor: JobDescriptor): String {
        return JobUtil.getGroupName(jobDescriptor)
    }


    /**
     * Convenience method for building a Trigger
     *
     * @return the Trigger associated with this descriptor
     */
    fun buildTrigger(jobDescriptor: JobDescriptor): Trigger {
        name = buildName(jobDescriptor)
        group = buildGroupName(jobDescriptor)
        val timeZone = jobDescriptor.timeZoneId
        val startDayTime = startDate?.atStartOfDay(timeZone)
        val endDateTime = endDate?.let {

            val endTime = LocalTime.of(23, 59, 59)
            LocalDateTime.of(endDate, endTime)?.atZone(timeZone)
        }
        val triggerBuilder = when {


            !isEmpty(cron) && !isValidExpression(cron) -> throw IllegalArgumentException("Provided expression '$cron' is not a valid cron expression")
            !isEmpty(cron) && isValidExpression(cron) -> {
                val jobDataMap = JobDataMap()
                jobDataMap["cron"] = cron
                //FIXME handle timezone to UTC, as of now it is set to systemDefault
                newTrigger()
                        .withIdentity(name, group)
                        .withSchedule(cronSchedule(cron)
                                .withMisfireHandlingInstructionFireAndProceed()
                                .inTimeZone(TimeZone.getTimeZone(timeZone)))
                        .usingJobData("cron", cron)
                        .usingJobData("timezone", (TimeZone.getTimeZone(timeZone)).id)
                        .usingJobData("startDate", startDate?.toEpochDay()?.toString())
                        .usingJobData("endDate", endDate?.toEpochDay()?.toString())
                        .usingJobData("countTimes", countTimes.toString())

            }
            !isEmpty(fireTime) -> {
                val firetimeatzone = advanceExpiredToNow(timeZone,fireTime)
                newTrigger()
                        .withIdentity(name, group)
                        .withSchedule(simpleSchedule()
                                .withMisfireHandlingInstructionFireNow()

                        )
                        .startAt(Date.from(firetimeatzone?.toInstant()))
                        .usingJobData("fireTime", firetimeatzone?.toEpochSecond()?.toString())
                        .usingJobData("timezone", (TimeZone.getTimeZone(timeZone)).id)
                        .usingJobData("startDate", startDate?.toEpochDay()?.toString())
                        .usingJobData("endDate", endDate?.toEpochDay()?.toString())
                        .usingJobData("countTimes", countTimes.toString())


            }
            !isEmpty(fireTimes) -> {


                newTrigger()
                        .withIdentity(name, group)
                        .withSchedule(simpleSchedule()
                                .withMisfireHandlingInstructionFireNow()
                                .withIntervalInSeconds(5)
                                //.withIntervalInHours(24)
                                .withRepeatCount((fireTimes?.size ?: 0) - 1)
                        )
                        .modifiedByCalendar(jobDescriptor.calendarName())


            }
            else -> {

                throw IllegalStateException("Specify either one of 'cron' or 'fireTime'")
            }

        }
        if (startDate != null) {
            val startDate: LocalDate = startDate!!
            val db = DateBuilder.newDateInTimezone(TimeZone.getTimeZone(timeZone))
            db.inYear(startDate.year).inMonthOnDay(startDate.monthValue, startDate.dayOfMonth).atHourMinuteAndSecond(0, 0, 0)
            triggerBuilder.startAt(db.build())
            //start time not resolve correctly from one point of view its correct if user select the start data > today if user select today then its not
            //correct make restriction on ui that in case of multiple date he/she not able to select today as start date.instead use other option.
        }
        if (endDate != null) {

            val endDate: LocalDate = endDate!!
            val db = DateBuilder.newDateInTimezone(TimeZone.getTimeZone(timeZone))
            db.inYear(endDate.year).inMonthOnDay(endDate.monthValue, endDate.dayOfMonth).atHourMinuteAndSecond(23, 59, 59)
//            triggerBuilder.startAt(db.build())
//            triggerBuilder.endAt(java.sql.Date.valueOf(endDate))
            triggerBuilder.endAt(db.build())
        }
        if (endDate == null && countTimes > 0) {
            val trigger = triggerBuilder.build()
            val endDate = TriggerUtils.computeEndTimeToAllowParticularNumberOfFirings(trigger as OperableTrigger,
                    BaseCalendar(TimeZone.getTimeZone(timeZone)), countTimes)
            triggerBuilder.endAt(endDate)
        }

        return triggerBuilder.build()


    }

    private fun advanceExpiredToNow(timeZone: ZoneId ,firetime: LocalDateTime?): ZonedDateTime? {
        return fireTime?.let { time ->

            var firetimeatzone = time.atZone(timeZone)
            val now = ZonedDateTime.now(timeZone)
            val expired = firetimeatzone.isBefore(now)
            if (expired) now.plusSeconds(5) else now

        }

    }

    companion object {
        /**
         *
         * @param trigger
         * the Trigger used to build this descriptor
         * @return the TriggerDescriptor
         */
        fun buildDescriptor(trigger: Trigger): TriggerDescriptor {
            val triggerDescriptor = TriggerDescriptor()
            with(triggerDescriptor) {
                name = trigger.key.name
                group = trigger.key.group
                val timeZoneId = trigger.jobDataMap["timezone"] as String
                val tz = TimeZone.getTimeZone(timeZoneId)
                val fireTimeString = trigger.jobDataMap["fireTime"] as String?
                if (!fireTimeString.isNullOrBlank()) {
                    fireTime = fireTimeString?.let { LocalDateTime.ofEpochSecond(fireTimeString.toLong(), 0, ZoneOffset.UTC) }
                }
                cron = trigger.jobDataMap["cron"] as String?
                val startDateString = trigger.jobDataMap["startDate"] as String?
                if (!startDateString.isNullOrBlank()) {
                    startDate = startDateString?.let { LocalDate.ofEpochDay(startDateString.toLong()) }
                }
                val endDateString = trigger.jobDataMap["endDate"] as String?
                if (!endDateString.isNullOrBlank()) {
                    endDate = endDateString?.let { LocalDate.ofEpochDay(endDateString.toLong()) }
                }
                val count = (trigger.jobDataMap["countTimes"] as String?)
                if (!count.isNullOrBlank()) {
                    countTimes = count?.let { count.toInt() } ?: 0
                }
                return triggerDescriptor
            }

        }
    }

}

