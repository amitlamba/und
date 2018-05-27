package com.und.model

enum class MessageType(val value: Short) {
    TRANSACTIONAL(1),
    PROMOTIONAL(2);

    companion object {
        private val map = MessageType.values().associateBy(MessageType::value)
        fun fromValue(type: Short) = map[type]
    }
}