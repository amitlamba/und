package com.und.livesegment.controller

import com.und.livesegment.model.jpa.LiveSegment
import com.und.livesegment.model.webmodel.WebLiveSegment
import com.und.livesegment.service.LiveSegmentService
import com.und.security.utils.AuthenticationUtils
import com.und.web.model.Response
import com.und.web.model.ResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.naming.AuthenticationException

@RestController
class LiveSegmentController {

    @Autowired
    private lateinit var liveSegmentService:LiveSegmentService

    @GetMapping("/get/live/segments")
    fun getLiveSegments():List<LiveSegment>{
        return emptyList()
    }

    @PostMapping("/save")
    fun saveLiveSegment(@RequestBody liveSegment: WebLiveSegment):ResponseEntity<HttpStatus>{
        val clientId=AuthenticationUtils.clientID?:throw AuthenticationException("Access Denied.")
        val appUserId=AuthenticationUtils.principal.id
        return try {
            liveSegmentService.saveLiveSegment(liveSegment,clientId,appUserId)
            ResponseEntity(HttpStatus.CREATED)
        }catch (ex:Exception){
            throw ex
        }

    }

    @GetMapping("/get/live/segment/{id}")
    fun getLiveSegmentById(@PathVariable("id",required = true)id:Long):WebLiveSegment{
                return WebLiveSegment()
    }

}