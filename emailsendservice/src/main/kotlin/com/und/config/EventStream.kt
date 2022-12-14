package com.und.config

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel


interface EventStream {

    @Output("emailEventSend")
    fun emailEventSend(): MessageChannel

    @Input("emailEventReceive")
    fun emailEventReceive(): SubscribableChannel

    @Output(value="smsEventSend")
    fun smsEventSend():MessageChannel

    @Input(value="smsEventReceive")
    fun smsEventReceive():SubscribableChannel

    @Output(value="emailFailureEventSend")
    fun emailFailureEventSend():MessageChannel

    @Output("fcmEventSend")
    fun fcmEventSend():MessageChannel

    @Input(value="fcmEventReceive")
    fun fcmEventReceive():SubscribableChannel

    @Output("fcmFailureEventSend")
    fun fcmFailureEventSend():MessageChannel

    @Input(value="VerificationEmailReceive")
    fun verificationEmailReceive():SubscribableChannel

    @Input("clientEmailReceive")
    fun clientEmailSend(): SubscribableChannel

    @Output("clientEmailSend")
    fun clientEmailOut(): MessageChannel

    @Input("EmailUpdateReceive")
    fun emailUpdateEvent(): SubscribableChannel

    @Input("clickTrackEventReceive")
    fun clickTrackEvent(): SubscribableChannel

    @Input("campaignTriggerReceive")
    fun campaignTriggerReceive(): SubscribableChannel

    @Input("inLiveSegment")
    fun inLiveSegmentReceive():SubscribableChannel

    @Input("inTestCampaign")
    fun inTestCampaign():SubscribableChannel

    @Input("abCampaignTriggerReceive")
    fun abCampaignReceive():SubscribableChannel

    @Output("scheduleJobSend")
    fun scheduleJobSend(): MessageChannel

    @Input("receiveManualTriggerCampaign")
    fun runManualCampaign():SubscribableChannel

    @Output("outTestEmail")
    fun testEmailCampaign():MessageChannel
    @Output("outTestSms")
    fun testSmsCampaign():MessageChannel
    @Output("outTestFcm")
    fun testFcmCampaign():MessageChannel

    @Input("inTestEmail")
    fun inTestEmailCampaign():SubscribableChannel
    @Input("inTestSms")
    fun inTestSmsCampaign():SubscribableChannel
    @Input("inTestFcm")
    fun inTestFcmCampaign():SubscribableChannel

    @Output("outEmailLiveCampaign")
    fun outEmailLiveCampaign():MessageChannel
    @Output("outSmsLiveCampaign")
    fun outSmsLiveCampaign():MessageChannel
    @Output("outFcmLiveCampaign")
    fun outFcmLiveCampaign():MessageChannel

    @Input("inEmailLiveCampaign")
    fun inEmailLiveCampaign():SubscribableChannel
    @Input("inSmsLiveCampaign")
    fun inSmsLiveCampaign():SubscribableChannel
    @Input("inFcmLiveCampaign")
    fun inFcmLiveCampaign():SubscribableChannel

}