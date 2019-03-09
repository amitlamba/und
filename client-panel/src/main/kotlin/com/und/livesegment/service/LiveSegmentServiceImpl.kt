package com.und.livesegment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.livesegment.model.jpa.LiveSegment
import com.und.livesegment.model.webmodel.WebLiveSegment
import com.und.livesegment.repository.jpa.LiveSegmentRepository
import com.und.repository.jpa.SegmentRepository
import com.und.security.utils.AuthenticationUtils
import com.und.service.SegmentService
import com.und.web.model.Segment
import com.und.model.jpa.Segment as JpaSegment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.Exception

@Service
class LiveSegmentServiceImpl : LiveSegmentService {

    @Autowired
    lateinit var liveSegmentRepository: LiveSegmentRepository

    @Autowired
    private lateinit var segmentRepository: SegmentRepository

    @Autowired
    private lateinit var segmentService:SegmentService

    @Autowired
    private lateinit var objectMapper:ObjectMapper

    override fun findByClientIDAndStartEvent(clientId: Long, startEvent: String): List<LiveSegment> {
        val segments = liveSegmentRepository.findByClientIDAndStartEvent(clientId, startEvent)
        return segments?: emptyList()

    }

    override fun findByClientIDAndEndEvent(clientId: Long, endEvent: String): List<LiveSegment> {
        val segments = liveSegmentRepository.findByClientIDAndEndEvent(clientId, endEvent)
        return segments?: emptyList()
    }

    override fun saveLiveSegment(segment: WebLiveSegment,clientId: Long,appUserId: Long?) {
         val jpaSegment=buildSegment(segment.segment,clientId,appUserId)
        val liveJpaSegment=buildLiveSegment(segment,clientId)
        saveLiveSegment(jpaSegment,liveJpaSegment)
    }

    @Transactional(rollbackFor = arrayOf(Exception::class))
    protected fun saveLiveSegment(normalSegment:JpaSegment,liveSegment:LiveSegment){
        val persistedSegment=segmentRepository.save(normalSegment)
        liveSegment.segmentId=persistedSegment.id!!
        /*TODO there is no need to delete it because transaction will be roll back in
        * case of any unchecked exception.In kotlin all exception are unchecked.
        * */
        liveSegmentRepository.save(liveSegment)

    }

    override fun getLiveSegments(clientId: Long): List<WebLiveSegment> {
        //TODO
        //get all live segment from db
        //get all live segment type segment from db
        //build webLive segment

        return emptyList()
    }

    private fun buildSegment(websegment: Segment, clientId: Long, appUserId:Long?): JpaSegment {
        val segment = JpaSegment()
        with(segment) {
            id = websegment.id
            name = websegment.name
            type = websegment.type
            clientID = clientId
            appuserID = appUserId
            data = getSimpleJsonStringOfObject(websegment)
        }
        return segment
    }

    private fun buildLiveSegment(segment:WebLiveSegment,clientId: Long): LiveSegment {
        val liveSegment = LiveSegment()
        with(liveSegment) {
            clientID = clientId
            liveSegmentType = segment.liveSegmentType
            startEvent = segment.startEvent
            endEvent = segment.endEvent
            startEventFilter = getSimpleJsonStringOfObject(segment.startEventFilter)
            endEventFilter = getSimpleJsonStringOfObject(segment.endEventFilter)
            interval = segment.interval
        }
        return liveSegment
    }

    private fun getSimpleJsonStringOfObject(instance:Any):String{
        return objectMapper.writeValueAsString(instance)
    }
}