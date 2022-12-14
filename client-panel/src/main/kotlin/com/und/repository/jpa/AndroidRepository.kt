package com.und.repository.jpa

import com.und.model.jpa.AndroidTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AndroidRepository:JpaRepository<AndroidTemplate,Long> {
    fun findByClientIdAndName(clientId:Long,name:String):List<AndroidTemplate>
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN 'true' ELSE 'false' END FROM AndroidTemplate a WHERE a.clientId=?1 AND a.name = ?2")
    fun existsByClientIdAndName(clientId: Long,name: String):Boolean
    fun findByClientId(clientId: Long):List<AndroidTemplate>
    fun findByClientIdAndId(clientId: Long,id:Long):AndroidTemplate?
    @Query("SELECT a FROM AndroidTemplate a WHERE a.clientId=?1 AND a.id = ?2")
    fun isExistsByClientIdAndId(clientId: Long,id: Long):List<AndroidTemplate>
}