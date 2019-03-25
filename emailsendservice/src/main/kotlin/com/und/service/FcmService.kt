package com.und.service

import com.und.model.utils.FcmMessage

interface FcmService {

    fun sendMessage(message: FcmMessage)
}