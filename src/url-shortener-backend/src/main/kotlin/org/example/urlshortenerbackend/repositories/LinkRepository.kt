package org.example.urlshortenerbackend.repositories

import org.example.urlshortenerbackend.entities.Link
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface LinkRepository : JpaRepository<Link, Long> {
    fun findByShortCode(shortCode: String): Link?
    fun existsByShortCode(shortCode: String): Boolean

    // In order not to raise from the database the whole Link object, just find the original URL by short code for redirecting
    @Query("""
        SELECT l.originalUrl FROM Link l
        WHERE l.shortCode = :shortCode
            AND l.isActive = true
            AND (l.expiresAt IS NULL OR l.expiresAt > :now)
    """)
    fun findOriginalUrlByShortCode(
        @Param("shortCode") shortCode: String,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): String?

    @Query("""
        SELECT l FROM Link l
        WHERE l.shortCode = :shortCode
            AND l.isActive = true
            AND (l.expiresAt IS NULL OR l.expiresAt > :now)
    """)
    fun findActiveByShortCode(
        @Param("shortCode") shortCode: String,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): Link?

    @Modifying
    @Query("""
        UPDATE Link l
        SET l.clicks = l.clicks + 1,
            l.lastAccessedAt = :now
        WHERE l.shortCode = :shortCode
    """)
    fun incrementClicks(
        @Param("shortCode") shortCode: String,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    )

    @Modifying
    @Query("""
        UPDATE Link l
        SET l.uniqueVisitors = l.uniqueVisitors + 1
        WHERE l.shortCode = :shortCode
    """)
    fun incrementUniqueVisitors(@Param("shortCode") shortCode: String)

    @Modifying
    @Query("""
        UPDATE Link l
        SET l.clicks = l.clicks + :totalClicks,
            l.uniqueVisitors = l.uniqueVisitors + :uniqueVisitors,
            l.lastAccessedAt = :lastAccessedAt
        WHERE l.shortCode = :shortCode
    """)
    fun bulkIncrementStats(
        @Param("shortCode") shortCode: String,
        @Param("totalClicks") totalClicks: Long,
        @Param("uniqueVisitors") uniqueVisitors: Long,
        @Param("lastAccessedAt") lastAccessedAt: LocalDateTime = LocalDateTime.now()
    )


    fun deleteByShortCode(shortCode: String): Long

    @Modifying
    @Query("""
        UPDATE Link l
        SET l.isActive = false
        WHERE l.shortCode = :shortCode
    """)
    fun setNonActive(shortCode: String): Long
}