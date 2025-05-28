package org.example.urlshortenerbackend.services

import org.example.urlshortenerbackend.dtos.CreateLinkRequest
import org.example.urlshortenerbackend.dtos.LinkResponse

interface LinkService {

    fun createLink(request: CreateLinkRequest): LinkResponse
    fun getLinkInfo(shortCode: String): LinkResponse
    fun resolveLink(shortCode: String, ip: String, userAgent: String): String
    fun deleteLink(shortCode: String): Boolean

}