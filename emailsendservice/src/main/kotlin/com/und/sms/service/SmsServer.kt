package com.und.sms.service

import com.und.model.utils.ServiceProviderCredentials
import com.und.model.utils.Sms
import com.und.utils.loggerFor
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import org.slf4j.Logger

interface SmsServer {
    fun sendSms(serviceProviderCredentials: ServiceProviderCredentials, sms: Sms): Boolean
}

object TwilioSmsSendService : SmsServer {
    var looger: Logger = loggerFor(TwilioSmsSendService::class.java)

    override fun sendSms(serviceProviderCredentials: ServiceProviderCredentials, sms: Sms): Boolean {

        Twilio.init("sid", "accounttoken")
        Message.creator(PhoneNumber("+9178388540240"), PhoneNumber("+9178388540240"), "test message")
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

object AWS_SNSSmsService : SmsServer {
    var looger: Logger = loggerFor(AWS_SNSSmsService::class.java)

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

    object Example {
        // Find your Account Sid and Token at twilio.com/user/account
        val ACCOUNT_SID = "ACee7cb87331f38645aaefd0bf42cbff79"
        val AUTH_TOKEN = "d93ff2f90bb18853fea41c8915fc647a"

//        @JvmStatic
//        fun main(args: Array<String>) {
//            Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
//
//            val message = Message.creator(
//                    PhoneNumber("+18507506286"),
//                    PhoneNumber("+917838540240"),
//                    "This is the ship that made the Kessel Run in fourteen parsecs?").create()
//
//            println(message.sid)
//        }
    }


}

object ExotelService : SmsServer {
    var looger: Logger = loggerFor(ExotelService::class.java)

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

    object Example {
        // Find your Account Sid and Token at twilio.com/user/account
        val ACCOUNT_SID = "ACee7cb87331f38645aaefd0bf42cbff79"
        val AUTH_TOKEN = "d93ff2f90bb18853fea41c8915fc647a"

//        @JvmStatic
//        fun main(args: Array<String>) {
//            Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
//
//            val message = Message.creator(
//                    PhoneNumber("+18507506286"),
//                    PhoneNumber("+917838540240"),
//                    "This is the ship that made the Kessel Run in fourteen parsecs?").create()
//
//            println(message.sid)
//        }
    }
}