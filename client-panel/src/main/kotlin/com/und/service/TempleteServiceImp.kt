package com.und.service

import com.und.repository.jpa.TemplateRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TempleteServiceImp:TempleteService {

    @Autowired
    lateinit var templeteRepository: TemplateRepository

    override fun getCountOfTemplete(): Long {
        return templeteRepository.count()
    }

    override fun getNewTempletes(): Long {
        return templeteRepository.getNewTempletes()
    }
}