package com.und.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import freemarker.template.Configuration as FreeMarkerConfig
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean


@Configuration
class FreeMarkerConfig {


    @Bean
    fun freeMarkerConfiguration():FreeMarkerConfig {
        val configuration = freeMarkerConfigurationBean().createConfiguration()
        configuration.localizedLookup = false
        return configuration
    }

    @Bean
    fun freeMarkerConfigurationBean(): FreeMarkerConfigurationFactoryBean {
        val freeMarkerConfigurationFactoryBean = FreeMarkerConfigurationFactoryBean()
        freeMarkerConfigurationFactoryBean.setPostTemplateLoaders(cacheTemplateLoader(), dataBaseTemplateLoader())
        return freeMarkerConfigurationFactoryBean
    }

    @Bean
    fun dataBaseTemplateLoader(): DatabaseTemplateLoader {
        return DatabaseTemplateLoader()
    }
    @Bean
    fun cacheTemplateLoader(): CacheTemplateLoader {
        return CacheTemplateLoader()
    }



}