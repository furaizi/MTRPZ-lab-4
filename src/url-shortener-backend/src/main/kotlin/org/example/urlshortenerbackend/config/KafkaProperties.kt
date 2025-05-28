package org.example.urlshortenerbackend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.kafka")
data class KafkaProperties(val topics: Topics) {
    data class Topics(val linkClicked: String)
}
