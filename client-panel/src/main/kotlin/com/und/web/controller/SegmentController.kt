package com.und.web.controller

import com.und.model.mongo.CommonMetadata
import com.und.model.mongo.EventMetadata
import com.und.model.mongo.eventapi.EventUser
import com.und.service.EventMetadataService
import com.und.service.SegmentService
import com.und.web.model.Segment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

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
        return eventMetadataService.getEventMetadata()
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/commonproperties"])
    fun getCommonProperties(): List<CommonMetadata> {
        return eventMetadataService.getCommonProperties()
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/save"])
    fun save(@RequestBody segment: Segment): ResponseEntity<Segment> {
        //FIXME Validate for unique name of segment for a client
        val persistedSegment = segmentService.createSegment(segment)
        return ResponseEntity( persistedSegment, HttpStatus.CREATED)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/list"])
    fun list(): ResponseEntity<List<Segment>> {
        val allSegment = segmentService.allSegment()
        return ResponseEntity( allSegment, HttpStatus.OK)
    }

    @GetMapping(value = ["/users/{segmentId}"])
    @PreAuthorize("hasRole('ROLE_UND_SERVICE')")
    fun segmentUsers(@PathVariable("segmentId") segmentId :Long): List<EventUser> {
        val clientId = 2L
        val asegmentUsers = segmentService.segmentUsers(segmentId, clientId)
        return  asegmentUsers
    }



}