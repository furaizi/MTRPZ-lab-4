package org.example.urlshortenerbackend.utils

import org.springframework.stereotype.Component

@Component
class ShortCodeGeneratorImpl : ShortCodeGenerator {
    companion object {
        private const val CODE_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        private const val CODE_LENGTH = 8
    }

    override fun generate(): String = (1..CODE_LENGTH)
        .map { CODE_ALPHABET.random() }
        .joinToString("")
}