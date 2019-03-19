package com.und.service

import com.und.model.utils.Email

interface CommonEmailService {
    fun sendEmail(email: Email)
}