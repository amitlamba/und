package com.und.repository.mongo

import com.und.model.mongo.BlockedEmail

interface BlockedEmailCustomRepository  {



    fun appendHistory(clientId: Long, blockedEmail: BlockedEmail)

}



