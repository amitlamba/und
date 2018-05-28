package com.und.config

import com.und.model.jpa.Template
import com.und.model.redis.CachedTemplate
import com.und.repository.jpa.TemplateRepository
import com.und.repository.redis.TemplateCacheRepository
import freemarker.cache.TemplateLoader
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.Reader
import java.io.StringReader
import java.time.ZoneOffset

@Service
class DatabaseTemplateLoader : TemplateLoader {


    @Autowired
    lateinit var templateRepository: TemplateRepository

    @Autowired
    lateinit var templateCacheRepository: TemplateCacheRepository

    override fun closeTemplateSource(templateSource: Any?) {
    }

    override fun getReader(templateSource: Any?, encoding: String?): Reader {
        val template = templateSource as Template
        return StringReader(template.template)
    }

    override fun getLastModified(templateSource: Any?): Long {
        val template = templateSource as Template
        return template.dateModified.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    override fun findTemplateSource(name: String): Any? {
        //id:clientId:name:subject/body
        val templateOption = templateRepository.findByName(name)
        if(templateOption.isPresent) {
            val template = templateOption.get()
            template.template = addPixelTrackingPlaceholder(template.template)
            val cachedTemplate = CachedTemplate()
            cachedTemplate.id = template.name
            cachedTemplate.dateModified = template.dateModified
            cachedTemplate.template = template.template
            templateCacheRepository.save(cachedTemplate)

            return template
        }
        return null
    }

    private fun addPixelTrackingPlaceholder(content: String): String {
        val doc = Jsoup.parse(content)
        doc.body().append("\${pixelTrackingPlaceholder}")
        return doc.body().html().toString()
    }
}