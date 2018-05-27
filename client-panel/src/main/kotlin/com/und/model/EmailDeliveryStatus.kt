package com.und.model

enum class EmailDeliveryStatus(val value: Short) {
    NOT_SCHEDULED(1),
    SCHEDULED(2),
    IN_PROCESS(3),
    DELIVERED(4);

    companion object {
        private val map = EmailDeliveryStatus.values().associateBy(EmailDeliveryStatus::value)
        fun fromValue(type: Short) = map[type]
    }
}