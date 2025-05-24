package com.ofya.markdown.blob.server.dtos.auth

data class UserResponse(
    val id: Long,
    val email: String,
    val roles: List<String>
)