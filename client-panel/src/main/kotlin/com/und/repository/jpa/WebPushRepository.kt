package com.und.repository.jpa

import com.und.model.jpa.WebPushTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WebPushRepository:JpaRepository<WebPushTemplate,Long> {
    fun save(template: WebPushTemplate):WebPushTemplate
    fun findByClientIdAndId(clientId:Long,id:Long):WebPushTemplate?
    @Query(value = "SELECT t FROM WebPushTemplate t where t.clientId=?1 AND t.id=?2")
    fun isTemplateExistsForThisId(clientId: Long,id: Long):List<WebPushTemplate>
    fun findByClientId(clientId: Long):List<WebPushTemplate>
    fun findByClientIdAndName(clientId: Long,name:String):List<WebPushTemplate>
}