package org.example.urlshortenerbackend.dtos

import java.time.LocalDateTime

data class LinkStats(
    val shortCode: String,
    val clicks: Long,
    val uniqueVisitors: Long,
    val lastAccessedAt: LocalDateTime?,
    val isActive: Boolean
)