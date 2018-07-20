package com.und.service

import com.und.model.mongo.SmsStatus
import com.und.model.utils.ServiceProviderCredentials
import com.und.model.utils.Sms
import com.und.model.utils.SmsSNSConfig
import com.und.repository.jpa.ClientSettingsRepository
import com.und.utils.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedWriter
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

@Service
class SmsService {

    companion object {
        protected val logger = loggerFor(EmailService::class.java)
    }


    @Autowired
    private lateinit var serviceProviderCredentialsService: ServiceProviderCredentialsService

    @Autowired
    private lateinit var clientSettingsRepository: ClientSettingsRepository

    @Autowired
    private lateinit var smsHelperService: SmsHelperService

    @Autowired
    private lateinit var smsSendService: SmsSendService

    private var wspCredsMap: MutableMap<Long, ServiceProviderCredentials> = mutableMapOf()


     fun sendSms(sms: Sms){
        val smsToSend = smsHelperService.updateBody(sms)
        val mongoSmsId = smsHelperService.saveSmsInMongo(smsToSend, SmsStatus.NOT_SENT)
        //FIXME: cache the findByClientID clientSettings
        val clientSettings = clientSettingsRepository.findByClientID(smsToSend.clientID)
        //emailToSend.emailBody = emailHelperService.trackAllURLs(emailToSend.emailBody!!, emailToSend.clientID, mongoEmailId)
        sendSmsWithoutTracking(smsToSend)
        smsHelperService.updateSmsStatus(mongoSmsId, SmsStatus.SENT, smsToSend.clientID)
    }

    fun sendSmsWithoutTracking(sms: Sms){
            val serviceProviderCredential = serviceProviderCredentials(sms)
            sendSms(serviceProviderCredential,sms)

    }

    private fun sendSms(serviceProviderCredential: ServiceProviderCredentials,sms:Sms) {
        when (serviceProviderCredential.serviceProvider) {
                ServiceProviderCredentialsService.ServiceProvider.AWS_SNS.desc -> {
                 val smsSNSConfig = SmsSNSConfig.build(serviceProviderCredential)
                    //TODO
                    //smsSendService.sendSMSMessageBySNS()

                    /*
                    here we are going to write data in file
                            Lets start
                   */
                val fileSystem = FileSystems.getDefault()
                val path = fileSystem.getPath("smslog.txt")
                    Files.createFile(path)
                    var bufferedWriter=Files.newBufferedWriter(path)
                    bufferedWriter.write(sms.fromSmsAddress)
                    sms.toSmsAddresses.forEach { bufferedWriter.write(it) }
                    bufferedWriter.write(sms.clientID.toString())
                    bufferedWriter.write(smsSNSConfig.awsAccessKeyId)
                    bufferedWriter.write(smsSNSConfig.region.getName())
                    bufferedWriter.write(smsSNSConfig.awsSecretAccessKey)


            }
            //no method implemented for Google_FCM

//            ServiceProviderCredentialsService.ServiceProvider.GOOGLE_FCM.desc->{
//                val smsSNSConfig = SmsSNSConfig.build(serviceProviderCredential)
//                smsSendService.sendSMSMessageBySNS()
//            }






        }
    }

    private fun serviceProviderCredentials(sms: Sms): ServiceProviderCredentials {
        synchronized(sms.clientID) {
            //TODO: This code can be cached in Redis
            if (!wspCredsMap.containsKey(sms.clientID)) {
                val webServiceProviderCred = serviceProviderCredentialsService.getServiceProviderCredentials(sms)
                wspCredsMap[sms.clientID] = webServiceProviderCred
            }
        }
        return wspCredsMap[sms.clientID]!!
    }
}