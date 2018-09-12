package com.und.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.*
import com.amazonaws.services.sns.model.InternalErrorException
import com.amazonaws.services.sns.model.ThrottledException
import com.und.exception.Connection
import com.und.exception.EmailError
import com.und.exception.EmailFailureException
import com.und.model.utils.EmailSESConfig
import org.junit.Before
import org.junit.Test

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.mail.internet.InternetAddress
import kotlin.math.log

class Email(var from: InternetAddress, var to: List<InternetAddress>) {
    var body = "testing";
    var subject = "testing";


}

class AwsEmailSendServiceTest {

    //access denied if not available permission
    lateinit var emailSESConfig: EmailSESConfig;
    var from = InternetAddress("jogende.live@gmail.com");
//    error code MessageRejected
//    error message Email address is not verified. The following identities failed the check in region US-EAST-1: joederr@gmail.com
//    error type Client
//    400
    var to = listOf<InternetAddress>(InternetAddress("jogende@gail.com"), InternetAddress("jogendrshekhawat@userndot.com"), InternetAddress("jogender.live@gmail.com"));
//    var to= ArrayList<InternetAddress>(500)
//    error code InvalidParameterValue
//    error message Missing final '@domain'
//    error type Client
//    400
    var email: Email = Email(from, to);
    lateinit var logger: Logger;
    @Before
    fun setup() {
        logger = LoggerFactory.getLogger(AwsEmailSendServiceTest::class.java);

        var region: Regions = Regions.US_EAST_1;
//        UnknownHostException
        var awsAccesskeyId: String="AKIAIEYLU4YI6Q44HM2Q";
//        error code InvalidClientTokenId
//        error message The security token included in the request is invalid.
//        error type Client
//        403
        var awssecretkeyid: String="VFMSzha+0G8i/pKJ9fFg/yMo8FVuJsj6j5yEAR5V";
//        error code SignatureDoesNotMatch
//        error message The request signature we calculated does not match the signature you provided. Check your AWS Secret Access Key and signing method. Consult the service documentation for details.
//                error type Client
//        403
        emailSESConfig = EmailSESConfig(1, 3, region, awsAccesskeyId, awssecretkeyid);
//        var reader=BufferedReader(FileReader(File("/home/jogendra/userndot/GeolocationList")))
//        for(i in 0..900){
//            var line=reader.readLine()
//            line=line.replace("\\W".toRegex(),"")
//            line=line+"@jogi.com"
//            to.add(InternetAddress(line));
//        }
    }
//  if domain part missing
//    error code InvalidParameterValue
//    error message Missing final '@domain'
//    error type Client
//    400
    @Test
    fun sendEmailByAWSSDK() {
        var retries = 0;
        var retry = false;
    var toEmailList = email.to;
    var validEmail= mutableListOf<InternetAddress>()
    var unsentEmail= mutableListOf<InternetAddress>()
    var to:InternetAddress= toEmailList[0]
    var client : AmazonSimpleEmailService?=null
    val credentialsProvider: AWSCredentialsProvider = AWSStaticCredentialsProvider(BasicAWSCredentials(emailSESConfig.awsAccessKeyId, emailSESConfig.awsSecretAccessKey))
    println("credential provider clled")
    try {

            client = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(emailSESConfig.region).build()
//            if credentail are wrong
//            error code InvalidClientTokenId
//            error message The security token included in the request is invalid.
//            error type Client
//            403
//            var c=client.createConfigurationSet(CreateConfigurationSetRequest()
//                    .withConfigurationSet(ConfigurationSet().withName("jogi")))
            println("amazon simple email service client created")
//            var  eventDestination=EventDestination().withName("jogievent")
//            var s=eventDestination.snsDestination
//            eventDestination.setMatchingEventTypes(listOf("Bounces","Rejects"))
//            s.withTopicARN("topic")

//            val bounce=SendBounceRequest()
            for (to in toEmailList) {


                val request = SendEmailRequest()


                with(request) {
                    destination = Destination().withToAddresses(to.address)

                    message = (Message()
                            .withBody(Body()
                                    .withHtml(Content()
                                            .withCharset("UTF-8").withData(email.body))
                            )
                            .withSubject(Content()
                                    .withCharset("UTF-8").withData(email.subject)))

                    source = from.address

                    configurationSetName = "jogi"
//                        error code ConfigurationSetDoesNotExist
//                        error message Configuration set <jogi> does not exist.
//                        error type Client
//                        400

                }
//                    println(bounce.bouncedRecipientInfoList)
                //var RetryListner

                var sendEmailResult: SendEmailResult = client.sendEmail(request)
            }


        }catch (ex: MessageRejectedException) {
        var error=EmailError()
        for (c in Connection.values()){
            if(c.toString().equals(ex.errorCode)){
                error.failureType=EmailError.FailureType.CONNECTION
                println("\nmesage reject connection call\n")
            }else{
                error.failureType=EmailError.FailureType.OTHER
            }
        }
            logger.info("error code ${ex.errorCode} \n error message ${ex.errorMessage}\n error type ${ex.errorType}\n ${ex.statusCode}\n ${ex.isRetryable}")
        }
        catch (ex: ConfigurationSetDoesNotExistException) {
            var int=toEmailList.indexOf(to)
            unsentEmail.addAll(toEmailList.subList(int,toEmailList.size))
            var emailError=EmailError()
            with(emailError){
                clientid = 1
                failureType = EmailError.FailureType.OTHER   //errrorCode
                causeMessage = "Message rejected : ${ex.errorMessage}"
                errorType="${ex.errorType}"
                errorCode="${ex.errorCode}"
                validSentAddresses=validEmail
                unsentAddresses=unsentEmail
                failedSettingId = emailSESConfig.serviceProviderCredentialsId

            }
            throw EmailFailureException("Message Rejected ${ex.errorMessage} ",ex,emailError)

        }catch (ex: InternalErrorException) {
            var int=toEmailList.indexOf(to)
            unsentEmail.addAll(toEmailList.subList(int,toEmailList.size))
            logger.info("error code ${ex.errorCode} \n error message ${ex.errorMessage}\n error type ${ex.errorType}\n ${ex.statusCode} \n ${ex.isRetryable}")
        }catch (ex: MailFromDomainNotVerifiedException) {
            var int=toEmailList.indexOf(to)
            unsentEmail.addAll(toEmailList.subList(int,toEmailList.size))
            logger.info("error code ${ex.errorCode} \n error message ${ex.errorMessage}\n error type ${ex.errorType}\n ${ex.statusCode}\n${ex.isRetryable}")
        }catch (ex:AmazonSimpleEmailServiceException){
            var int=toEmailList.indexOf(to)
            unsentEmail.addAll(toEmailList.subList(int,toEmailList.size))
            logger.info("error code ${ex.errorCode} \n error message ${ex.errorMessage}\n error type ${ex.errorType}\n ${ex.statusCode}\n${ex.isRetryable}")
        }
        catch (ex: Exception) {
            logger.info("${ex.message}")
        }

        print("terminate normally")
    }

    fun getSleepDuration(currentTry: Int, minSleepMillis: Long, maxSleepMillis: Long): Long {
        var currentTry = currentTry
        currentTry = Math.max(0, currentTry)
        val currentSleepMillis = (minSleepMillis * Math.pow(2.0, currentTry.toDouble())).toLong()
        return Math.min(currentSleepMillis, maxSleepMillis)
    }
}

//InternalFailure 500
//InternalFailure 503


enum class Connection{
    MessageRejected ,   //from addresss not verified
    InvalidParameterValue,  //if domain part missing
    InvalidClientTokenId,  //access id wrong
    SignatureDoesNotMatch, // access seceret id wrong
    AccessDenied,       //not enough permisssion to iam user
    ConfigurationSetDoesNotExist,  //config set not exist
}