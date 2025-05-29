package org.example.urlshortenerbackend.dtos

import org.aspectj.apache.bcel.classfile.Code
import java.time.LocalDateTime

data class LinkResponse(
    val shortCode: String,
    val url: String,
    val originalUrl: String,
    val expiresAt: LocalDateTime?,
    val createdAt: LocalDateTime
)