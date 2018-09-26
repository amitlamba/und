package com.und.model.utils

import com.amazonaws.regions.Regions
import com.und.exception.EmailError
import com.und.exception.EmailFailureException
import com.und.factory.Security
import com.und.model.jpa.Status
import java.time.LocalDateTime

class ServiceProviderCredentials {
    var id: Long? = null
    var clientID: Long = 0
    var appuserID: Long? = null
    lateinit var serviceProviderType: String
    lateinit var serviceProvider: String
    lateinit var dateCreated: LocalDateTime
    lateinit var dateModified: LocalDateTime
    lateinit var status: Status
    var credentialsMap: HashMap<String, String> = HashMap()

/*    fun getServiceProvider(): Any? {
        when (this.serviceProviderType) {
            "Email Service Provider" -> {
                when (this.serviceProvider) {
                    "SMTP" -> return EmailSMTPConfig.build(this)
                    "AWS - Simple Email Service (API)" -> return EmailSMTPConfig.build(this)
                    "AWS - Simple Email Service (SMTP)" -> return EmailSESConfig.build(this)
                }
            }
            "SMS Service Provider" -> {
                when (this.serviceProvider) {
                    "AWS - Simple Notification Service" -> return SmsSNSConfig.build(this)
                }
            }
            "Notification Service Provider" -> {
                when (this.serviceProvider) {
                    "Google - FCM" -> return GoogleFCMConfig.build(this)
                    "Google - GCM" -> return GoogleFCMConfig.build(this)
                }
            }
        }
        return null
    }*/
}


data class EmailSESConfig(
        var serviceProviderCredentialsId: Long?,
        val clientID: Long,
        val region: Regions,
        val awsAccessKeyId: String,
        val awsSecretAccessKey: String
) {
    companion object {
        fun build(serviceProviderCredentials: ServiceProviderCredentials): EmailSESConfig {
            val credentialMap = serviceProviderCredentials.credentialsMap
            val region = credentialMap["AWS_REGION"]
            val accessKeyId = credentialMap["AWS_ACCESS_KEY_ID"]
            val secretAccessKey = credentialMap["AWS_SECRET_ACCESS_KEY"]
            val clientId = serviceProviderCredentials.clientID
            return if (region == null || accessKeyId == null || secretAccessKey == null) {
                val error = EmailError()
                with(error) {
                    clientid = serviceProviderCredentials.clientID
                    causeMessage = "details for email setting are incorrect region : $region , accesKeyId : $accessKeyId , secretAccesKey :$secretAccessKey  "
                    failedSettingId = serviceProviderCredentials.id
                    failureType = EmailError.FailureType.CONNECTION
                }
                throw EmailFailureException("details for email setting are incorrect region : $region , accesKeyId : $accessKeyId , secretAccesKey :$secretAccessKey  ", error)
            } else {
                EmailSESConfig(
                        serviceProviderCredentials.id,
                        clientId,
                        Regions.fromName(region),
                        accessKeyId,
                        secretAccessKey
                )
            }
        }
    }
}

data class EmailSMTPConfig(
        var serviceProviderCredentialsId: Long?,
        var clientID: Long,
        var HOST: String,
        var PORT: Int,
        var SMTP_USERNAME: String,
        var SMTP_PASSWORD: String,
        var security:Security,
        var CONFIGSET: String? = null
) {
    companion object {
        fun build(serviceProviderCredentials: ServiceProviderCredentials): EmailSMTPConfig {
            val host = serviceProviderCredentials.credentialsMap["url"]
            val port = serviceProviderCredentials.credentialsMap["port"]
            val username = serviceProviderCredentials.credentialsMap["username"]
            val password = serviceProviderCredentials.credentialsMap["password"]
            val security = serviceProviderCredentials.credentialsMap["security"]?.let {security->Security.valueOf(security)}?:Security.STARTTLS
            return if (host == null || port == null || username == null || password == null) {
                val error = EmailError()
                with(error) {
                    clientid = serviceProviderCredentials.clientID
                    causeMessage = "details for email setting are incorrect host : $host , port : $port , username :$username , password : $password "
                    failedSettingId = serviceProviderCredentials.id
                    failureType = EmailError.FailureType.CONNECTION
                }
                throw EmailFailureException("details for email setting are incorrect host : $host , port : $port , username :$username , password : $password ", error)
            } else {
                EmailSMTPConfig(
                        serviceProviderCredentials.id,
                        serviceProviderCredentials.clientID,
                        host,
                        port.toInt(),
                        username,
                        password,
                        security=security
                )
            }
        }
    }
}

data class SmsSNSConfig(
        var serviceProviderCredentialsId: Long?,
        val clientID: Long,
        val region: Regions,
        val awsAccessKeyId: String,
        val awsSecretAccessKey: String
) {
    companion object {
        fun build(serviceProviderCredentials: ServiceProviderCredentials): SmsSNSConfig {
            val host = serviceProviderCredentials.credentialsMap["url"]
            val port = serviceProviderCredentials.credentialsMap["port"]
            val username = serviceProviderCredentials.credentialsMap["username"]
            val password = serviceProviderCredentials.credentialsMap["password"]
            return null!!
        }
    }
}

data class GoogleFCMConfig(
        var serviceProviderCredentialsId: Long?,
        val clientID: Long,
        val serverKey: String
) {
    companion object {
        fun build(serviceProviderCredentials: ServiceProviderCredentials): GoogleFCMConfig {
            val host = serviceProviderCredentials.credentialsMap["url"]
            val port = serviceProviderCredentials.credentialsMap["port"]
            val username = serviceProviderCredentials.credentialsMap["username"]
            val password = serviceProviderCredentials.credentialsMap["password"]
            return null!!
        }
    }
}
