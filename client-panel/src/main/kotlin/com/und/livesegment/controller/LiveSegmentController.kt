package com.und.livesegment.controller

import com.und.livesegment.model.jpa.LiveSegment
import com.und.livesegment.model.mongo.LiveSegmentReportCount
import com.und.livesegment.model.webmodel.WebLiveSegment
import com.und.livesegment.service.LiveSegmentService
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.CustomException
import com.und.web.model.DidEvents
import com.und.web.model.Response
import com.und.web.model.ResponseStatus
import com.und.web.model.Segment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.naming.AuthenticationException
import javax.validation.Valid

@RestController
@RequestMapping("/livesegment")
class LiveSegmentController {

    @Autowired
    private lateinit var liveSegmentService:LiveSegmentService

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/get/live/segments")
    fun getLiveSegments():List<WebLiveSegment>{
        val clientId=AuthenticationUtils.clientID?:throw throw AccessDeniedException("Access Denied.")
        return liveSegmentService.getLiveSegments(clientId)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/save")
    fun saveLiveSegment(@Valid @RequestBody liveSegment: WebLiveSegment):ResponseEntity<HttpStatus>{
        val clientId=AuthenticationUtils.clientID?:throw throw AccessDeniedException("Access Denied.")
        val appUserId=AuthenticationUtils.principal.id
        liveSegmentService.segmentValidator(liveSegment.segment)
        return try {
            liveSegmentService.saveLiveSegment(liveSegment,clientId,appUserId)
            ResponseEntity(HttpStatus.CREATED)
        }catch (ex:Exception){
            throw CustomException("Failed To save Segment. ${ex.message}")
        }

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/get/live/segment/{id}")
    fun getLiveSegmentById(@PathVariable("id",required = true)id:Long):WebLiveSegment{
        val clientId=AuthenticationUtils.clientID?:throw throw AccessDeniedException("Access Denied.")
        return liveSegmentService.getLiveSegmentByClientIDAndId(clientId,id)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/get/ls/users")
    fun getLiveSegmentUserCount(@RequestParam("segmentId",required = true)segmentId:Long):Long{
        val clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("Access Denied.")
        return liveSegmentService.getLiveSegmentUsersCount(clientId, segmentId)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/report/{segmentId}")
    fun getLiveSegmentReportCountInDateRange(
            @RequestParam("startDate",required = true)startDate:String,
            @RequestParam("endDate",required = true)endDate:String,
            @PathVariable("segmentId",required = true)segmentId: Long):LiveSegmentReportCount{
        val clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("Access Denied.")
        return liveSegmentService.getLiveSegmentReportByDateRange(startDate,endDate,segmentId,clientId)
    }
}