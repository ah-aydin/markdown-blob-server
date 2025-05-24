package com.ofya.markdown.blob.server.security.jwt

data class UserClaims(
    val id: Long,
    val email: String
)