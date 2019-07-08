package com.und.model

enum class CampaignStatus {
    PAUSED,
    RESUMED,
    CREATED,
    ERROR,
    SCHEDULE_PENDING,
    SCHEDULE_ERROR,
    DELETED,
    STOPPED,
    COMPLETED,
    FORCE_PAUSED,
    AB_COMPLETED
}

enum class LiveCampaignStatus(order:Int){
    CREATED(0),
    PAUSED(1),
    RESUMED(2),
//    ERROR,
//    SCHEDULE_PENDING,
//    SCHEDULE_ERROR,

    STOPPED(3),
    COMPLETED(4),
    DELETED(5)
//    FORCE_PAUSED
}