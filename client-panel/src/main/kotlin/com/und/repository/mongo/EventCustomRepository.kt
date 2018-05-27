package com.und.repository.mongo

import org.springframework.data.mongodb.core.aggregation.Aggregation

interface EventCustomRepository {
    fun usersFromEvent(query: Aggregation, clientId: Long): List<String>

}