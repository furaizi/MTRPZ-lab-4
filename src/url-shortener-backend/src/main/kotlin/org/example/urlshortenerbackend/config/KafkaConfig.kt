package org.example.urlshortenerbackend.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig(
    @Value("\${kafka.bootstrap}")
    private val bootstrap: String
) {

    @Bean
    fun producerFactory(): ProducerFactory<String, ClickEvent> =
        DefaultKafkaProducerFactory(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrap,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
            )
        )

    @Bean
    fun kafkaTemplate() = KafkaTemplate(producerFactory())
}

data class ClickEvent(
    val shortCode: String,
    val linkId: String,
    val timestamp: Long,
    val ip: String,
    val userAgent: String
)