package com.ofya.markdown.blob.server.controllers

import com.ofya.markdown.blob.server.dtos.auth.LoginRequest
import com.ofya.markdown.blob.server.dtos.auth.LoginResponse
import com.ofya.markdown.blob.server.dtos.auth.RefreshRequest
import com.ofya.markdown.blob.server.dtos.auth.RefreshResponse
import com.ofya.markdown.blob.server.dtos.auth.UserResponse
import com.ofya.markdown.blob.server.dtos.auth.UserSignupRequest
import com.ofya.markdown.blob.server.services.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody userSignupRequest: UserSignupRequest): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(
            authService.signup(userSignupRequest)
        )
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(authService.login(loginRequest))
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody refreshRequest: RefreshRequest): ResponseEntity<RefreshResponse> {
        return ResponseEntity.ok(authService.refresh(refreshRequest))
    }

    @PutMapping("/verify")
    fun verify(@RequestParam token: String) {
        authService.verify(token)
    }
}