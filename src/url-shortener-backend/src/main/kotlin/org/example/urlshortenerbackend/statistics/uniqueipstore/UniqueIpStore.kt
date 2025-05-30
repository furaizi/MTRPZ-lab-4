package org.example.urlshortenerbackend.statistics.uniqueipstore

interface UniqueIpStore {
    fun isFirstTime(linkShortCode: String, ip: String): Boolean
}