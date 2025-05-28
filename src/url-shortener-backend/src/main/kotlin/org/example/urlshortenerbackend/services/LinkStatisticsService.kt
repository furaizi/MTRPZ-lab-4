package org.example.urlshortenerbackend.services

import org.example.urlshortenerbackend.dtos.LinkStatistics

interface LinkStatisticsService {

    fun getStatistics(shortCode: String): LinkStatistics

}