package org.example.urlshortenerbackend.controllers

import jakarta.servlet.http.HttpServletRequest
import org.example.urlshortenerbackend.services.link.LinkService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.view.RedirectView

@Controller
class LinkRedirectController(private val linkService: LinkService) {

    @GetMapping("/{shortCode}")
    fun redirect(
        @PathVariable shortCode: String,
                        request: HttpServletRequest
    ): RedirectView {
        val ip = request.remoteAddr
        val userAgent = request.getHeader("User-Agent")
        val redirectUrl = linkService.resolveLink(shortCode, ip, userAgent)

        return RedirectView(redirectUrl)
    }
}