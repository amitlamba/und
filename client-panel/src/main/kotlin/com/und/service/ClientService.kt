package com.und.service

import com.und.model.jpa.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

interface ClientService  {

    fun getClientByClientId(clientId:Long): Client?
    fun getClientCount():Long
    fun getNewClient():Long
    fun getClients():List<Client>
}