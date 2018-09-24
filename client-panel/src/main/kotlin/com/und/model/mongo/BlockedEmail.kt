package com.und.model.mongo

import com.und.common.utils.DateUtils
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@TypeAlias("blockedemail")
@Document(collection = "blockedemail")
class BlockedEmail(
        @field: Id var id: String? = null,
        var clientId: Long? = null,
        val history: List<BlockHistory>
)


data class BlockHistory(val email:String, val creationTime: Date = DateUtils.nowInUTC(), val message: String)