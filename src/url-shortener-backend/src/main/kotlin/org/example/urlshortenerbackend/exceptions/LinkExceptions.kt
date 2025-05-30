package org.example.urlshortenerbackend.exceptions

class LinkNotFoundException(shortCode: String) : RuntimeException("Link with short code '$shortCode' not found.")