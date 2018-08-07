package com.und.service

import com.und.repository.mongo.EventRepositoryImp
import org.springframework.beans.factory.annotation.Autowired

interface EventService {

    fun getTotalEventToday():Long
    fun getUserWithMaxEvent():String

}