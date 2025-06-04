package org.example.urlshortenerbackend.controllers

import org.example.urlshortenerbackend.dtos.CreateLinkRequest
import org.example.urlshortenerbackend.dtos.LinkResponse
import org.example.urlshortenerbackend.services.link.LinkService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/links")
class LinkController(
    private val linkService: LinkService
) {

    @PostMapping
    fun createLink(@RequestBody request: CreateLinkRequest)
    : ResponseEntity<LinkResponse> {
        val linkResponse = linkService.createLink(request)
        return ResponseEntity.ok(linkResponse)
    }

    @GetMapping("/{shortCode}")
    fun getLinkInfo(@PathVariable shortCode: String)
    : ResponseEntity<LinkResponse> {
        val linkResponse = linkService.getLinkInfo(shortCode)
        return ResponseEntity.ok(linkResponse)
    }

    @DeleteMapping("/{shortCode}")
    fun deleteLink(@PathVariable shortCode: String)
    : ResponseEntity<Void> {
        linkService.deleteLink(shortCode)
        return ResponseEntity.noContent().build()
    }

}