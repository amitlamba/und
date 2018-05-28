package com.und.model

import org.quartz.impl.calendar.HolidayCalendar
import java.time.*


class FixedDateCalendar(
        val fireTimes: List<LocalDate>
) : HolidayCalendar() {


    private var triggeredcount: Int = 0
    override fun isTimeIncluded(p0: Long): Boolean {
        val dateTime = millsToLocalDateTime(p0)
        if(triggeredcount >= fireTimes.size)
            return false
        if(fireTimes.contains(dateTime)) {

            return true
        }
        return false
    }



    private fun millsToLocalDateTime(millis: Long): LocalDate {
        val instant = Instant.ofEpochMilli(millis)
        return instant.atZone(ZoneId.systemDefault()).toLocalDate()
    }


}