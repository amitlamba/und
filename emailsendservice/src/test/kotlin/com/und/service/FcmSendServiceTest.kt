package com.und.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.und.model.utils.fcm.FcmResponse
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

//@RunWith(SpringRunner::class)
//@SpringBootTest
class FcmSendServiceTest {

    @Autowired
    private lateinit var fcmSendService: FcmSendService

    /*
    <200 OK,{multicast_id=7846428299515901690, success=0, failure=1, canonical_ids=0, results=[{error=InvalidRegistration}]},{accept-ranges=[none], alt-svc=[hq=":443"; ma=2592000; quic=51303431; quic=51303339; quic=51303338; quic=51303337; quic=51303335,quic=":443"; ma=2592000; v="41,39,38,37,35"], cache-control=[private, max-age=0], content-type=[application/json; charset=UTF-8], date=[Tue, 21 Nov 2017 11:24:28 GMT], expires=[Tue, 21 Nov 2017 11:24:28 GMT], server=[GSE], transfer-encoding=[chunked], vary=[Accept-Encoding], x-content-type-options=[nosniff], x-frame-options=[SAMEORIGIN], x-xss-protection=[1; mode=block]}>
    {
        multicast_id = 7846428299515901690, success = 0, failure = 1, canonical_ids = 0, results = [{
            error = InvalidRegistration
        }]
    }
     */
//    @Test
//    fun testFcmSendService() {
//        //server key AAAAHxikc0w:APA91bHBzujPVoTOBrrkUolCzFWIsXhz-LJHYsDuw4kHCPULto7TdpZCUYfUjPTN7Ff4fYHJuacqbAQMFQa7isxwmACj89dCzKa2awR2xMZIx7QkbtU0jTOVlHe-qYKDrbiB-a2kRA0l
//        //legacy server key AIzaSyDxc94h8UKCYhrVCcvybgd6H7PWjGaHzM4
////        val response = fcmSendService.sendMessage(1, "AAAAHxikc0w:APA91bHBzujPVoTOBrrkUolCzFWIsXhz-LJHYsDuw4kHCPULto7TdpZCUYfUjPTN7Ff4fYHJuacqbAQMFQa7isxwmACj89dCzKa2awR2xMZIx7QkbtU0jTOVlHe-qYKDrbiB-a2kRA0l", "Hello",
//                "Hello World", "abc")
//        val jsonString = jacksonObjectMapper().writeValueAsString(response!!.body)
//        var fcmResponse: FcmResponse = jacksonObjectMapper().readValue<FcmResponse>(jsonString)
//    }
    @Test
    @Ignore
    fun testFcmSendService() {
//        //server key AAAAHxikc0w:APA91bHBzujPVoTOBrrkUolCzFWIsXhz-LJHYsDuw4kHCPULto7TdpZCUYfUjPTN7Ff4fYHJuacqbAQMFQa7isxwmACj89dCzKa2awR2xMZIx7QkbtU0jTOVlHe-qYKDrbiB-a2kRA0l
//        //legacy server key AIzaSyDxc94h8UKCYhrVCcvybgd6H7PWjGaHzM4
//        val response = fcmSendService.sendMessage(1, "AAAAHxikc0w:APA91bHBzujPVoTOBrrkUolCzFWIsXhz-LJHYsDuw4kHCPULto7TdpZCUYfUjPTN7Ff4fYHJuacqbAQMFQa7isxwmACj89dCzKa2awR2xMZIx7QkbtU0jTOVlHe-qYKDrbiB-a2kRA0l", "Hello",
//                "Hello World", "abc")
//        val jsonString = jacksonObjectMapper().writeValueAsString(response!!.body)
//        var fcmResponse: FcmResponse = jacksonObjectMapper().readValue<FcmResponse>(jsonString)
    }

    @Test
    fun sendMessage(){
        var fcmSendService=FcmSendService()
        var fcmMessage=FcmSendService.FcmMessage()
        fcmMessage.project_id="cloudmessaging-d2f78"
        var message=TestMessage()
        var m=FcmSendService.TestMessage1()
//        message.token="APA91bFnYNQZj-d5iWVkzDc0xTFf27ag97mC14VxIFkO2kVrL2tYFsjvit_I6pCvMF_tVJGrS01lszCCg7BJPLmO_HuA1deRJtdloKN2nuMUXOz472-uCjw"
        message.token="eEpmrv70drA:APA91bH8uGOUHhTi5AFMJuMx7tQ49vnQkphMNryaXE9IMV47MVQ8FDyaz2sSztyeGTxSZtpI1kA7hii-MnvFzq1Nh6IpdT8bhfaprjscWLNJfAxO3Wz3wQmhtZDB6BiIX2llcaVdjiAx"
        var map=HashMap<String,String>()
        map.put("channel_id","userndot")
        map.put("title","Httpv1 test")
        map.put("body","Http v1 test successfull")
        map.put("channel_name","userndotc")
        map.put("pr","high")
        map.put("bg_pic","")
        map.put("userndot","yes")
        message.data=map
        m.message=message
        fcmMessage.message=m
        fcmSendService.sendMessage(fcmMessage)
    }
}