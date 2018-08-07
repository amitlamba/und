package com.und.service

import com.und.model.jpa.Client
import org.springframework.stereotype.Service

interface ClientService  {

    fun getClientByClientId(clientId:Long): Client
    fun getClientCount():Long
    fun getNewClient():Long
}