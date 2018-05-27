package com.und.model.utils.fcm

data class FcmResponse (
    val multicast_id: Long,
    val success: Int,
    val failure: Int,
    val canonical_ids: Int,
    val results: List<FcmResult>
)

data class FcmResult(
        val registration_id: String?,
        val message_id: String?,
        val error: String?
)