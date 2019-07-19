package com.und.service

import com.und.campaign.service.TestCampaignService
import com.und.common.utils.ReplaceNullPropertyOfEventUser
import com.und.sms.utility.SmsServiceUtility
import com.und.model.utils.Sms
import com.und.sms.service.SmsHelperService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service("testsmsservice")
class TestSmsService:CommonSmsService {
    companion object {
        val logger=LoggerFactory.getLogger(TestCampaignService::class.java)
    }

    @Autowired
    lateinit var templateContentCreationService: TemplateContentCreationService

    @Autowired
    lateinit var smsServiceUtility: SmsServiceUtility

    @Autowired
    lateinit var smsService: SmsHelperService

    override fun sendSms(sms: Sms) {

        val model = sms.data

        val variable = getSmsTemplateVariable(sms.smsBody?:"")
        val user = ReplaceNullPropertyOfEventUser.replaceNullPropertyOfEventUser(sms.eventUser,variable)
        user?.let {
            model["user"] = it
        }

        var smsToSend  = sms.copy()
        smsToSend.smsBody=templateContentCreationService.getTestSmsTemplateBody(sms.smsBody?:"",model)

        val response = smsServiceUtility.sendSmsWithoutTracking(smsToSend)
        if(response.status!=200) logger.info("Error in sending test campaign for client ${sms.clientID}")
    }

    fun getSmsTemplateVariable(smsbody:String):Set<String>{
        val listOfVariable = mutableSetOf<String>()

            val body=smsbody
            val regex="(\\$\\{.*?\\})"
            val pattern = Pattern.compile(regex)

            val bodyMatcher = pattern.matcher(body)
            var i=0
            while (bodyMatcher.find()){
                listOfVariable.add(bodyMatcher.group(i+1))
            }
        return listOfVariable
    }
}