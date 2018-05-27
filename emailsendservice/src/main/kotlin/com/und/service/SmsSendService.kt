package com.und.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import org.springframework.stereotype.Service


@Service
class SmsSendService {

    fun sendSMSMessageBySNS(message: String, phoneNumber: String, smsAttributes: Map<String, MessageAttributeValue>? = null,
                            senderID: String, smsType: String, maxPrice: String) {
        val credentialsProvider: AWSCredentialsProvider = AWSStaticCredentialsProvider(BasicAWSCredentials("awsAccessKeyId", "awsSecretAccessKey"))
        val snsClient = AmazonSNSClientBuilder.standard()
                .withRegion(Regions.DEFAULT_REGION)
                .withCredentials(credentialsProvider)
                .build()
        val message = "My SMS message for Amit from UserNDot"
        val phoneNumber = "+918882774104"
        val smsAttributes = setSnsSmsAttributes(senderID, smsType, maxPrice)
        sendSnsSmsMessage(snsClient, message, phoneNumber, smsAttributes)
    }

    private fun setSnsSmsAttributes(senderID: String, smsType: String, maxPrice: String): Map<String, MessageAttributeValue> {
        var smsAttributes: Map<String, MessageAttributeValue> = mutableMapOf()

        smsAttributes.plus(Pair("AWS.SNS.SMS.SenderID", MessageAttributeValue()
                .withStringValue(senderID) //The sender ID shown on the device.
                .withDataType("String")))
        smsAttributes.plus(Pair("AWS.SNS.SMS.MaxPrice", MessageAttributeValue()
                .withStringValue(maxPrice) //Sets the max price to 0.50 USD.
                .withDataType("Number")))
        smsAttributes.plus(Pair("AWS.SNS.SMS.SMSType", MessageAttributeValue()
                .withStringValue(smsType) //Sets the type to promotional."Promotional"
                .withDataType("String")))

        return smsAttributes
    }

    private fun sendSnsSmsMessage(snsClient: AmazonSNS, message: String,
                                  phoneNumber: String, smsAttributes: Map<String, MessageAttributeValue>) {
        val result = snsClient.publish(PublishRequest()
                .withMessage(message)
                .withPhoneNumber(phoneNumber)
                .withMessageAttributes(smsAttributes))
        println(result) // Prints the message ID.
    }
}