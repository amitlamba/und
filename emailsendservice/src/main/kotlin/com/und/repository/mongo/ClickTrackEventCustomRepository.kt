package com.und.repository.mongo

import com.und.model.mongo.ClickTrackEvent

interface ClickTrackEventCustomRepository {
    fun saveEvent(save: ClickTrackEvent): ClickTrackEvent
}