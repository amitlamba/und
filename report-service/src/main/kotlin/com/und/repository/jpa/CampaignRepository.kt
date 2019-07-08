package com.und.repository.jpa

import com.und.model.jpa.Campaign
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface CampaignRepository : JpaRepository<Campaign, Long>, PagingAndSortingRepository<Campaign, Long> {
    fun findByIdAndClientID(id: Long, clientID: Long): Optional<Campaign>
    fun findByClientIDAndSegmentationID(clientId:Long, segmetationId:Long):List<Campaign>
}