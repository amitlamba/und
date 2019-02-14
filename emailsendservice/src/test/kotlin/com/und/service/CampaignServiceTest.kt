package com.und.service

import com.und.repository.jpa.CampaignRepository
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(value = SpringRunner::class)
        @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DataJpaTest
class CampaignServiceTest {

    @Autowired
    lateinit var campaignRepository:CampaignRepository

    var campaignId:Long=1004
    var clientId:Long=3
    @Before
    fun setUp() {

    }

    @Test
    fun executeCampaign() {
            var campaign=campaignRepository.findById(campaignId)
            print("$campaign.campaignId,$campaign.campaignType")
    }

    @Test
    fun getUsersData() {
    }
}