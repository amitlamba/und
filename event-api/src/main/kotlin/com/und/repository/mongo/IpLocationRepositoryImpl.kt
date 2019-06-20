//package com.und.repository.mongo
//
//import com.und.model.mongo.IpLocation
//import com.und.model.mongo.eventapi.Geogrophy
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.data.mongodb.core.MongoTemplate
//import org.springframework.data.mongodb.core.query.Criteria
//import org.springframework.data.mongodb.core.query.Query
//import org.springframework.stereotype.Repository
//
//@Repository
//class IpLocationRepositoryImpl :IpLocationRepository{
//
//    @Autowired
//    private lateinit var mongoTemplate: MongoTemplate
//
//    override fun getGeographyByIpAddress(ipAddress: String):Geogrophy? {
//        //convert ip address into decimal
//        var ipToDecimal = convertIpv4ToDecimal(ipAddress)
//        if(ipToDecimal==0) return null
//        var result= mongoTemplate.findOne(
//                Query().addCriteria(Criteria()
//                        .andOperator(
//                                Criteria("from").lte(ipToDecimal),
//                                Criteria("to").gte(ipToDecimal))),
//                IpLocation::class.java,"ip_location_mapping")
//        result?.let {
//            return Geogrophy(country = it.country,state = it.state,city = it.city)
//        }
//        return null
//    }
//
//    private fun convertIpv4ToDecimal(ipAddress: String): Int {
//        if(ipAddress.contains(":")) return 0
//        val octet = ipAddress.split(".")
//        val octet1 = Integer.parseInt(octet[0]) * 16777216
//        val octet2 = Integer.parseInt(octet[1]) * 65536
//        val octet3 = Integer.parseInt(octet[2]) * 256
//        //val octet4 = Integer.parseInt(octet[3])
//        //Fixme remove below line when we have commercial database of ip address currently our database not support this level of preciseness
//        val octet4 = 0
//        return octet1 + octet2 + octet3 + octet4
//    }
//}