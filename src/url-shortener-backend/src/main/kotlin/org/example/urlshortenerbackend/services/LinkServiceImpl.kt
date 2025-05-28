package org.example.urlshortenerbackend.services

import org.example.urlshortenerbackend.config.ClickEvent
import org.example.urlshortenerbackend.dtos.CreateLinkRequest
import org.example.urlshortenerbackend.dtos.LinkResponse
import org.example.urlshortenerbackend.mappers.LinkMapper
import org.example.urlshortenerbackend.repositories.LinkRepository
import org.example.urlshortenerbackend.utils.ShortCodeGenerator
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class LinkServiceImpl(
    private val repo: LinkRepository,
    private val mapper: LinkMapper,
    private val shortCodeGenerator: ShortCodeGenerator,
    private val kafka: KafkaTemplate<String, ClickEvent>
): LinkService {

    override fun createLink(request: CreateLinkRequest): LinkResponse {
        val shortCode = shortCodeGenerator.generate()
        val linkEntity = mapper.toEntity(dto = request, shortCode)
        val savedLink = repo.save(linkEntity)
        return mapper.toLinkResponse(savedLink, url = "replace this by real url")
    }

    override fun getLinkInfo(shortCode: String): LinkResponse {
        val link = repo.findByShortCode(shortCode)
            ?: throw NoSuchElementException("Link with short code $shortCode not found")
        return mapper.toLinkResponse(link, url = "replace this by real url")
    }

    override fun resolveLink(shortCode: String, ip: String, userAgent: String): String {
        val originalUrl = repo.findOriginalUrlByShortCode(shortCode)
            ?: throw NoSuchElementException("Link with short code $shortCode not found")

        kafka.send(
            "link-clicks",
            shortCode,
            ClickEvent(shortCode, System.currentTimeMillis(), ip, userAgent)
        )

        return originalUrl
    }

    override fun deleteLink(shortCode: String): Boolean {
        val deletedLinksCount = repo.deleteByShortCode(shortCode)
        return deletedLinksCount > 0
    }
}