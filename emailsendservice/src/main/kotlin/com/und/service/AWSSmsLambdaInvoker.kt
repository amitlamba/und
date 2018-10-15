package com.und.report.service

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.*
import com.fasterxml.jackson.databind.ObjectMapper

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.util.UriEncoder


@Component
class AWSSmsLambdaInvoker {

    @Value("\${und.lambda.sms.accessKey}")
    private lateinit var AWS_ACCESS_KEY: String

    @Value("\${und.lambda.sms.secretKey}")
    private lateinit var AWS_SECRET_KEY: String

    @Value("\${und.lambda.sms.region}")
    private lateinit var AWS_REGION: String

    @Value("\${und.lambda.sms.functionName}")
    private lateinit var AWS_LAMBDA_FUNCTION: String


    companion object {
        val logger: Logger = LoggerFactory.getLogger(AWSSmsLambdaInvoker::class.java)
    }

    @Autowired
    lateinit var mapper: ObjectMapper

    fun sendSms(smsData: SmsData): Response {

        val body = UriEncoder.encode(smsData.body)
        smsData.body=body
        val region = Regions.fromName(AWS_REGION)

        val basicAWSCredentials = BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)

        val awsLambdaClient = AWSLambdaClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials)).withRegion(region).build()

        val invokeRequest = InvokeRequest().withFunctionName(AWS_LAMBDA_FUNCTION).withInvocationType(InvocationType.RequestResponse.toString())
                .withPayload(mapper.writeValueAsString(smsData))

        return try {
            val invokeResult = awsLambdaClient.invoke(invokeRequest)

            val result = String(invokeResult.payload.array())
            mapper.readValue(result, Response::class.java)
        } catch (e: InvalidParameterValueException) {
            logger.error("couldn't send message an error occurred", e)
            return Response(400, "Invalid Parameters")
        } catch (e: EC2ThrottledException) {
            logger.error("couldn't send message an error occurred", e)
            return Response(400, "throttle error")
        } catch (e: TooManyRequestsException) {
            logger.error("couldn't send message an error occurred", e)
            return Response(400, "Too many requests")
        } catch (e: EC2AccessDeniedException) {
            logger.error("couldn't send message an error occurred", e)
            return Response(400, "AWs lambda access denied")
        } catch (e: Exception) {
            logger.error("couldn't send message an error occurred", e)
            return Response(400, "Invalid Parameters")
        }


    }


}

class Response() {
    constructor(status: Int, message: String) : this() {
        this.status = status
        this.message = message
    }

    var status: Int? = null
    var message: String? = null
}

enum class SMSServiceProvider {
    EXOTEL
}

class SmsData(
        var sid: String,
        var token: String,
        var from: String,
        var to: String,
        var body: String
)

class SmsRequest(
        var sid: String,
        var token: String,
        var from: String,
        var serviceProvider: SMSServiceProvider,
        var exotelAccess: SmsExotelInput?
)

class SmsExotelInput(var sid: String, var token: String)

//fun main(args: Array<String>) {
//
//    val smsRequest = SmsRequest(
//            "09513886363",
//            "7838540240",
//            "HelloWorld",
//            SMSServiceProvider.EXOTEL,
//            SmsExotelInput("shiv53", "54743a3bab3609e08473ace008df1d64bcf998cd")
//
//    )
//    val lambdaInvoker = AWSSmsLambdaInvoker()
///*    lambdaInvoker.AWS_ACCESS_KEY = "AKIAIEEV7Q6MG5DLL52Q"
//    lambdaInvoker.AWS_SECRET_KEY = "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"
//    lambdaInvoker.AWS_REGION = "ap-south-1"
//    lambdaInvoker.AWS_LAMBDA_FUNCTION = "exotel"*/
//    val smsData = SmsData(
//            "shiv53",
//            "54743a3bab3609e08473ace008df1d64bcf998cd",
//            "09513886363",
//            "7838540240",
//            "HelloWorld"
//    )
//
//    val response = lambdaInvoker.sendSms(smsData)
//    print("${response.status} ${response.message}")
//}


