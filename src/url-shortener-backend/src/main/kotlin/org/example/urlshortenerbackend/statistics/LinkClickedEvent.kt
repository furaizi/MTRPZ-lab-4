package org.example.urlshortenerbackend.statistics

data class LinkClickedEvent(
    val shortCode: String,
    val timestamp: Long,
    val ip: String,
    val userAgent: String
)
