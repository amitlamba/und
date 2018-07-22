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
        looger.info(serviceProviderCredentials.serviceProviderType)
        looger.info(serviceProviderCredentials.serviceProvider)
        looger.info(serviceProviderCredentials.credentialsMap.toString())
        looger.info(sms.fromSmsAddress)
        looger.info(sms.toSmsAddresses)
        looger.info(sms.smsBody)
        return true
    }

}
class AWS_SNSSmsService:SmsServer{
    companion object {
        var looger=loggerFor(TwilioSmsSendService::class.java)
    }
    override fun sendSms(serviceProviderCredentials: ServiceProviderCredentials, sms: Sms): Boolean {
        //write sms send code
        looger.info(serviceProviderCredentials.serviceProviderType)
        looger.info(serviceProviderCredentials.serviceProvider)
        looger.info(serviceProviderCredentials.credentialsMap.toString())
        looger.info(sms.fromSmsAddress)
        looger.info(sms.toSmsAddresses)
        looger.info(sms.smsBody)
        return true
    }

}