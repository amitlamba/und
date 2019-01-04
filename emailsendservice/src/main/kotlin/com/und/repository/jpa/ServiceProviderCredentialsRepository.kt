package com.und.repository.jpa
import com.und.model.jpa.ServiceProviderCredentials
import com.und.model.jpa.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface ServiceProviderCredentialsRepository : JpaRepository<ServiceProviderCredentials, Long> {
    fun findByClientIDAndServiceProviderTypeAndStatus(clientID: Long, serviceProviderType: String, status: Status)
            : List<ServiceProviderCredentials>

    fun findTop1ByClientIDAndServiceProviderTypeAndStatus(clientID: Long, serviceProviderType: String, status: Status)
            : Optional<ServiceProviderCredentials>

    @Query("select sp from ServiceProviderCredentials sp where sp.clientID= :clientId And sp.serviceProviderType= :type And sp.isDefault=true")
    fun findByIsdefaultTrue(@Param("type")type:String,@Param("clientId")clientID: Long):Optional<ServiceProviderCredentials>

}