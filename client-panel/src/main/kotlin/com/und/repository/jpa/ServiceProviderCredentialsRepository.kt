package com.und.repository.jpa

import com.und.model.Status
import com.und.model.jpa.ServiceProviderCredentials
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceProviderCredentialsRepository : JpaRepository<ServiceProviderCredentials, Long> {
    fun findAllByClientIDAndServiceProviderTypeAndStatus(clientID: Long, serviceProviderType: String, status: Status): List<ServiceProviderCredentials>
    fun findAllByClientIDAndServiceProviderType(clientID: Long, serviceProviderType: String): List<ServiceProviderCredentials>
    fun findAllByClientIDAndIdAndServiceProviderType(clientID: Long, id: Long, serviceProviderType: String): ServiceProviderCredentials?
    fun findAllByClientID(clientID: Long): List<ServiceProviderCredentials>
}