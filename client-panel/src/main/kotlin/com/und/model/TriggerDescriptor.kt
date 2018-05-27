package com.und.model

import java.time.LocalDate
import java.time.LocalDateTime


class TriggerDescriptor {

    var startDate: LocalDate? = null
    var endDate: LocalDate? = null
    var countTimes: Int = 0
    //@JsonSerialize(using = LocalDateTimeSerializer::class)
    var fireTime: LocalDateTime? = null
    var fireTimes: List<LocalDateTime>? = null
    var cron: String? = null


}

