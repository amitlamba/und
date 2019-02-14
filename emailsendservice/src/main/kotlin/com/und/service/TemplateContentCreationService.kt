package com.und.service

import com.und.model.jpa.AndroidTemplate
import com.und.model.jpa.SmsTemplate
import com.und.model.jpa.WebPushTemplate
import com.und.model.mongo.EventUser
import com.und.model.utils.Email
import com.und.utils.loggerFor
import freemarker.template.Configuration
import freemarker.template.Template
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils
import java.io.StringReader
import java.net.URLEncoder
import java.util.regex.Pattern

@Service
class TemplateContentCreationService {

    companion object {
        val logger:Logger = loggerFor(TemplateContentCreationService::class.java)
    }

    @Autowired
    private lateinit var freeMarkerConfiguration: Configuration


    val urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"
    val trackingURL = "https://userndot.com/event/track"
    val excludeTrackingURLs = arrayOf(
            "^(https?|ftp)://userndot.com.*\$"
    )

    enum class EmailContent(val desc: String) {
        BODY("body"), SUBJECT("subject")
    }

    enum class SmsContent(val desc: String){
        BODY(desc = "body")
    }
    fun getEmailSubject(email: Email, model: MutableMap<String, Any>): String {
        return getContentFromTemplate(email, EmailContent.SUBJECT, model)
    }

    fun getEmailBody(email: Email, model: MutableMap<String, Any>): String {
        return getContentFromTemplate(email, EmailContent.BODY, model)
    }

    fun getAndroidBody(template:AndroidTemplate,model: MutableMap<String,Any>):String{
        return getContentFromTemplate(template,model)
    }
    fun getWebpushBody(template: WebPushTemplate,model: MutableMap<String, Any>):String{
        return getContentFromTemplate(template,model)
    }
    fun getSmsBody(template:SmsTemplate,model:MutableMap<String,Any>):String{
        return FreeMarkerTemplateUtils.processTemplateIntoString(getSmsBodyFreeMarkerTemplate(template),model)
    }
    @Cacheable(value = "sms-template-body",key = "'sms-template-body-'+#smsTemplate.id")
    private fun getSmsBodyFreeMarkerTemplate(smsTemplate: SmsTemplate):Template{
        return Template("${smsTemplate.clientID}-a-t-${smsTemplate.id}",StringReader(smsTemplate.smsTemplateBody),freeMarkerConfiguration)
    }
    fun getContentFromTemplate(template:AndroidTemplate, model:MutableMap<String,Any>):String{
        return FreeMarkerTemplateUtils.processTemplateIntoString(getAndroidBodyFreemarkerTemplate(template), model)
    }

    fun getContentFromTemplate(template:WebPushTemplate, model:MutableMap<String,Any>):String{
        return FreeMarkerTemplateUtils.processTemplateIntoString(getWebpushBodyFreemarkerTemplate(template), model)
    }

    @Cacheable(value = "android-template-body",key = "'android-template-body-'+ #androidTemplate.id")
    private fun getAndroidBodyFreemarkerTemplate(androidTemplate: AndroidTemplate): Template {
        var template = Template("${androidTemplate.clientId}-a-t-${androidTemplate.id}", StringReader(androidTemplate.body), freeMarkerConfiguration)
        return template
    }
    @Cacheable(value = "webpush-template-body",key = "'webpush-template-body'+#webTemplate.id")
    private fun getWebpushBodyFreemarkerTemplate(webTemplate: WebPushTemplate):Template{
        return Template("${webTemplate.clientId}-web-t-${webTemplate.id}",StringReader(webTemplate.body),freeMarkerConfiguration)
    }
    fun getContentFromTemplate(templateName: String, model: MutableMap<String, Any>): String {
        val template = freeMarkerConfiguration.getTemplate(templateName)
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model)
    }


    private fun getContentFromTemplate(email: Email, contentType: EmailContent, model: MutableMap<String, Any>): String {
        var clientId=email.clientID
        if(email.tmpltVisiability) clientId=1
        val name = "${clientId}:${email.emailTemplateName}:${contentType.desc}:${email.emailTemplateId}"

        val template = freeMarkerConfiguration.getTemplate(name)
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model)
    }

    private fun getContentFromTemplate(name: String, templateContent: String, model: Map<String, Any>): String {
        val content = StringBuilder()
        try {
            val template = Template(name, templateContent, freeMarkerConfiguration)
            content.append(FreeMarkerTemplateUtils.processTemplateIntoString(template, model))
        } catch (e: Exception) {
            //FIXME do something here
            logger.error(e.message)
            throw Exception("Template content for name $name not found")
        }

        return content.toString()
    }

    fun trackAllURLs(content: String, clientId: Long, mongoEmailId: String): String {
        val containedUrls = ArrayList<String>()
        val pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)
        val urlMatcher = pattern.matcher(content)

        while (urlMatcher.find()) {
            containedUrls.add(content.substring(urlMatcher.start(0),
                    urlMatcher.end(0)))
        }

        var replacedContent = content
        for (c in containedUrls) {
            var skip = false
            for (exclude in excludeTrackingURLs) {
                if (c.matches(exclude.toRegex())) {
                    skip = true
                    break
                }
            }
            if (skip)
                continue
            replacedContent = replacedContent.replace(c, "$trackingURL?c=$clientId&e=$mongoEmailId&u=" + URLEncoder.encode(c, "UTF-8"))
        }
        return replacedContent
    }

}