package com.und.service

import org.junit.Ignore
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@Ignore
@RunWith(SpringRunner::class)
@SpringBootTest
class CampaignServiceTest {

    @Autowired
    private lateinit var campaignService: CampaignService

    @Autowired
    private lateinit var emailTemplateService: EmailTemplateService


}