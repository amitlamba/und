package com.und.repository.mongo

import org.springframework.stereotype.Repository

@Repository
interface EventUserRepo {
    fun totalEventUserToday(): Long
}