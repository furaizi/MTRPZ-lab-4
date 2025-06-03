package org.example.urlshortenerbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Version
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
class Link(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(length = 8, nullable = false, unique = true)
    val shortCode: String,

    @Column(nullable = false)
    var originalUrl: String,

    var clicks: Long = 0L,

    var uniqueVisitors: Long = 0L,

    var lastAccessedAt: LocalDateTime? = null,

    var expiresAt: LocalDateTime? = null,

    // Soft delete flag
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(updatable = false)
    val createdAt: LocalDateTime? = null,
    // nullable because it will be set automatically on creation by Hibernate
    @UpdateTimestamp
    var updatedAt: LocalDateTime? = null,

    // Optimistic lock for concurrent updates of "clicks"
    @Version
    var version: Long? = null
)