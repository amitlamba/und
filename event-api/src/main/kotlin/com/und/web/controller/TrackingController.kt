package com.und.web.controller

import com.und.eventapi.utils.ipAddr
import com.und.security.utils.TenantProvider
import com.und.service.eventapi.EventTrackService
import com.und.web.model.eventapi.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@CrossOrigin(origins = ["*"],methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS])
@Controller
class TrackingController {

    @Autowired
    private lateinit var eventTrackService: EventTrackService

    @Autowired
    private lateinit var tenantProvider: TenantProvider

    @GetMapping(value = ["/track"])
    fun trackUrlClick(@RequestParam("c") clientId: Long, @RequestParam("e") mongoEmailId: String,
                      @RequestParam("u") redirectToUrl: String, httpServletResponse: HttpServletResponse,request:HttpServletRequest) {

        val event = Event()
        event.clientId = clientId
        event.name = "Notification Clicked"
        event.notificationId = mongoEmailId
        //TODO put campaign id in attributes
        event.attributes = hashMapOf(
                Pair("und_redirect_to_url", redirectToUrl)
        )
        event.agentString=request.getHeader("User-Agent")
        event.ipAddress=request.ipAddr()
        tenantProvider.setTenat(clientId.toString())
        eventTrackService.toKafka(event)

        httpServletResponse.setHeader("Location", redirectToUrl)
        httpServletResponse.status=HttpStatus.TEMPORARY_REDIRECT.value()
    }
}