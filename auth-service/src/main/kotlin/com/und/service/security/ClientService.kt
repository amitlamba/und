package com.und.service.security

import com.und.repository.jpa.ClientRepository
import com.und.model.jpa.security.Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ClientService {

    @Autowired
    lateinit var clientRepository: ClientRepository

    fun save(client: Client): Client {
        return  clientRepository.save(client)
    }

    fun findByEmail(email: String): Client? {
        return clientRepository.findByEmail(email)
    }

    fun findById(id:Long): Client {
        val clientOption =  clientRepository.findById(id)
        return if(clientOption.isPresent) clientOption.get() else Client()
    }

    fun updateClient(client: Client):Boolean {
        val clientId = client.id
        if(clientId != null) {
            val savedClient = findById(clientId)
            savedClient.firstname = client.firstname?:savedClient.firstname
            savedClient.lastname = client.lastname?:savedClient.lastname
            savedClient.phone = client.phone?:savedClient.phone
            savedClient.address = client.address?:savedClient.address
            clientRepository.save(savedClient)
        }
        return clientId!=null
    }
}