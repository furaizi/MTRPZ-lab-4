package org.example.urlshortenerbackend.services.linkstatistics

import org.example.urlshortenerbackend.dtos.LinkStatistics
import org.example.urlshortenerbackend.mappers.LinkMapper
import org.example.urlshortenerbackend.repositories.LinkRepository
import org.springframework.stereotype.Service

@Service
class LinkStatisticsServiceImpl(
    val repo: LinkRepository,
    val mapper: LinkMapper
) : LinkStatisticsService {

    override fun getStatistics(shortCode: String): LinkStatistics {
        val link = repo.findByShortCode(shortCode)
            ?: throw NoSuchElementException("Link with short code $shortCode not found")

        return mapper.toLinkStatistics(link)
    }
}