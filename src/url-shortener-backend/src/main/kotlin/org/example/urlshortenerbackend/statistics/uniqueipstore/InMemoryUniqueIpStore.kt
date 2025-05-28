package org.example.urlshortenerbackend.statistics.uniqueipstore

import org.springframework.stereotype.Component

@Component
class InMemoryUniqueIpStore : UniqueIpStore {
    private val visitedMap = mutableMapOf<String, MutableSet<String>>()


    override fun isFirstTime(linkShortCode: String, ip: String): Boolean {
        val visitedIps = visitedMap.getOrPut(linkShortCode) { mutableSetOf() }
        return visitedIps.add(ip)
    }
}