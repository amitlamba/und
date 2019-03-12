package com.und


import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories(basePackages = ["com.und.repository.mongo","com.und.livesegment.repository.mongo"])
@EnableJpaRepositories(basePackages = ["com.und.repository.jpa", "com.und.livesegment.repository.jpa"])
//@EnableRedisRepositories(basePackages = ["com.und.repository.redis"])
@EnableAutoConfiguration
@ComponentScan(basePackages = ["com.und"])
@RefreshScope
@EnableEurekaClient
@EnableFeignClients
@EnableCaching
class ClientPanelApplication

fun main(args: Array<String>) {
    SpringApplication.run(ClientPanelApplication::class.java, *args)
}