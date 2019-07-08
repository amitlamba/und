package com.und.report.service

import com.und.model.Count
import com.und.model.IncludeUsers
import com.und.model.SegmentTrendCount
import com.und.model.mongo.SegmentReachability
import com.und.report.repository.mongo.ReachabilityRepository
import com.und.report.web.model.Reachability
import com.und.repository.jpa.ClientSettingsRepository
import com.und.repository.mongo.SegmentReachabilityRepository
import com.und.service.SegmentService
import com.und.util.loggerFor
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import java.lang.Integer.parseInt
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.NoSuchElementException

@Service("reachabilityserviceimpl")
class ReachabilityServiceImpl : ReachabilityService {

    companion object {
        val logger: Logger = loggerFor(ReachabilityServiceImpl::class.java)
        const val allUser = ReportUtil.ALL_USER_SEGMENT
    }


    @Autowired
    private lateinit var mongoTemplate: MongoTemplate
    @Autowired
    private lateinit var reachabilityRepository: ReachabilityRepository

    @Autowired
    private lateinit var segmentService: SegmentService

    @Autowired
    private lateinit var segmentReachabilityRepository: SegmentReachabilityRepository

    @Autowired
    private lateinit var clientSetting:ClientSettingsRepository

    override fun getReachabilityBySegmentId(segmentId: Long,includeUsers: IncludeUsers,clientId:Long): Reachability {
        //val clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("Access Denied.")
        val objectIds = usersInSegment(segmentId, clientId,includeUsers)
        val result = reachabilityRepository.getReachabilityOfSegment(clientId, segmentId, objectIds)

            val reachability = Reachability()
            with(reachability) {
                totalUser = objectIds.size
                if (result.emailCount.isNotEmpty()) email = result.emailCount[0]
                if (result.mobileCount.isNotEmpty()) sms = result.mobileCount[0]
                if (result.webCount.isNotEmpty()) webpush = result.webCount[0]
                if (result.androidCount.isNotEmpty()) android = result.androidCount[0]
                if (result.iosCount.isNotEmpty()) ios = result.iosCount[0]
            }
             return reachability
    }

//    private fun buildReachability(sr:SegmentReachability,date:String):Reachability{
//        var reachability=sr.dates.get(date)!!
//        return reachability
//    }
//
//    private fun buildSegmentReachability(cId:Long,sId: Long,totalUser: Int,sr:SegmentReachability):SegmentReachability{
//        with(sr){
//            clientId=cId
//            id=sId
//            dates.put(parseInt(LocalDate.now(ZoneId.of(AuthenticationUtils.principal.timeZoneId)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).replace("-","")),totalUser)
//        }
//        return sr
//    }
    fun isPresent(sr: SegmentReachability,date:String):Boolean{
        return sr.dates.contains(parseInt(date.replace("-","")))
    }

    override fun getReachabilityOfSegmentByDate(segmentId: Long, date: String,clientId:Long): Map<String,Int>? {
        //val clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
//        var sr: Optional<SegmentReachability> = findSegmentReachability(clientId, segmentId)
//        if(sr.isPresent){
//            return sr.get().dates[parseInt(date.replace("-",""))]
//        }
//        return null

        return segmentReachabilityRepository.getReachabilityOfSegmentByDate(segmentId,getKey(date),date,clientId)
    }

    override fun getReachabilityOfSegmentByDateRange(clientId: Long,segmentId: Long, date1: String, date2: String): List<SegmentTrendCount> {
        var sr: Optional<SegmentReachability> = findSegmentReachability(clientId, segmentId)
        var startDate= LocalDate.parse(date1)
        var endDate= LocalDate.parse(date2)
        var dateRange= mutableListOf<Int>()
        var result= mutableListOf<SegmentTrendCount>()
        while (startDate.compareTo(endDate)<=0){
            var month=if(startDate.monthValue<10) "0${startDate.monthValue}" else startDate.monthValue
            var day=if(startDate.dayOfMonth<10) "0${startDate.dayOfMonth}" else startDate.dayOfMonth
            dateRange.add(parseInt("${startDate.year}${month}${day}"))
//            result.add(SegmentTrendCount(date=startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),count = 0))
            startDate=startDate.plusDays(1)
        }
        if(sr.isPresent) {
            var dates = sr.get().dates
            for (i in dateRange) {
                result.add(SegmentTrendCount(date = i.toString(), count = buildCount(dates.get(i))))
            }
            //NOTE if end date is today and lastmodified time is > 2 hour update result.
            if(endDate.isEqual(LocalDate.now(ZoneId.of(sr.get().timeZone)))&& sr.get().lastModifiedTime.isBefore(LocalDateTime.now(ZoneId.of(sr.get().timeZone)).minusHours(2))){
                val count=setReachabilityOfSegmentToday(segmentId, clientId)
                result.set((result.size)-1,SegmentTrendCount(date=dateRange.last().toString(),count = buildCount(count)))
            }
        }
        return result
    }

