package com.und.report.service

import com.und.report.web.model.Reachability
import com.und.security.utils.AuthenticationUtils
import com.und.service.SegmentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException

class ReachabilityServiceImp:ReachabilityService {

    @Autowired
    private lateinit var segmentService:SegmentService

    override fun getReachabilityBySegmentId(segmentId: Long): Reachability {
        var clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("")
        var segmentUsers=segmentService.segmentUserIds(segmentId,clientId)
        //2 todo find total reachable user based on all device

        var totalEmailUser=
        //3 todo find intersection and returu result
    }
}