package com.und.service

import com.und.model.*
import com.und.model.web.Event
import com.und.repository.mongo.IpLocationRepository
import com.und.utils.Constants
import com.und.utils.copyToMongo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class PreProcessEvent {

    @Autowired
    private lateinit var ipLocationRepository:IpLocationRepository

    @StreamListener(Constants.EVENT_QUEUE)
    @SendTo(Constants.EVENT_INTER_STATE)
    fun procesEvent(event:Event):MongoEvent{
        val mongoEvent = event.copyToMongo()
        mongoEvent.clientTime.hour
        var agentString = event.agentString ?: ""
        var pattern = Pattern.compile("^(Mobile-Agent).*")
        var matcher = pattern.matcher(agentString)
        //No need to check is empty
        if (matcher.matches() && agentString.isNotEmpty()) {
            val system = System()
            mongoEvent.system = system
            var agent = agentString.split("/")
            with(system) {
                os = SystemDetails(name = agent[1], version = agent[2])
                browser = SystemDetails(name = agent[3], version = agent[4])
                device = SystemDetails(name = agent[5], version = agent[6])
                application = SystemDetails(name = agent[7], version = agent[8])
            }

            mongoEvent.system = system

            var appFileds = AppField()
            with(appFileds) {
                make = agent[9]
                model = agent[6]
                sdkversion = agent[10]
                appversion = agent[8]
                os = agent[1]
            }
            mongoEvent.appfield = appFileds

        }
        if (event.country == null || event.state == null || event.city == null) {

            var geogrophy = getGeography(event.ipAddress)
            geogrophy?.let { mongoEvent.geogrophy = geogrophy }
        }
        return mongoEvent
    }

    private fun getGeography(ipAddress: String?): Geogrophy? {
        if (ipAddress != null) {
            return ipLocationRepository.getGeographyByIpAddress(ipAddress)
        }
        return null
    }
}