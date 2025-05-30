package org.example.urlshortenerbackend.statistics.uniqueipstore

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryUniqueIpStore : UniqueIpStore {
    private val visitedMap = ConcurrentHashMap<String, MutableSet<String>>()


    override fun isFirstTime(linkShortCode: String, ip: String): Boolean {
        val visitedIps = visitedMap.computeIfAbsent(linkShortCode) { ConcurrentHashMap.newKeySet() }
        return visitedIps.add(ip)
    }
}