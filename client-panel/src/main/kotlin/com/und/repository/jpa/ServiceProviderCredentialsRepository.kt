package com.und.repository.jpa

import com.sun.org.apache.xpath.internal.operations.Bool
import com.und.model.Status
import com.und.model.jpa.ServiceProviderCredentials
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ServiceProviderCredentialsRepository : JpaRepository<ServiceProviderCredentials, Long> {
    fun findAllByClientIDAndServiceProviderTypeAndStatus(clientID: Long, serviceProviderType: String, status: Status): List<ServiceProviderCredentials>
    fun findAllByClientIDAndServiceProviderType(clientID: Long, serviceProviderType: String): List<ServiceProviderCredentials>
    fun findAllByClientIDAndIdAndServiceProviderType(clientID: Long, id: Long, serviceProviderType: String): ServiceProviderCredentials?
    fun findAllByClientID(clientID: Long): List<ServiceProviderCredentials>
    @Modifying
    @Query("update ServiceProviderCredentials set isDefault=true where id=?1")
    fun markSPDefault(id:Long)
    @Modifying
    @Query("update ServiceProviderCredentials set isDefault=false where serviceProviderType=?1 AND clientID=?2 AND isDefault=true")
    fun unMarkDefaultSp(serviceProviderType:String,clientID: Long)

//    fun findAllByClientIDAndServiceProviderType(clientID: Long,serviceProviderType: String):List<ServiceProviderCredentials>
}