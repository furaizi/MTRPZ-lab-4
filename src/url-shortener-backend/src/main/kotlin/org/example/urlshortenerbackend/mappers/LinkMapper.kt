package org.example.urlshortenerbackend.mappers

import org.example.urlshortenerbackend.dtos.CreateLinkRequest
import org.example.urlshortenerbackend.dtos.LinkResponse
import org.example.urlshortenerbackend.dtos.LinkStatistics
import org.example.urlshortenerbackend.entities.Link
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring", uses = [])
interface LinkMapper {

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "originalUrl", source = "dto.url"),
        Mapping(target = "active", ignore = true)
    )
    fun toEntity(dto: CreateLinkRequest, shortCode: String): Link

    fun toLinkResponse(link: Link, url: String): LinkResponse

    @Mapping(target = "isActive", source = "link.active")
    fun toLinkStatistics(link: Link): LinkStatistics
}