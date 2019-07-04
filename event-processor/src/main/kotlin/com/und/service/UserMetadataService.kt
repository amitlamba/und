package com.und.service

import com.und.model.mongo.CommonMetadata
import com.und.model.web.EventUser
import com.und.repository.mongo.CommonMetadataRepository
import com.und.utils.Constants
import com.und.utils.MetadataUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service

@Service
class UserMetadataService {

    @Autowired
    private lateinit var commonMetadataRepository : CommonMetadataRepository

    @StreamListener(Constants.BUILD_USER_METADATA)
    fun buildUserMetadata(eventUser: EventUser){
        val userProfileMetadta = buildMetadata(eventUser)
        userProfileMetadta.clientId = eventUser.identity.clientId?.toLong()
        commonMetadataRepository.save(userProfileMetadta,userProfileMetadta.clientId?:-1)
    }
    private fun buildMetadata(eventUser: EventUser): CommonMetadata {
        val propertyName = "UserProperties"
        val metadata = commonMetadataRepository.findByName(propertyName,eventUser.clientId.toLong()) ?: CommonMetadata()
        metadata.name = propertyName
        val properties = MetadataUtil.buildMetadata(eventUser.additionalInfo, metadata.properties)
        metadata.properties.clear()
        metadata.properties.addAll(properties)
        return metadata
    }
}