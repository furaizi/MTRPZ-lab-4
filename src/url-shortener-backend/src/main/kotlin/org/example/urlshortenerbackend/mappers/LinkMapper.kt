package org.example.urlshortenerbackend.mappers

import org.example.urlshortenerbackend.dtos.CreateLinkRequest
import org.example.urlshortenerbackend.dtos.LinkResponse
import org.example.urlshortenerbackend.dtos.LinkStatistics
import org.example.urlshortenerbackend.entities.Link
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface LinkMapper {

    @Mapping(target = "id", ignore = true)
    fun toEntity(dto: CreateLinkRequest, shortCode: String): Link

    fun toLinkResponse(link: Link, url: String): LinkResponse
    fun toLinkStatistics(link: Link): LinkStatistics
}