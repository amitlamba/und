package com.und.web.model

class CampaignReached {
    var delivered: Long = 0
    var failed: Long = 0
    var read: Long = 0
    var interacted: Long = 0

}
data class CampaignReachedResult(
    var id:String,
    var count:Long
)