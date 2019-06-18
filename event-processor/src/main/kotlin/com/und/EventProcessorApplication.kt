package com.und

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventProcessorApplication

fun main(args: Array<String>) {
	runApplication<EventProcessorApplication>(*args)
}
