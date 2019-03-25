package com.und.service

import com.und.model.utils.Email
import org.springframework.stereotype.Service

@Service
interface CommonEmailService {
    fun sendEmail(email: Email)
}