package com.und.repository.jpa

import com.und.web.model.ClientEmailSettIdFromAddrSrp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CustomFromAddrAndSrpRepository:JpaRepository<ClientEmailSettIdFromAddrSrp,Long> {
    @Query("select ces.id as ceid ,ces.email as from_address ,sp.name as srp_name from client_setting_email ces  " +
            "INNER JOIN service_provider_credentials sp on ces.service_provider_id=sp.id where ces.client_id=?1 And ces.verified=true",nativeQuery = true)
    fun joinClientEmailSettingAndServicePtoivder(clientId: Long):List<ClientEmailSettIdFromAddrSrp>
}