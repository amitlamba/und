package com.und.fcmpush.service

import com.und.model.utils.FcmMessage
import org.springframework.stereotype.Service

@Service
interface FcmService {

    fun sendMessage(message: FcmMessage)
}