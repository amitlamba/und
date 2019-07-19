package com.und.campaign.repository.jpa

import com.und.model.jpa.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ClientRepository:JpaRepository<Client,Long> {

    @Query("Select name,email,phone,firstname,lastname,id from client where id = :clientId",nativeQuery = true)
    override fun findById(clientId:Long):Optional<Client>
}