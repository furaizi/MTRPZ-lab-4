package org.example.urlshortenerbackend.services.linkstatistics

import org.example.urlshortenerbackend.dtos.LinkStatistics

interface LinkStatisticsService {

    fun getStatistics(shortCode: String): LinkStatistics

}