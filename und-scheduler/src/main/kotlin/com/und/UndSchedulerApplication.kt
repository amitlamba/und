package com.und


import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
//import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableAutoConfiguration()
@ComponentScan(basePackages = ["com.und"])
@RefreshScope
@EnableEurekaClient
//@EnableFeignClients
@EnableScheduling
class UndSchedulerApplication

fun main(args: Array<String>) {
    SpringApplication.run(UndSchedulerApplication::class.java, *args)
}
