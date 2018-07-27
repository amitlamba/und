package com.und.service


import com.und.model.jpa.Status
import com.und.model.utils.ServiceProviderCredentials
import com.und.model.utils.Sms
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime

@DataJpaTest
@RunWith(SpringRunner::class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
class SmsServiceTest {

    @Autowired
    private lateinit var smsHelperService: SmsHelperService;

    private var sms = Sms(3, "1234567890", "9999934345",
            "Nothing", 1, null)

    private var serviceProviderCredentials=ServiceProviderCredentials()

    @Before
     fun setUp() {
        serviceProviderCredentials.id=1007
        serviceProviderCredentials.clientID=3
        serviceProviderCredentials.appuserID=5
        serviceProviderCredentials.serviceProviderType="Sms Service Provider"
        serviceProviderCredentials.serviceProvider="AWS - Simple Notification Service"
        serviceProviderCredentials.dateCreated= LocalDateTime.now()
        serviceProviderCredentials.dateModified= LocalDateTime.now()
        serviceProviderCredentials.status=Status.ACTIVE
        serviceProviderCredentials.credentialsMap= hashMapOf()

    }
    @Test
    fun sendSms() {

        var smsToSend = smsHelperService.updateBody(sms)

        serviceProviderCredentials.sendSms(smsToSend)


    }



}

fun ServiceProviderCredentials.sendSms(sms:Sms){
    when(this.serviceProvider){
        ServiceProviderCredentialsService.ServiceProvider.Twillio.desc->{
            print(this.serviceProvider)
            TwilioSmsSendService().sendSms(this,sms)}
        ServiceProviderCredentialsService.ServiceProvider.AWS_SNS.desc->{
            print(this.serviceProvider)
            AWS_SNSSmsService().sendSms(this,sms)}
    }
}
