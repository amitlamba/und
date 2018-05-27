package com.und.config

import com.und.model.redis.CachedTemplate
import com.und.repository.redis.TemplateCacheRepository
import freemarker.cache.TemplateLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.Reader
import java.io.StringReader
import java.time.ZoneOffset

@Service
class CacheTemplateLoader : TemplateLoader {


    @Autowired
    lateinit var templateCacheRepository: TemplateCacheRepository

    override fun closeTemplateSource(templateSource: Any?) {
    }

    override fun getReader(templateSource: Any?, encoding: String?): Reader {
        val template = templateSource as CachedTemplate
        return StringReader(template.template)
    }

    override fun getLastModified(templateSource: Any?): Long {
        val template = templateSource as CachedTemplate
        return template.dateModified.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    override fun findTemplateSource(name: String): Any? {
        //id:clientId:name:subject/body
        val template = templateCacheRepository.findById(name)
        return if(template.isPresent)template.get() else null
    }
}