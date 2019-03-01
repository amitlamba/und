package com.und.service


import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.und.model.jpa.ClientSettings
import com.und.repository.jpa.ClientSettingsRepository
import com.und.repository.jpa.ServiceProviderCredentialsRepository
import com.und.web.model.AccountSettings
import com.und.web.model.EmailAddress
import com.und.web.model.ServiceProviderCredentials
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.test.util.ReflectionTestUtils
import org.hamcrest.CoreMatchers.`is` as Is

class UserSettingsServiceTest {

    val clientID = 5L
    val userID = 9L

    @InjectMocks
    private lateinit var userSettingsService: UserSettingsService

    @Mock
    private lateinit var serviceProviderCredentialsRepository: ServiceProviderCredentialsRepository

    @Mock
    private lateinit var clientSettingsRepository: ClientSettingsRepository

    @InjectMocks
    private var objectMapper: ObjectMapper = ObjectMapper()

//    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        ReflectionTestUtils.setField(userSettingsService, "clientSettingsRepository", clientSettingsRepository) // one hour
        ReflectionTestUtils.setField(userSettingsService, "serviceProviderCredentialsRepository", serviceProviderCredentialsRepository) // one hour
        ReflectionTestUtils.setField(userSettingsService, "objectMapper", objectMapper) // one hour
        val clientSetting = ClientSettings()
        clientSetting.authorizedUrls = """["http://userndot.com"]"""
        whenever(clientSettingsRepository.findByClientID(clientID)).thenReturn(clientSetting)

    }

    @Test
    fun testSaveAccountSettings() {
        val accountSettings = AccountSettings(urls = arrayOf("http://userndot.com"), timezone = "Asia/Kolkata")
        userSettingsService.saveAccountSettings(accountSettings = accountSettings, clientID = clientID, userID = userID)
        verify(clientSettingsRepository, times(1)).save<ClientSettings>(any())
    }

    @Test
    fun testgetAccountSettings() {
        val accountSettings = userSettingsService.getAccountSettings(clientID)

        verify(clientSettingsRepository, times(1)).findByClientID(clientID)
        assertThat(accountSettings.isPresent, Is(true))
    }

    @Test
    fun testgetAccountSettingsEmpty() {
        val accountSettings = userSettingsService.getAccountSettings(9L)

        verify(clientSettingsRepository, times(1)).findByClientID(9L)
        assertThat(accountSettings.isPresent, Is(false))
    }

    @Test
    fun testAddEmailAddress() {
        val emailAddress = EmailAddress("amit@userndot.com", "Amit Lamba",1)
        val emailAddress2 = EmailAddress("anil@userndot.com", "Anil Thamba",1)
        val emailAddressList = listOf(emailAddress2,emailAddress )
        whenever((clientSettingsRepository).findSenderEmailAddressesByClientId(clientID)).thenReturn(objectMapper.writeValueAsString(listOf(emailAddress2)))
        userSettingsService.addSenderEmailAddress(emailAddress = emailAddress, clientID = clientID)
        verify(clientSettingsRepository, times(1)).findSenderEmailAddressesByClientId(clientID)
        verify(clientSettingsRepository, times(1)).saveSenderEmailAddresses(objectMapper.writeValueAsString(emailAddressList), clientID)
    }


    @Test
    fun testRemoveEmailAddress() {
        val emailAddress = EmailAddress("amit@userndot.com", "Amit Lamba",1)
        val emailAddress2 = EmailAddress("anil@userndot.com", "Anil Thamba",1)
        val emailAddressList = listOf(emailAddress2,emailAddress )
        whenever((clientSettingsRepository).findSenderEmailAddressesByClientId(clientID)).thenReturn(objectMapper.writeValueAsString(emailAddressList))
        userSettingsService.removeSenderEmailAddress(emailAddress = emailAddress, clientID = clientID)
        verify(clientSettingsRepository, times(1)).saveSenderEmailAddresses(objectMapper.writeValueAsString(listOf(emailAddress2)), clientID)
    }

    @Test
    fun testSmtpConnection(){
        var srpc=ServiceProviderCredentials()
        var cred = HashMap<String,String>()
        cred.put("port","465")
        cred.put("username","userndot19@gmail.com")
        cred.put("url","smtp.gmail.com")
        cred.put("password","Userndot1@")
        cred.put("security","NONE")
        srpc.credentialsMap=cred
        srpc.serviceProvider="SMTP"
        println(UserSettingsService().testConnection(srpc))
//        assertThat("Fail",UserSettingsService().testConnection(srpc),Is(true))
    }
}