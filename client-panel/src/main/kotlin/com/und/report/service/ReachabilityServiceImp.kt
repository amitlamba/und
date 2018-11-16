package com.und.report.service

import com.und.report.repository.mongo.ReachabilityRepository
import com.und.report.web.model.Reachability
import com.und.security.utils.AuthenticationUtils
import com.und.service.SegmentService
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service

@Service
class ReachabilityServiceImp:ReachabilityService {

    @Autowired
    private lateinit var reachabilityRepository: ReachabilityRepository
    @Autowired
    private lateinit var segmentService:SegmentService

    override fun getReachabilityBySegmentId(segmentId: Long): Reachability {
        var clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("")
        var segmentUsers=segmentService.segmentUserIds(segmentId,clientId)
        var objectIds=segmentUsers.map {
             ObjectId(it)
        }
        var result= reachabilityRepository.getReachabilityOfSegment(clientId,segmentId,objectIds)

        var reachability=Reachability()
        with(reachability){
            if(result.emailCount.isNotEmpty()) email=result.emailCount[0]
            if(result.mobileCount.isNotEmpty()) sms=result.mobileCount[0]
            if(result.webCount.isNotEmpty()) webpush=result.webCount[0]
            if(result.androidCount.isNotEmpty()) android=result.androidCount[0]
            if(result.iosCount.isNotEmpty()) ios=result.iosCount[0]
        }
        return reachability
    }
}