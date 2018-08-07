package com.und.service

import com.und.repository.mongo.EventRepositoryImp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EventServiceImp :EventService{

    @Autowired
    lateinit var eventRepository:EventRepositoryImp

    override fun getTotalEventToday(): Long {
        return eventRepository.getTotalEventToday()
    }

    override fun getUserWithMaxEvent(): String {
        return eventRepository.getUserWithMaxEvent()
    }
}