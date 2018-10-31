package com.und.repository.jpa

import com.und.model.jpa.AndroidTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AndroidRepository :JpaRepository<AndroidTemplate,Long>{
    fun findByClientIdAndId(clientId:Long,id:Long):AndroidTemplate
}