package com.und.model.mongo


class AnalyticFcmMessage(
        var id: String? = null,
        var clientId: Long,
        var campaignId: Long,
        var templateId: Long,
        var userId: String?=null,
        var serviceProvider: String
)