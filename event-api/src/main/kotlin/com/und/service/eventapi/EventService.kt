package com.und.service.eventapi

import com.und.config.EventStream
import com.und.eventapi.utils.ipAddr
import com.und.security.utils.AuthenticationUtils
import com.und.security.utils.TenantProvider
import com.und.web.model.eventapi.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import com.und.model.mongo.eventapi.Event as MongoEvent

@Service
class EventService {


    @Autowired
    private lateinit var tenantProvider: TenantProvider


    @Autowired
    private lateinit var eventStream: EventStream

    fun toKafka(event: Event): Boolean = eventStream.outEvent().send(MessageBuilder.withPayload(event).build())

    fun buildEvent(fromEvent: Event, request: HttpServletRequest): Event {
        with(fromEvent) {
            if (fromEvent.clientId != -1L) clientId = fromEvent.clientId else clientId = tenantProvider.tenant.toLong()
            ipAddress = request.ipAddr()
            timeZone = AuthenticationUtils.principal.timeZoneId
            var agent = request.getHeader("User-Agent")
            agentString = if (agent != "mobile") agent else request.getHeader("Mobile-Agent")
        }
        return fromEvent
    }

}
