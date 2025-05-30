package org.example.urlshortenerbackend

import org.example.urlshortenerbackend.config.KafkaProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(KafkaProperties::class)
class UrlShortenerBackendApplication

fun main(args: Array<String>) {
    runApplication<UrlShortenerBackendApplication>(*args)
}
