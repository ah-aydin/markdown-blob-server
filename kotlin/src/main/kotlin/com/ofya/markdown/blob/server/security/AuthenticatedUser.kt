package com.ofya.markdown.blob.server.security

import org.springframework.security.core.userdetails.User

data class AuthenticatedUser(
    val id: Long,
    val email: String,
) : User(email, "", emptyList())