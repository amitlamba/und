package com.und.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import com.und.config.CacheTemplateLoader
import com.und.config.DatabaseTemplateLoader
import com.und.model.jpa.Template
import com.und.model.utils.Email
import com.und.repository.jpa.TemplateRepository
import freemarker.template.Configuration
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean
import java.time.LocalDateTime
import java.util.*
import javax.mail.internet.InternetAddress


class EmailHelperServiceTest {

    @InjectMocks
    private lateinit var emailHelperService: EmailHelperService

    @InjectMocks
    private lateinit var templateContentCreationService: TemplateContentCreationService

    @InjectMocks
    private   var freeMarkerConfiguration: Configuration =  FreeMarkerConfigurationFactoryBean().createConfiguration()

    @InjectMocks
    private  var cacheTemplateLoader: CacheTemplateLoader = CacheTemplateLoader()

    @InjectMocks
    private  var dbTemplateLoader: DatabaseTemplateLoader = DatabaseTemplateLoader()

    @Mock
    lateinit var templateRepository: TemplateRepository
    @Mock
    lateinit var templateRepositoryCache: TemplateRepository



    fun initTemplateconfig() :Configuration {
        val config = FreeMarkerConfigurationFactoryBean()
        config.setPostTemplateLoaders(cacheTemplateLoader, dbTemplateLoader)
        return  config.createConfiguration()

    }

    @Before
    fun startup(){
        MockitoAnnotations.initMocks(this)

        ReflectionTestUtils.setField(emailHelperService, "templateContentCreationService", templateContentCreationService) // one hour
        ReflectionTestUtils.setField(templateContentCreationService, "freeMarkerConfiguration", freeMarkerConfiguration) // one hour
        ReflectionTestUtils.setField(cacheTemplateLoader, "templateRepository", templateRepository) // one hour
        ReflectionTestUtils.setField(dbTemplateLoader, "templateRepository", templateRepositoryCache) // one hour

        freeMarkerConfiguration.templateLoader = dbTemplateLoader
        //freeMarkerConfiguration =   initTemplateconfig()
/*        ReflectionTestUtils.setField(userSettingsService, "objectMapper", objectMapper) // one hour
        val clientSetting = ClientSettings()
        clientSetting.authorizedUrls = """["http://userndot.com"]"""*/


    }

    @Test
    fun testTemplateLoadDB() {
        val template = Template()
        template.template = "Hello world"
        template.dateCreated = LocalDateTime.now()
        template.dateModified = LocalDateTime.now()
        whenever(templateRepository.findByName(any())).thenReturn(Optional.of(template))

        val to = "shiv@userndot.com"
        val email = Email(clientID = 2,
                fromEmailAddress = InternetAddress(to, "Shiv Pratap"),
                toEmailAddresses = arrayOf(InternetAddress(to)),
                emailSubject = null,
                emailBody = null,
                emailTemplateId = 4L,
                emailTemplateName = "hello")
        emailHelperService.updateSubjectAndBody(email)
    }


}