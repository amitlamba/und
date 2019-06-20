package com.und

import com.und.config.StreamClass
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableBinding(StreamClass::class)
@EnableEurekaClient
@EnableMongoRepositories
@EnableJpaRepositories
class EventProcessorApplication

fun main(args: Array<String>) {
	runApplication<EventProcessorApplication>(*args)
}
