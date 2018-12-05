package com.und.report.service

import com.und.common.utils.loggerFor
import com.und.report.repository.mongo.ReachabilityRepository
import com.und.report.web.model.Reachability
import com.und.security.utils.AuthenticationUtils
import com.und.service.SegmentService
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service

@Service
class ReachabilityServiceImpl : ReachabilityService {

    companion object {
        val logger: Logger = loggerFor(ReachabilityServiceImpl::class.java)
        const val allUser = ReportUtil.ALL_USER_SEGMENT
    }


    @Autowired
    private lateinit var reachabilityRepository: ReachabilityRepository

    @Autowired
    private lateinit var segmentService: SegmentService

    override fun getReachabilityBySegmentId(segmentId: Long): Reachability {
        val clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")

        val objectIds = if (segmentId != allUser) {
            val segmentUsers = segmentService.segmentUserIds(segmentId, clientId)
            segmentUsers.map {
                ObjectId(it)
            }
        } else emptyList()
        val result = reachabilityRepository.getReachabilityOfSegment(clientId, segmentId, objectIds)

        val reachability = Reachability()
        with(reachability) {
            if (result.emailCount.isNotEmpty()) email = result.emailCount[0]
            if (result.mobileCount.isNotEmpty()) sms = result.mobileCount[0]
            if (result.webCount.isNotEmpty()) webpush = result.webCount[0]
            if (result.androidCount.isNotEmpty()) android = result.androidCount[0]
            if (result.iosCount.isNotEmpty()) ios = result.iosCount[0]
        }
        return reachability
    }
}