package org.example.urlshortenerbackend.services.link

import org.example.urlshortenerbackend.config.KafkaProperties
import org.example.urlshortenerbackend.dtos.CreateLinkRequest
import org.example.urlshortenerbackend.dtos.LinkResponse
import org.example.urlshortenerbackend.exceptions.LinkNotFoundException
import org.example.urlshortenerbackend.mappers.LinkMapper
import org.example.urlshortenerbackend.repositories.LinkRepository
import org.example.urlshortenerbackend.statistics.LinkClickedEvent
import org.example.urlshortenerbackend.utils.ShortCodeGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LinkServiceImpl(
    private val repo: LinkRepository,
    private val mapper: LinkMapper,
    private val shortCodeGenerator: ShortCodeGenerator,
    private val kafka: KafkaTemplate<String, LinkClickedEvent>,
    private val kafkaProps: KafkaProperties,
    @Value("\${app.server.url}")
    private val baseUrl: String
): LinkService {

    @Transactional
    override fun createLink(request: CreateLinkRequest): LinkResponse {
        val shortCode = shortCodeGenerator.generate()
        val linkEntity = mapper.toEntity(dto = request, shortCode)
        val savedLink = repo.save(linkEntity)
        val shortUrl = buildShortUrl(shortCode)
        return mapper.toLinkResponse(savedLink, url = shortUrl)
    }

    @Transactional(readOnly = true)
    override fun getLinkInfo(shortCode: String): LinkResponse {
        val link = repo.findByShortCode(shortCode)
            ?: throw LinkNotFoundException(shortCode)
        val shortUrl = buildShortUrl(shortCode)
        return mapper.toLinkResponse(link, url = shortUrl)
    }

    override fun resolveLink(shortCode: String, ip: String, userAgent: String): String {
        val originalUrl = repo.findOriginalUrlByShortCode(shortCode)
            ?: throw LinkNotFoundException(shortCode)

        kafka.send(
            kafkaProps.topics.linkClicked,
            shortCode,
            LinkClickedEvent(shortCode, System.currentTimeMillis(), ip, userAgent)
        )

        return originalUrl
    }

    @Transactional
    override fun deleteLink(shortCode: String): Boolean {
        val deletedLinksCount = repo.deleteByShortCode(shortCode)
        return deletedLinksCount > 0
    }

    private fun buildShortUrl(shortCode: String): String = "${baseUrl.trimEnd('/')}/$shortCode"
}