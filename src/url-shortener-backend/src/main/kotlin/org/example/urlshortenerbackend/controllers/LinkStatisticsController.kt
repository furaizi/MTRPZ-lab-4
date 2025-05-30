package org.example.urlshortenerbackend.controllers

import org.example.urlshortenerbackend.dtos.LinkStatistics
import org.example.urlshortenerbackend.services.linkstatistics.LinkStatisticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/links")
class LinkStatisticsController(private val linkStatsService: LinkStatisticsService) {

    @GetMapping("/{shortCode}/stats")
    fun getLinkStatistics(@PathVariable shortCode: String)
    : ResponseEntity<LinkStatistics> {
        val statistics = linkStatsService.getStatistics(shortCode)
        return ResponseEntity.ok(statistics)
    }
}