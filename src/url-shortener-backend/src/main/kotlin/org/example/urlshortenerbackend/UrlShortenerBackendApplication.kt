package org.example.urlshortenerbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UrlShortenerBackendApplication

fun main(args: Array<String>) {
    runApplication<UrlShortenerBackendApplication>(*args)
}
