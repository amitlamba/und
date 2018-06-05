package com.und.repository.jpa

import com.und.model.jpa.ContactUs
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface ContactUsRepository : JpaRepository<ContactUs, Long> {

    fun findByEmailAndDateCreated(email: String, date: LocalDateTime): Optional<ContactUs>


}