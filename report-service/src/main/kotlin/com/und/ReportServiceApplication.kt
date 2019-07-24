package com.und

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = ["com.und"])
@EnableMongoRepositories(value = ["com.und.repository.mongo"])
@EnableJpaRepositories(value = ["com.und.repository.jpa"])
class ReportServiceApplication

fun main(args: Array<String>) {
    runApplication<ReportServiceApplication>(*args)
}
