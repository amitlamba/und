package com.und.web.controller

import com.und.model.IncludeUsers
import com.und.model.mongo.CommonMetadata
import com.und.model.mongo.EventMetadata
import com.und.model.mongo.eventapi.EventUser
import com.und.security.utils.AuthenticationUtils
import com.und.service.EventMetadataService
import com.und.service.SegmentService
import com.und.web.model.IdName
import com.und.web.model.Segment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController("segment")
@RequestMapping("/segment")
@CrossOrigin
class SegmentController {

    @Autowired
    private lateinit var eventMetadataService: EventMetadataService

    @Autowired
    private lateinit var segmentService: SegmentService

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/metadata"])
    fun getEventMetadta(): List<EventMetadata> {
        val clientId = AuthenticationUtils.retrieveClientId()
        return eventMetadataService.getEventMetadata(clientId)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/commonproperties"])
    fun getCommonProperties(): List<CommonMetadata> {
        val clientId = AuthenticationUtils.retrieveClientId()
        return eventMetadataService.getCommonProperties(clientId)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/save"])
    fun save(@Valid @RequestBody segment: Segment): ResponseEntity<Segment> {
        //FIXME Validate for unique name of segment for a client
        val clientID = AuthenticationUtils.retrieveClientId()
        val appuserID = AuthenticationUtils.principal.id!!
        val persistedSegment = segmentService.createSegment(segment, clientID, appuserID)
        return ResponseEntity(persistedSegment, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/list"])
    fun list(): ResponseEntity<List<Segment>> {
        val clientId = AuthenticationUtils.retrieveClientId()
        val allSegment = segmentService.allSegment(clientId)
        return ResponseEntity(allSegment, HttpStatus.OK)

    }



    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/list/idname"])
    fun listIdName(): ResponseEntity<List<IdName>> {
        val clientId = AuthenticationUtils.retrieveClientId()
        val allSegmentIdName = segmentService.allSegmentIdName(clientId)
        return ResponseEntity(allSegmentIdName, HttpStatus.OK)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/segment/{segmentId}"])
    fun segment(@PathVariable("segmentId") segmentId: Long): ResponseEntity<Segment> {
        val clientId = AuthenticationUtils.retrieveClientId()
        val segment = segmentService.segmentById(segmentId, clientId)
        return ResponseEntity(segment, HttpStatus.OK)
    }

    @GetMapping(value = ["/segmentusers/{segmentId}"])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun segmentUsers(@PathVariable("segmentId") segmentId: Long, request: HttpServletRequest): List<EventUser> {
        val clientId = AuthenticationUtils.clientID ?: -1
        val includeUsers = request.getParameter("include") ?: "KNOWN"
       return segmentService.segmentUsers(segmentId, clientId, IncludeUsers.valueOf(includeUsers), null)
    }

    @GetMapping(value = ["/users/{segmentId}/{clientId}"])
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    fun segmentUsers(@PathVariable("segmentId") segmentId: Long, @PathVariable("clientId") clientId: Long, request: HttpServletRequest): List<EventUser> {
        val includeUsers = request.getParameter("include") ?: "KNOWN"
        val forCampaign = request.getParameter("fromCampaign")
        return segmentService.segmentUsers(segmentId, clientId, IncludeUsers.valueOf(includeUsers), forCampaign)
    }


}