package com.und.service

import com.und.model.jpa.Client
import com.und.repository.jpa.ClientRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ClientServiceImp:ClientService {

    @Autowired
    lateinit var clientRepository:ClientRepository

    override fun getClientByClientId(clientId: Long): Client {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getClientCount(): Long {
        return clientRepository.count()
    }

    override fun getNewClient(): Long {
        return clientRepository.getNewClient()
    }
}