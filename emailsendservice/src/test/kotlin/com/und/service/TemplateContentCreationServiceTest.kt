package com.und.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import com.und.config.CacheTemplateLoader
import com.und.config.DatabaseTemplateLoader
import com.und.model.jpa.Template
import com.und.model.mongo.EventUser
import com.und.model.utils.Email
import com.und.repository.jpa.TemplateRepository
import freemarker.template.Configuration
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.mail.internet.InternetAddress
import org.hamcrest.CoreMatchers.`is` as Is

class TemplateContentCreationServiceTest {


    @InjectMocks
    private var cacheTemplateLoader: CacheTemplateLoader = CacheTemplateLoader()

    @InjectMocks
    private var dbTemplateLoader: DatabaseTemplateLoader = DatabaseTemplateLoader()

    @InjectMocks
    private lateinit var templateContentCreationService: TemplateContentCreationService

    @InjectMocks
    private  var freeMarkerConfigurationFactoryBean : FreeMarkerConfigurationFactoryBean = FreeMarkerConfigurationFactoryBean()

    @InjectMocks
    private  var  freeMarkerConfiguration: Configuration = initTemplateconfig()

    @Mock
    lateinit var templateRepository: TemplateRepository
    @Mock
    lateinit var templateRepositoryCache: TemplateRepository


    fun initTemplateconfig(): Configuration {
        freeMarkerConfigurationFactoryBean.setPostTemplateLoaders(cacheTemplateLoader, dbTemplateLoader)
        val config = freeMarkerConfigurationFactoryBean.createConfiguration()
        config.localizedLookup = false
        return config

    }

    @Before
    fun startup() {
        MockitoAnnotations.initMocks(this)

        ReflectionTestUtils.setField(templateContentCreationService, "freeMarkerConfiguration", freeMarkerConfiguration) // one hour
        ReflectionTestUtils.setField(cacheTemplateLoader, "templateRepository", templateRepositoryCache) // one hour
        ReflectionTestUtils.setField(dbTemplateLoader, "templateRepository", templateRepository) // one hour

        //freeMarkerConfiguration.templateLoader = dbTemplateLoader
        //freeMarkerConfiguration.locale = Locale.ENGLISH


    }

    @Test
    fun testTemplateLoadDB() {
        val name = "SampleDB"
        val template = Template()
        template.template =  "Hello, \${ user.standardInfo.firstname} \${ user.standardInfo.lastname}"
        template.dateCreated = LocalDateTime.now(ZoneId.of("UTC"))
        template.dateModified = LocalDateTime.now(ZoneId.of("UTC"))
        whenever(templateRepository.findByName("${name}")).thenReturn(Optional.of(template))

        val converted = templateContentCreationService.getContentFromTemplate("$name", getModelMap())
        Assert.assertThat(converted, Is("Hello, Amit Lamba"))
    }


    @Test
    fun testTemplateLoadCache() {
        whenever(templateRepository.findByName(any())).thenReturn(Optional.empty())
        val name = "SampleCache"
        val template = Template()
        template.template =  "Hello, \${ user.standardInfo.firstname} \${ user.standardInfo.lastname}"
        template.dateCreated = LocalDateTime.now(ZoneId.of("UTC"))
        template.dateModified = LocalDateTime.now(ZoneId.of("UTC"))
        whenever(templateRepositoryCache.findByName("${name}")).thenReturn(Optional.of(template))


        val converted = templateContentCreationService.getContentFromTemplate("$name", getModelMap())
        Assert.assertThat(converted, Is("Hello, Amit Lamba"))
    }


    fun getModelMap(): MutableMap<String, Any> {
        val user = EventUser()
        user.standardInfo.firstname = "Amit"
        user.standardInfo.lastname = "Lamba"
        // return  mutableMapOf("firstname" to "Amit", "lastname" to "Lamba")
        return mutableMapOf("user" to user)
    }
}