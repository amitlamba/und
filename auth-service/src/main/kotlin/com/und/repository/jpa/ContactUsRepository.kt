package com.und.repository.jpa

import com.und.model.jpa.ContactUs
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

@Repository
interface ContactUsRepository : JpaRepository<ContactUs, Long> {

    fun findByEmailAndDateCreated(email: String, date: LocalDateTime): Optional<ContactUs>

    @Query("SELECT * FROM contact_us WHERE email = :email AND date_modified >= :startDate AND date_modified <= :endDate", nativeQuery = true)
    fun findByEmailBetweenDates(email: String, startDate: LocalDateTime, endDate: LocalDateTime): Optional<List<ContactUs>>

}