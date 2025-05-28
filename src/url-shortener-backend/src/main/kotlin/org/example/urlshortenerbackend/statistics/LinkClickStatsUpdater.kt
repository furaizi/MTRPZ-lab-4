package org.example.urlshortenerbackend.statistics

import org.example.urlshortenerbackend.repositories.LinkRepository
import org.example.urlshortenerbackend.statistics.uniqueipstore.UniqueIpStore
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class LinkClickStatsUpdater(
    private val repo: LinkRepository,
    private val uniqueStore: UniqueIpStore
) {

    @Transactional
    @KafkaListener(
        topics = ["link-clicks"],
        batch = "true",
        containerFactory = "kafkaBatchFactory"
    )
    fun onClick(events: List<LinkClickedEvent>) {
        events.groupBy { it.shortCode }.forEach { (code, list) ->
            repo.bulkIncrementStats(
                shortCode = code,
                totalClicks = list.size.toLong(),
                uniqueVisitors = getUniqueVisitorsCount(list),
                lastAccessedAt = getLastAccessedAt(list) ?: return
            )
            // if lastAccessedAt is null, we don't need to update stats because there are no clicks
        }
    }

    private fun getUniqueVisitorsCount(events: List<LinkClickedEvent>): Long {
        return events.count { event ->
            uniqueStore.isFirstTime(event.shortCode, event.ip)
        }.toLong()
    }

    private fun getLastAccessedAt(events: List<LinkClickedEvent>): LocalDateTime? {
        return events.maxOfOrNull { it.timestamp }
            ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() }
    }
}