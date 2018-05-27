package com.und


import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories(basePackages = ["com.und.repository.mongo"])
@EnableAutoConfiguration
@ComponentScan(basePackages = ["com.und"])
@RefreshScope
@EnableEurekaClient
@EnableFeignClients
class EventApplication

fun main(args: Array<String>) {
    SpringApplication.run(EventApplication::class.java, *args)
}
