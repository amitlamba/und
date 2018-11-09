package com.und.repository.jpa

import com.und.model.jpa.Action
import org.springframework.data.jpa.repository.JpaRepository

interface AndroidActionRepository:JpaRepository<Action,Long> {
    fun findByClientId(clientId:Long):List<Action>
}