package com.und.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.common.utils.loggerFor
import com.und.model.jpa.CampaignType
import com.und.model.jpa.Schedule
import com.und.security.utils.AuthenticationUtils
import com.und.service.*
import com.und.web.controller.exception.CustomException
import com.und.web.controller.exception.UndBusinessValidationException
import com.und.web.model.*
import com.und.web.model.ResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.ZoneId
import java.util.*
import javax.servlet.http.HttpServletRequest
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
    private lateinit var smsTempleteService: SmsTemplateService


    @Autowired
    private lateinit var emailTempleteService: EmailTemplateService
    @Autowired
    private lateinit var androidService:AndroidService
    @Autowired
    private lateinit var webPushService: WebPushService

    @Autowired
    lateinit var campaignService: CampaignService

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/list/all"])
    fun getCampaigns(): List<Campaign> {
        return campaignService.getCampaigns()
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/error/{campaignId}"])
    fun scheduleError(@PathVariable("campaignId") campaignId: Long): ResponseEntity<String> {

        logger.info("campaign schedule error fetching for campaignId $campaignId")
        val clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")

        val message = campaignService.getScheduleError(campaignId, clientId)

        logger.info("campaign schedule error message for campaignId : $campaignId is ${message.isPresent}")

        return message.map { ResponseEntity(message.get(), HttpStatus.OK) }
                .orElse(ResponseEntity(String(), HttpStatus.EXPECTATION_FAILED))


    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/save"])
    fun saveCampaign(@Valid @RequestBody campaign: Campaign): ResponseEntity<Campaign> {
        logger.info("campaign save request initiated ${campaign.name}")
        val clientId = AuthenticationUtils.clientID
        val templateId = campaign.templateID
        if (clientId != null && templateId!=null) {
            val template = when(campaign.campaignType) {
                CampaignType.EMAIL -> emailTempleteService.getEmailTemplate(templateId)
                CampaignType.SMS -> listOf(smsTempleteService.getSmsTemplateById(templateId))
                CampaignType.PUSH_ANDROID -> androidService.getAndroidTemplatesById(clientId,templateId)
                CampaignType.PUSH_IOS->{throw CustomException("This Service Not present")}
                CampaignType.PUSH_WEB -> webPushService.findExistsTemplate(templateId)
            }

            if (template.isNotEmpty()) {
                //TODO we always return campaign here
                val persistedCampaign = campaignService.save(campaign)
                if(persistedCampaign.id!=null){
                logger.info("campaign saved with name ${campaign.name}")
                return ResponseEntity(persistedCampaign, HttpStatus.CREATED)
                }
            }else{
                logger.info("campaign not saved with name ${campaign.name}")
                throw CustomException("template with id $templateId not exist")
            }
        }
        logger.info("campaign not saved with name ${campaign.name} and template $templateId")
        return ResponseEntity(campaign,HttpStatus.EXPECTATION_FAILED)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/send/testcampaign")
    fun runTestCampaign(@RequestBody testCampaign: TestCampaign): Response {
        val clientId =AuthenticationUtils.clientID?: throw AccessDeniedException("Access Denied.")
        campaignService.runTestCampaign(clientId,testCampaign)
        return Response(ResponseStatus.SUCCESS)
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/pause/livecampaign/{campaignId}")
    fun pauseLiveCampaign(@PathVariable("campaignId",required = true)campaignId: Long){
        val clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("Access Denied.")
        campaignService.pauseLiveCampaign(clientId,campaignId)
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/resume/livecampaign/{campaignId}")
    fun resumeLiveCampaign(@PathVariable("campaignId",required = true)campaignId: Long){
        val clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("Access Denied.")
        campaignService.resumeLiveCampaign(clientId,campaignId)
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/stop/livecampaign/{campaignId}")
    fun stopLiveCampaign(@PathVariable("campaignId",required = true)campaignId: Long){
        val clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("Access Denied.")
        campaignService.stopLiveCampaign(clientId,campaignId)
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/livecampaign/{campaignId}")
    fun deleteLiveCampaign(@PathVariable("campaignId",required = true)campaignId: Long){
        val clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("Access Denied.")
        campaignService.deleteLiveCampaign(clientId,campaignId)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/updateschedule/{campaignId}"])
    fun updateschedule(@Valid @RequestBody schedule: Schedule, @PathVariable("campaignId") campaignId: Long): ResponseEntity<HttpStatus> {
        logger.info("schedule update request initiated for $campaignId")
        val clientId = AuthenticationUtils.clientID
        if (clientId != null) {
            campaignService.updateSchedule(campaignId, clientId, schedule)
            return ResponseEntity(HttpStatus.ACCEPTED)
        }
        logger.info("campaign schedule for id $campaignId not accepted ")
        return ResponseEntity(HttpStatus.NOT_ACCEPTABLE)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/update"])
    fun updateCampaign(@Valid @RequestBody campaign: Campaign): ResponseEntity<Campaign> {
        logger.info("campaign update request inititated ${campaign.name}")
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

//    @GetMapping(value = ["/email/faddrandsrp"])
//    fun getClientFromAddressAndSrp1():ClientFromAddressAndSrp{
//        val clientId=AuthenticationUtils.clientID?:throw AccessDeniedException("Access denied")
//        return campaignService.getClientFromAddressAndSrp(clientId)
//    }

    @GetMapping(value = ["/email/faddrandsrp"])
    fun getClientFromAddressAndSrp(): List<ClientEmailSettIdFromAddrSrp> {
        val clientId=AuthenticationUtils.clientID?:throw AccessDeniedException("Access denied")
        return campaignService.getClientFromAddressAndSrp(clientId)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value=["/save/ab"])
    fun saveAbCampaign(@Valid @RequestBody abCampaign: AbCampaign):Response{
        val clientID=AuthenticationUtils.clientID?: throw AccessDeniedException("Access Denied.")
        //TODO check template and segment exists.
        campaignService.saveAbCampaign(abCampaign,clientID)
        return Response(status = ResponseStatus.SUCCESS,message = "Ab Campaign Save Successfully.")
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/run/manually/{campaignId}"])
    fun triggerCampaignManually(@PathVariable(value = "campaignId",required = true) campaignId: Long):Response{
        val clientID=AuthenticationUtils.clientID?: throw AccessDeniedException("Access Denied.")
        val timeZone=AuthenticationUtils.principal.timeZoneId
        campaignService.runManualCampaign(campaignId,clientID,ZoneId.of(timeZone))
        return Response(status = ResponseStatus.SUCCESS)
    }

    @GetMapping("/testpaging")
    fun testpging(@RequestParam("page")page:Int):List<com.und.model.jpa.Campaign>{
        return campaignService.testPaging(page)
    }

}