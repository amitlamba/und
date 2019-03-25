package com.und.service

import com.und.model.utils.Sms

interface CommonSmsService {
    fun sendSms(sms: Sms)
}