package org.example.urlshortenerbackend.services

import org.example.urlshortenerbackend.dtos.CreateLinkRequest
import org.example.urlshortenerbackend.dtos.LinkResponse
import org.example.urlshortenerbackend.mappers.LinkMapper
import org.example.urlshortenerbackend.repositories.LinkRepository
import org.example.urlshortenerbackend.utils.ShortCodeGenerator
import org.springframework.stereotype.Service

@Service
class LinkServiceImpl(
    private val linkRepository: LinkRepository,
    private val linkMapper: LinkMapper,
    private val shortCodeGenerator: ShortCodeGenerator
): LinkService {

    override fun createLink(request: CreateLinkRequest): LinkResponse {
        val shortCode = shortCodeGenerator.generate()
        val linkEntity = linkMapper.toEntity(dto = request, shortCode)
        val savedLink = linkRepository.save(linkEntity)
        return linkMapper.toLinkResponse(savedLink, url = "replace this by real url")
    }

    override fun getLinkInfo(shortCode: String): LinkResponse {
        val link = linkRepository.findByShortCode(shortCode)
            ?: throw NoSuchElementException("Link with short code $shortCode not found")
        return linkMapper.toLinkResponse(link, url = "replace this by real url")
    }

    override fun resolveLink(shortCode: String): String {
        TODO("Not yet implemented")
    }

    override fun deleteLink(shortCode: String): Boolean {
        TODO("Not yet implemented")
    }
}