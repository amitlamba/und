package com.und.repository.jpa

import com.und.model.jpa.WebPushTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WebPushRepository :JpaRepository<WebPushTemplate,Long>{
    fun findByClientIdAndId(clientId:Long,id:Long):WebPushTemplate
}