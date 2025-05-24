package com.ofya.markdown.blob.server.dtos.auth

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val roles: List<String>
)