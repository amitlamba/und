package com.und.repository.mongo


import com.und.model.mongo.Geogrophy
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface IpLocationRepository{
    fun getGeographyByIpAddress(ipAddress:String):Geogrophy?
}