package com.und.repository.jpa

import com.und.model.jpa.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.persistence.Id

@Repository
interface ClientRepository: JpaRepository<Client, Long> {

    override fun count(): Long
    @Query("Select count(c) from Client c where c.dateCreated=now()")
    fun getNewClient():Long

    fun findById(id: Id):Client

}