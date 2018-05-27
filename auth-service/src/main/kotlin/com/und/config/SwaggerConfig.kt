/*
package com.und.config


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMethod
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.builders.ResponseMessageBuilder
import springfox.documentation.schema.ModelRef
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

import com.google.common.collect.Lists.newArrayList
import springfox.documentation.service.Contact
import springfox.documentation.service.ResponseMessage


*/
/**
 * Created by shiv on 21/07/17.
 *//*

@Configuration
@EnableSwagger2
class SwaggerConfig {

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors
                        .basePackage("com.und.eventapi"))
                .paths(PathSelectors.any()).build()
                .apiInfo(apiInfo())
                .apiInfo(apiInfo()).useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET,
                        newArrayList<ResponseMessage>(
                                ResponseMessageBuilder()
                                        .code(500)
                                        .message("500 message")
                                        .responseModel(ModelRef("Error"))
                                        .build(),
                                ResponseMessageBuilder()
                                        .code(403)
                                        .message("Forbidden!!!!!").build())
                )
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfo(
                "User And Dot Event REST API",
                "API to track user generated events data of interest.",
                "1.0.0", "http://userndot.com/v1/api/terms",
                Contact("shiv Pratap", "http://userndot.com","shiv@userndot.com"),
                "License of API",
                "http://userndot.com/v1/api/licence", arrayListOf())
    }
}*/
