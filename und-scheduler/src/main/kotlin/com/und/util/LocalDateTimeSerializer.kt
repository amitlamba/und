package com.und.util

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.fasterxml.jackson.core.JsonProcessingException
import java.io.IOException
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.core.JsonGenerator



class LocalDateTimeSerializer : StdSerializer<LocalDateTime> {
    companion object {
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

    constructor(t: Class<LocalDateTime>) : super(t) {
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(
            value: LocalDateTime,
            gen: JsonGenerator,
            arg2: SerializerProvider) {

        gen.writeString(formatter.format(value))
    }
}