    fun buildCount(count:Map<String,Int>?): Count {
        count?.let {
            val known = count["known"] ?: 0
            val unknown = count["unknown"] ?: 0
            val all = known + unknown
            return Count(known = known, unknown = unknown, all = all)
        }
        return Count(0,0,0)
    }
    //TODO cache the segment
    override fun checkTypeOfSegment(clientId: Long, segmentId: Long): Boolean {
        val segment=segmentService.segmentById(segmentId,clientId)
        if(segment.type.equals("Live")) return true else return false
    }

    private fun setReachabilityOfSegmentNow(count: Map<String, Int>, segmentId: Long, clientId: Long){

        clientSetting.findByClientID(clientId)?.let {
            val timeZoneId= ZoneId.of(it.timezone)
            val todayDate=LocalDate.now(timeZoneId).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val modifiedTime=LocalDateTime.now(timeZoneId)
//        var sr: Optional<SegmentReachability> = findSegmentReachability(clientId, segmentId)
//        if(sr.isPresent && isPresent(sr.get(), todayDate)) {
//            return
//        }else{
//            var segmentReachability:SegmentReachability
//            if(!sr.isPresent) segmentReachability=SegmentReachability() else segmentReachability=sr.get()
//            segmentReachabilityRepository.save(buildSegmentReachability(clientId, segmentId, objectIds.size,segmentReachability))
//        }


            segmentReachabilityRepository.updateSegmentReachability(segmentId,getKey(todayDate),count,clientId,modifiedTime,it.timezone)
        }
    }

    private fun setAllUsersReachabilityOfSegmentNow(count:Map<String,Int>, segmentId: Long, clientId: Long){

        clientSetting.findByClientID(clientId)?.let {
            val timeZoneId= ZoneId.of(it.timezone)
            val todayDate=LocalDate.now(timeZoneId).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val modifiedTime=LocalDateTime.now(timeZoneId)
//        var sr: Optional<SegmentReachability> = findSegmentReachability(clientId, segmentId)
//        if(sr.isPresent && isPresent(sr.get(), todayDate)) {
//            return
//        }else{
//            var segmentReachability:SegmentReachability
//            if(!sr.isPresent) segmentReachability=SegmentReachability() else segmentReachability=sr.get()
//            segmentReachabilityRepository.save(buildSegmentReachability(clientId, segmentId, objectIds.size,segmentReachability))
//        }


            segmentReachabilityRepository.updateAllUsersSegmentReachability(segmentId,getKey(todayDate),count,clientId,modifiedTime,it.timezone)
        }
    }
    override fun setReachabilityOfSegmentToday(segmentId: Long,clientId: Long):Map<String,Int> {
        val known = usersInSegment(segmentId, clientId,IncludeUsers.KNOWN).size
        val unknown = usersInSegment(segmentId, clientId,IncludeUsers.UNKNOWN).size
        val count = mutableMapOf<String,Int>()
        count.put("known",known)
        count.put("unknown",unknown)
        setReachabilityOfSegmentNow(count,segmentId, clientId)
        return count
    }

    private fun usersInSegment(segmentId: Long, clientId: Long, includeUsers: IncludeUsers): List<ObjectId> {
        val objectIds = if (segmentId != allUser) {
            val segmentUsers = segmentService.segmentUserIds(segmentId, clientId,includeUsers)
            segmentUsers.map {
                ObjectId(it)
            }
        } else emptyList()
        return objectIds
    }

    private fun getKey(date: String):String{
        var formattedDate = date.replace("-","")
        return "dates.${formattedDate}"
    }

    private fun findSegmentReachability(clientId: Long, segmentId: Long): Optional<SegmentReachability> {
        var sr: Optional<SegmentReachability> = Optional.empty()
        try {
            sr = segmentReachabilityRepository.findByClientIdAndId(clientId, segmentId)
        } catch (ex: NoSuchElementException) {
            //when document not exist it throw Exception
        }
        return sr
    }
}

class SegmentResult{
    var key:Map<String,Int> = mutableMapOf()
}