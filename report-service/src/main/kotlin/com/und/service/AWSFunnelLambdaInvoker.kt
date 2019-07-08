package com.und.report.service

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.und.model.FunnelData
import com.und.report.web.model.FunnelReport
import com.und.util.loggerFor
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AWSFunnelLambdaInvoker {

    @Value("\${und.lambda.funnel.accessKey}")
    private lateinit var AWS_ACCESS_KEY: String

    @Value("\${und.lambda.funnel.secretKey}")
    private lateinit var AWS_SECRET_KEY: String

    @Value("\${und.lambda.funnel.region}")
    private lateinit var AWS_REGION: String

    @Value("\${und.lambda.funnel.functionName}")
    private lateinit var AWS_LAMBDA_FUNCTION: String

    @Autowired
    lateinit var mapper: ObjectMapper

    companion object {
        val logger: Logger = loggerFor(AWSFunnelLambdaInvoker::class.java)

    }

    fun computeFunnels(funnelData: FunnelData): List<FunnelReport.FunnelStep> {
        logger.debug("Computing funnels at lambda: ${AWS_LAMBDA_FUNCTION}")
        val region = Regions.fromName(AWS_REGION)

        val basicAWSCredentials = BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)

        val awsLambdaClient = AWSLambdaClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials)).withRegion(region).build()

        val invokeRequest = InvokeRequest().withFunctionName(AWS_LAMBDA_FUNCTION).withInvocationType(InvocationType.RequestResponse.toString())
                .withPayload(mapper.writeValueAsString(funnelData))

        val invokeResult = awsLambdaClient.invoke(invokeRequest)

        if (invokeResult.functionError != null || invokeResult.statusCode != 200) {
            logger.error("Status code received from aws lambda: ${invokeResult.statusCode} with function error: ${invokeResult.functionError} for request: ${invokeRequest.functionName}")
            return emptyList()
        }
        val result = String(invokeResult.payload.array())

        return jacksonObjectMapper().readValue(result)
    }
}