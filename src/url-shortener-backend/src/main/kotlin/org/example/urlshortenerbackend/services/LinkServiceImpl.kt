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
        return linkMapper.toLinkResponse(savedLink, request.url)
    }

    override fun getLinkInfo(shortCode: String): LinkResponse {
        TODO("Not yet implemented")
    }

    override fun resolveLink(shortCode: String): String {
        TODO("Not yet implemented")
    }

    override fun deleteLink(shortCode: String): Boolean {
        TODO("Not yet implemented")
    }
}