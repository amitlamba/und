package com.und.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.common.utils.loggerFor
import com.und.exception.UndBusinessValidationException
import com.und.model.jpa.CampaignType
import com.und.model.jpa.EmailCampaign
import com.und.model.jpa.Schedule
import com.und.model.jpa.SmsCampaign
import com.und.security.utils.AuthenticationUtils
import com.und.service.CampaignService
import com.und.web.model.Campaign
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping("/campaign")
class CampaignController {

    companion object {

        protected val logger = loggerFor(CampaignController::class.java)
    }

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var campaignService: CampaignService

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["list/all"])
    fun getCampaigns(@RequestParam(value = "id", required = false) id: Long? = null): List<Campaign> {
        return campaignService.getCampaigns()
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/save"])
    fun saveCampaign(@Valid @RequestBody campaign: Campaign): ResponseEntity<Campaign> {
        logger.info("campaign save request inititated ${campaign.name}")
        val clientId = AuthenticationUtils.clientID
        if (clientId != null) {
            val persistedCampaign = campaignService.save(campaign)
            return ResponseEntity(persistedCampaign, HttpStatus.CREATED)
        }
        logger.info("campaign saved with name ${campaign.name}")
        return ResponseEntity(campaign, HttpStatus.EXPECTATION_FAILED)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = ["/pause/{campaignId}"])
    fun pauseCampaign(@PathVariable campaignId: Long): ResponseEntity<*> {
        return takeAction(campaignId, campaignService::pause)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = ["/resume/{campaignId}"])
    fun resumeCampaign(@PathVariable campaignId: Long): ResponseEntity<*> {
        return takeAction(campaignId, campaignService::resume)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = ["/stop/{campaignId}"])
    fun stopCampaign(@PathVariable campaignId: Long): ResponseEntity<*> {
        return takeAction(campaignId, campaignService::stop)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = ["/delete/{campaignId}"])
    fun deleteCampaign(@PathVariable campaignId: Long): ResponseEntity<*> {
        return takeAction(campaignId, campaignService::delete)
    }

    private fun takeAction(campaignId: Long, perform: (Long) -> Long?): ResponseEntity<out Long?> {
        val clientId = AuthenticationUtils.clientID
        if (clientId != null) {
            val status = perform(campaignId)
            return ResponseEntity(status, HttpStatus.OK)
        }

        return ResponseEntity(campaignId, HttpStatus.EXPECTATION_FAILED)
    }


}