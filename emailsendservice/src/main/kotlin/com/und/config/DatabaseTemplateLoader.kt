package com.und.config

import com.und.model.utils.EmailTemplate
import com.und.repository.jpa.EmailTemplateRepository
import com.und.repository.jpa.TemplateRepository
import com.und.repository.redis.TemplateCacheRepository
import freemarker.cache.TemplateLoader
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.io.Reader
import java.io.StringReader
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class DatabaseTemplateLoader : TemplateLoader {


    @Autowired
    lateinit var templateRepository: TemplateRepository
    @Autowired
    lateinit var emailTemplateRepository: EmailTemplateRepository

    @Autowired
    lateinit var templateCacheRepository: TemplateCacheRepository

    override fun closeTemplateSource(templateSource: Any?) {
    }

    override fun getReader(templateSource: Any?, encoding: String?): Reader {
        val template = templateSource as String
        return StringReader(template)
    }

    override fun getLastModified(templateSource: Any?): Long {
        return LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    override fun findTemplateSource(name: String): Any? {
        //id:clientId:name:subject/body
        return if(!name.isNullOrBlank()) {
            val nameParts = name.trim().split(":")
            val size = nameParts.size
            if(size !=3 && size !=4) return null
            val clientId = nameParts[0].toLong()
            val templatename = nameParts[1]
            val bodyOrSubject = nameParts[2]
            val templateId = if(size==4) nameParts[3].toLong() else 0L

            val template = retrievetemplate(clientId, templateId, "$clientId:$templatename:$bodyOrSubject")
            template?.let {
                if (bodyOrSubject.contains("body")) {
                    return if( clientId != 1L) addPixelTrackingPlaceholder(template.emailTemplateBody) else template.emailTemplateBody
                }
                else {
                    template.emailTemplateSubject
                }
            }
        } else null

    }

    @Cacheable("emailtemplate", "'client_'+#clientId+'_template_'+#templateId")
    fun retrievetemplate(clientId:Long, templateId:Long, name: String): EmailTemplate? {

        return clientId.let {
            val emailTemplateOtion = emailTemplateRepository.findByIdAndClientID(templateId, clientId)
            emailTemplateOtion.map {template-> buildWebEmailTemplate(template) }.orElse(null)

        }

    }

    private fun buildWebEmailTemplate(emailTemplate: com.und.model.jpa.EmailTemplate): EmailTemplate {
        val webTemplate = EmailTemplate()
        webTemplate.id = emailTemplate.id
        webTemplate.name = emailTemplate.name
        webTemplate.emailTemplateBody = emailTemplate.emailTemplateBody?.template ?: ""
        webTemplate.emailTemplateSubject = emailTemplate.emailTemplateSubject?.template ?: ""
        webTemplate.from = emailTemplate.from
        webTemplate.messageType = emailTemplate.messageType
        webTemplate.parentID = emailTemplate.parentID
        webTemplate.tags = emailTemplate.tags
        //webTemplate.editorSelected = emailTemplate.editorSelected
        return webTemplate

    }

    private fun addPixelTrackingPlaceholder(content: String): String {
        val doc = Jsoup.parse(content)
        doc.body().append("\${pixelTrackingPlaceholder}")
        return doc.body().html().toString()
    }
}