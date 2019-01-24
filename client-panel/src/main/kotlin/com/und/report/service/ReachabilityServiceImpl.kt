package com.und.report.service

import com.und.common.utils.loggerFor
import com.und.model.mongo.SegmentReachability
import com.und.report.repository.mongo.ReachabilityRepository
import com.und.report.web.model.Reachability
import com.und.repository.mongo.SegmentReachabilityRepository
import com.und.security.utils.AuthenticationUtils
import com.und.service.SegmentService
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId

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

    @Autowired
    private lateinit var segmentReachabilityRepository: SegmentReachabilityRepository

    override fun getReachabilityBySegmentId(segmentId: Long): Reachability {
        val clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
        var sr= segmentReachabilityRepository.
                findByClientIdAndSegmentIdAndDate(clientId,segmentId,LocalDate.now(ZoneId.of(AuthenticationUtils.principal.timeZoneId)))
        if(sr.isPresent) return buildReachability(sr.get())
        val objectIds = if (segmentId != allUser) {
            val segmentUsers = segmentService.segmentUserIds(segmentId, clientId)
            //at this point one document is insert.
            segmentUsers.map {
                ObjectId(it)
            }
        } else emptyList()
        val result = reachabilityRepository.getReachabilityOfSegment(clientId, segmentId, objectIds)

        val reachability = Reachability()
        with(reachability) {
            totalUser=objectIds.size
            if (result.emailCount.isNotEmpty()) email = result.emailCount[0]
            if (result.mobileCount.isNotEmpty()) sms = result.mobileCount[0]
            if (result.webCount.isNotEmpty()) webpush = result.webCount[0]
            if (result.androidCount.isNotEmpty()) android = result.androidCount[0]
            if (result.iosCount.isNotEmpty()) ios = result.iosCount[0]
        }
        segmentReachabilityRepository.save(buildSegmentReachability(clientId,segmentId,reachability))
        return reachability
    }

    private fun buildReachability(sr:SegmentReachability):Reachability{
        var reachability=Reachability()
        with(reachability){
            totalUser=sr.totalUser
            email=sr.email
            webpush=sr.webpush
            sms=sr.sms
            ios=sr.ios
            android=sr.android
        }
        return reachability
    }

    private fun buildSegmentReachability(cId:Long,sId: Long,reachability: Reachability):SegmentReachability{
        var segmentReachability=SegmentReachability()
        with(segmentReachability){
            clientId=cId
            segmentId=sId
            date= LocalDate.now(ZoneId.of(AuthenticationUtils.principal.timeZoneId))
            totalUser=reachability.totalUser
            email=reachability.email
            sms=reachability.sms
            android=reachability.android
            ios=reachability.ios
            webpush=reachability.webpush
        }
        return segmentReachability
    }
}