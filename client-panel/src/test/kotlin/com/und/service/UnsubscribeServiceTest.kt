package com.und.service


import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.test.util.ReflectionTestUtils
import org.hamcrest.CoreMatchers.`is` as Is


class UnsubscribeServiceTest {

    @InjectMocks
    private lateinit var unsubscribeService: UnsubscribeService

    @Before
    fun setup() {
        val key = "a".repeat(16)//key of strength 16, it can be 16,24 or 32,
        MockitoAnnotations.initMocks(this)
        ReflectionTestUtils.setField(unsubscribeService, "encryptDecryptKey", key) // one hour

    }

    @Test
    fun testUnsubscribeLinks() {
        val dummyUnsubscribeLinkParams = createDummyUnsubscribeLinkParams()
        val unsubscribeLink = unsubscribeService.createUnsubscribeLink(unsubscribeLinkParams = dummyUnsubscribeLinkParams)
        val dataFromUnsubscribeLink = unsubscribeService.getDataFromUnsubscribeLink(unsubscribeLink = unsubscribeLink)
        assertThat(dummyUnsubscribeLinkParams , Is(dataFromUnsubscribeLink))
    }

    private fun createDummyUnsubscribeLinkParams(): UnsubscribeService.UnsubscribeLinkParams {
        return UnsubscribeService.UnsubscribeLinkParams("amit@userndot.com", clientID = 1, userID = "4104")
    }
}