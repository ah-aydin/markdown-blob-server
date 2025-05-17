package com.ofya.markdown.blob.server.controllers

import com.ofya.markdown.blob.server.entities.User
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/test")
class TestController {

    @GetMapping("/hello")
    fun hello(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.principal is User) {
            println("The user ID is ${(authentication.principal as User).id}")
        }
        return "hello world"
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    fun user(): String {
        return "USER"
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    fun admin(): String {
        return "ADMIN"
    }
}