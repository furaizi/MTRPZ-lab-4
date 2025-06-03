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
        Mapping(target = "shortCode", source = "shortCode"),
        Mapping(target = "clicks", constant = "0L"),
        Mapping(target = "uniqueVisitors", constant = "0L"),
        Mapping(target = "lastAccessedAt", ignore = true),
        Mapping(target = "expiresAt", ignore = true),
        Mapping(target = "active", constant = "true"),
        Mapping(target = "createdAt", ignore = true),
        Mapping(target = "updatedAt", ignore = true),
        Mapping(target = "version", ignore = true)
    )
    fun toEntity(dto: CreateLinkRequest, shortCode: String): Link

    @Mappings(
        Mapping(target = "shortCode", source = "link.shortCode"),
        Mapping(target = "url", source = "url"),
        Mapping(target = "originalUrl", source = "link.originalUrl"),
        Mapping(target = "expiresAt", source = "link.expiresAt"),
        Mapping(target = "createdAt", source = "link.createdAt"),
        Mapping(target = "isActive", source = "link.active")
    )
    fun toLinkResponse(link: Link, url: String): LinkResponse

    @Mappings(
        Mapping(target = "shortCode", source = "shortCode"),
        Mapping(target = "clicks", source = "clicks"),
        Mapping(target = "uniqueVisitors", source = "uniqueVisitors"),
        Mapping(target = "lastAccessedAt", source = "lastAccessedAt"),
        Mapping(target = "isActive", source = "active")
    )
    fun toLinkStatistics(link: Link): LinkStatistics
}