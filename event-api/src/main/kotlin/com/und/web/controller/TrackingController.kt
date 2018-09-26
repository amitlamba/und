package com.und.web.controller

import com.und.service.eventapi.EventTrackService
import com.und.web.model.eventapi.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletResponse

@CrossOrigin
@Controller
class TrackingController {

    @Autowired
    private lateinit var eventTrackService: EventTrackService

    @GetMapping(value = ["/track"])
    fun trackUrlClick(@RequestParam("c") clientId: Long, @RequestParam("e") mongoEmailId: String,
                      @RequestParam("u") redirectToUrl: String, httpServletResponse: HttpServletResponse) {

        val event = Event()
        event.clientId = clientId
        event.name = "Notification"
        event.notificationId = mongoEmailId
        event.attributes = hashMapOf(
                Pair("und_redirect_to_url", redirectToUrl)
        )

        eventTrackService.toKafka(event)

        httpServletResponse.setHeader("Location", redirectToUrl)
    }
}