package com.und.report.service

import com.und.report.web.model.Reachability
import org.springframework.stereotype.Service

@Service
interface ReachabilityService {
    fun getReachabilityBySegmentId(segmentId:Long):Reachability
}