package com.und.service.security

import com.und.repository.jpa.AuthorityRepository
import com.und.model.jpa.security.Authority
import com.und.model.jpa.security.AuthorityName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthorityService {
    @Autowired
    lateinit var authorityRepository: AuthorityRepository

    fun findByName(name: AuthorityName): Authority? {
        return authorityRepository.findByName(name)
    }
}

