package com.und.repository.mongo

import com.und.model.mongo.IpLocation
import com.und.model.mongo.eventapi.Geogrophy
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface IpLocationRepository{
    fun getGeographyByIpAddress(ipAddress:String):Geogrophy?
}