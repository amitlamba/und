package com.und.livesegment.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.und.livesegment.model.jpa.LiveSegment
import com.und.livesegment.model.mongo.CountPerDay
import com.und.livesegment.model.mongo.LiveSegmentReportCount
import com.und.livesegment.model.webmodel.LiveSegmentCount
import com.und.livesegment.model.webmodel.WebLiveSegment
import com.und.livesegment.repository.jpa.LiveSegmentRepository
import com.und.livesegment.repository.mongo.LiveSegmentResult
import com.und.livesegment.repository.mongo.LiveSegmentUserTrackRepository
import com.und.repository.jpa.SegmentRepository
import com.und.repository.jpa.UserRepository
import com.und.repository.mongo.SegmentMetadataRepository
import com.und.security.utils.AuthenticationUtils
import com.und.service.CreateMetadataService
import com.und.service.SegmentService
import com.und.web.controller.exception.CustomException
import com.und.web.model.DidEvents
import com.und.web.model.PropertyFilter
import com.und.web.model.Segment
import com.und.model.jpa.Segment as JpaSegment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.Exception
import java.time.LocalDate
import java.util.*
import kotlin.Comparator

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

    @Autowired
    private lateinit var liveSegmentUserRepository: LiveSegmentUserTrackRepository

    @Autowired
    private lateinit var metadataService: CreateMetadataService
    @Autowired
    private lateinit var metadataRepository:SegmentMetadataRepository

    override fun findByClientIDAndStartEvent(clientId: Long, startEvent: String): List<LiveSegment> {
        val segments = liveSegmentRepository.findByClientIDAndStartEvent(clientId, startEvent)
        return segments?: emptyList()

    }

    override fun findByClientIDAndEndEvent(clientId: Long, endEvent: String): List<LiveSegment> {
        val segments = liveSegmentRepository.findByClientIDAndEndEvent(clientId, endEvent)
        return segments?: emptyList()
    }

    override fun saveLiveSegment(segment: WebLiveSegment,clientId: Long,appUserId: Long?) {
         val jpaSegment=buildSegment(segment.segment!!,clientId,appUserId)
        val liveJpaSegment=buildLiveSegment(segment,clientId)
        saveLiveSegment(jpaSegment,liveJpaSegment)
    }

    @Transactional(rollbackFor = arrayOf(Exception::class))
    fun saveLiveSegment(normalSegment:JpaSegment,liveSegment:LiveSegment){
        //TODO cache start , end date of
        val persistedSegment=segmentRepository.save(normalSegment)
        liveSegment.segmentId=persistedSegment.id!!
        try {
            liveSegmentRepository.save(liveSegment)
            //we can do it in background also.
            val metadata = metadataService.createSegmentMetadata(buildWebSegment(normalSegment),liveSegment.segmentId,liveSegment.clientID!!,"live")
            metadataRepository.save(metadata)
        }catch (ex:Throwable){
            segmentRepository.deleteById(persistedSegment.id)
            throw ex
        }


    }

    override fun getLiveSegments(clientId: Long): List<WebLiveSegment> {
        val liveSegments=liveSegmentRepository.findByClientID(clientId)
        val behaviouralSegments=segmentRepository.findByClientIDAndType(clientId,"Live")

            return liveSegments.map {
                val segment=behaviouralSegments.find { segment ->  segment.id==it.segmentId}!!
                buildWebLiveSegment(it,segment)
            }

    }

    override fun getLiveSegmentByClientIDAndId(clientId: Long, id: Long): WebLiveSegment {
        val liveSegment=liveSegmentRepository.findByClientIDAndId(clientId, id)
        if(liveSegment.isPresent){
            val normalSegment=segmentRepository.findByIdAndClientID(liveSegment.get().segmentId,clientId)
            return buildWebLiveSegment(liveSegment.get(),normalSegment.get())
        }else throw CustomException("Live Segment for client $clientId and id $id not exists.")
    }

    override fun getLiveSegmentUsersCount(clientId: Long, segmentId: Long): LiveSegmentCount {
        val result=liveSegmentUserRepository.findCountByClientIdAndSegmentId(clientId, segmentId)
        return LiveSegmentCount(known = result.first,unknown = result.second)
    }

    override fun getJpaLiveSegmentByClientIdAndId(clientId: Long, id: Long): LiveSegment {
        val liveSegment=liveSegmentRepository.findByClientIDAndId(clientId, id)
        if(liveSegment.isPresent) return liveSegment.get()
        else throw CustomException("Live Segment with id $id clientId $clientId not found")
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
            startEventFilter = getSimpleJsonStringOfObject(segment.startEventFilters)
            endEventFilter = getSimpleJsonStringOfObject(segment.endEventFilters)
            interval = segment.interval
            endEventDone = segment.endEventDone
        }
        return liveSegment
    }

    private fun buildWebLiveSegment(liveSegment: LiveSegment,normalSegment: JpaSegment):WebLiveSegment{
        val normalWebSegment=buildWebSegment(normalSegment)
        val liveWebSegment=WebLiveSegment()
        with(liveWebSegment){
            id=liveSegment.id
            clientId=liveSegment.clientID
            segment=normalWebSegment
            liveSegmentType=liveSegment.liveSegmentType
            interval=liveSegment.interval
            startEvent=liveSegment.startEvent
            endEvent=liveSegment.endEvent
            startEventFilters=getPropertyFilterListFromJsonString(liveSegment.startEventFilter)
            endEventFilters=getPropertyFilterListFromJsonString(liveSegment.endEventFilter)
        }
        return liveWebSegment
    }

    private fun buildWebSegment(segment: JpaSegment):Segment{
        val websegment = objectMapper.readValue(segment.data, Segment::class.java)
        with(websegment) {
            id = segment.id
            name = segment.name
            type = segment.type
        }
        return websegment
    }

    private fun getPropertyFilterListFromJsonString(propertyFiltersJson:String):List<PropertyFilter>{
        return objectMapper.readValue(propertyFiltersJson)
    }

    private fun getSimpleJsonStringOfObject(instance:Any):String{
        return objectMapper.writeValueAsString(instance)
    }

    override fun segmentValidator(segment: Segment?) {
        if (segment == null) {
            throw CustomException("Failed To save Segment. Segment not be null.")
        } else {
            if (segment.didEvents == null) {
                segment.didEvents = DidEvents()
            }
            if (segment.didNotEvents==null){
                segment.didNotEvents= DidEvents()
            }
        }
    }

    override fun getLiveSegmentReportByDateRange(startDate:String,endDate:String,segmentId: Long,clientId: Long):LiveSegmentReportCount{
        val result=liveSegmentUserRepository.getLiveSegmentReportByDateRange(startDate, endDate, clientId, segmentId)
        val list= mutableSetOf<String>()
        val liveSegmentCount= mutableListOf<CountPerDay>()

        var totalUsers:Int=0
        result.forEach {
            liveSegmentCount.add(CountPerDay(
                    totalUsersPerDay = it.totalusersperday,
                    uniqueUsersPerDay = it.uniqueusersperday,
                    date = it.date))
            list.addAll(it.users)
            totalUsers += it.totalusersperday
        }
        return  LiveSegmentReportCount(sortingByDate(liveSegmentCount),totalUsers,list.size)
    }

    fun sortingByDate(liveSegmentCount:List<CountPerDay>):List<CountPerDay>{
        Collections.sort(liveSegmentCount,object :Comparator<CountPerDay>{
            override fun compare(o1: CountPerDay?, o2: CountPerDay?): Int {
                val date1=LocalDate.parse(o1?.date)
                val date2=LocalDate.parse(o2?.date)
                return date1.compareTo(date2)
            }
        })
        return liveSegmentCount
    }

}