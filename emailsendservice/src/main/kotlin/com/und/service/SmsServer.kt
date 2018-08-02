package com.und.service

import com.und.model.utils.ServiceProviderCredentials
import com.und.model.utils.Sms
import com.und.utils.loggerFor

interface SmsServer {
    fun sendSms(serviceProviderCredentials: ServiceProviderCredentials,sms: Sms):Boolean
}

class TwilioSmsSendService:SmsServer{
    companion object {
        var looger=loggerFor(TwilioSmsSendService::class.java)
    }
    override fun sendSms(serviceProviderCredentials: ServiceProviderCredentials, sms: Sms): Boolean {
        //write sms send code
        looger.info("Service provider type  ${serviceProviderCredentials.serviceProviderType}")
        looger.info("Service provider  ${serviceProviderCredentials.serviceProvider}")
        looger.info("Credential  ${serviceProviderCredentials.credentialsMap.toString()}")
        looger.info("From  ${sms.fromSmsAddress}")
        looger.info("To  ${sms.toSmsAddresses}")
        looger.info("Body  ${sms.smsBody}")
        return true
    }

}
class AWS_SNSSmsService:SmsServer{
    companion object {
        var looger=loggerFor(TwilioSmsSendService::class.java)
    }
    override fun sendSms(serviceProviderCredentials: ServiceProviderCredentials, sms: Sms): Boolean {
        //write sms send code
        looger.info("Service provider type  ${serviceProviderCredentials.serviceProviderType}")
        looger.info("Service provider  ${serviceProviderCredentials.serviceProvider}")
        looger.info("Credential  ${serviceProviderCredentials.credentialsMap.toString()}")
        looger.info("From  ${sms.fromSmsAddress}")
        looger.info("To  ${sms.toSmsAddresses}")
        looger.info("Body  ${sms.smsBody}")
        return true
    }

}