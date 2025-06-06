package org.example.urlshortenerbackend.dtos

import java.time.LocalDateTime

data class LinkStatistics(
    val shortCode: String,
    val clicks: Long,
    val uniqueVisitors: Long,
    val lastAccessedAt: LocalDateTime?,
    val isActive: Boolean
)