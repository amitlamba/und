package com.und.service

import com.und.model.jpa.Client
import com.und.repository.jpa.ClientRepository
import com.und.web.controller.exception.CustomException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ClientServiceImp:ClientService {

    @Autowired
    lateinit var clientRepository:ClientRepository

    override fun getClientByClientId(clientId: Long): Client? {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        var client=clientRepository.findById(clientId)
        if (client.isPresent) return client.get()
        else LoggerFactory.getLogger(ClientServiceImp::class.java).info("Client with id $clientId not exists.")
        return null
    }

    override fun getClientCount(): Long {
        return clientRepository.count()
    }

    override fun getNewClient(): Long {
        return clientRepository.getNewClient()
    }

    override fun getClients(): List<Client> {
        return clientRepository.findAll()
    }
}