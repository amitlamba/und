package com.und.model.mongo

import com.und.web.model.Reachability
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

//@Document/*(collection="#{tenantProvider.getTenant()}_segmentReachability")*/
//class SegmentReachability {
//    @field:Id
//    lateinit var id:String
//    var clientId:Long?=null
//    var segmentId:Long?=null
//    var date: LocalDate?=null
//    var totalUser: Int = 0
//    var email: Long = 0
//    var sms: Long = 0
//    var webpush: Long = 0
//    var android: Long = 0
//    var ios: Long = 0
//
//}

@Document(collection = "segmentreachability")
class SegmentReachability {
    @field:Id
    var id:Long?=null
    var timeZone:String="UTC"
    var clientId:Long?=null
   var  dates :MutableMap<Int,Map<String,Int>> = mutableMapOf()
    var lastModifiedTime:LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))

}