/*
package com.und.report.service

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.InvocationType
import com.amazonaws.services.lambda.model.InvokeRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.util.JSON
import com.und.common.utils.loggerFor
import com.und.report.model.FunnelData
import com.und.report.repository.mongo.UserAnalyticsRepositoryImpl
import org.slf4j.Logger
import org.springframework.stereotype.Component
import springfox.documentation.spring.web.json.Json

@Component
class AWSFunnelLambdaInvoker {

    private val AWS_ACCESS_KEY = "AKIAIFFDPORQ4VDJTTJQ"
    private val AWS_SECRET_KEY = "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"
    private val AWS_REGION = "us-east-1"
    private val AWS_LAMBDA_FUNCTION = "FunnelData"

    companion object {
        val logger: Logger = loggerFor(UserAnalyticsRepositoryImpl::class.java)
    }

    val mapper: ObjectMapper = ObjectMapper()

    fun computeFunnels(funnelData: FunnelData): String? {
        val region = Regions.fromName(AWS_REGION)

        val basicAWSCredentials = BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)

        val awsLambdaClient= AWSLambdaClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials)).withRegion(region).build()

        val invokeRequest = InvokeRequest().withFunctionName(AWS_LAMBDA_FUNCTION).withInvocationType(InvocationType.RequestResponse.toString())
                .withPayload(mapper.writeValueAsString(funnelData))

        val invokeResult = awsLambdaClient.invoke(invokeRequest)

        if(invokeResult.statusCode != 200){
            //TODO error handling
        }

        //TODO error handling

        return String(invokeResult.payload.array())
    }


}


fun main(args: Array<String>) {
    AWSFunnelLambdaInvoker().computeFunnels(FunnelData())
}*/
