package com.und


import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["com.und.repository.jpa"])
@EnableRedisRepositories(basePackages = ["com.und.repository.redis"])
@EnableAutoConfiguration
@ComponentScan(basePackages = arrayOf("com.und"))
@RefreshScope
@EnableEurekaClient
@EnableFeignClients
class AuthServiceApplication

    fun main(args: Array<String>) {

        SpringApplication.run(AuthServiceApplication::class.java, *args)
    }

