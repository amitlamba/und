package com.und

import org.junit.Test
import java.util.regex.PatternSyntaxException
import org.springframework.test.context.transaction.TestTransaction.end
import java.net.URLEncoder
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE





class Test {

    val urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"
    val trackingURL = "https://userndot.com/event/track"
    val excludeTrackingURLs = arrayOf(
            "^(https?|ftp)://(www.)?userndot.com.*\$"
    )

    @Test
    fun parseUrls() {
        val test = """something https://google.com?a=1
            something https://google.com?a=2
            something http://userndot.com?url=https%3A%2F%2Fgoogle.com%3Fa%3D2"""
        try {
            val replacedText = trackAllURLs(test, 1L, "abc")
            println(test)
            println(replacedText)
        } catch (ex: PatternSyntaxException) {
            // Syntax error in the regular expression
        } catch (ex: IllegalArgumentException) {
            // Syntax error in the replacement text (unescaped $ signs?)
        } catch (ex: IndexOutOfBoundsException) {
            // Non-existent backreference used the replacement text
        }

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
        for(c in containedUrls) {
            var skip = false
            for(exclude in excludeTrackingURLs) {
                if (c.matches(exclude.toRegex())) {
                    skip = true
                    break
                }
            }
            if( skip )
                continue
            replacedContent = replacedContent.replace(c, "$trackingURL?c=$clientId&e=$mongoEmailId&u="+ URLEncoder.encode(c,"UTF-8"))
        }
        return replacedContent
    }
}