package com.und.repository.mongo

import com.und.model.mongo.eventapi.EventUser

interface EventUserCustomRepository {
    fun findUserById(id:String, clientId:Long): EventUser?
}