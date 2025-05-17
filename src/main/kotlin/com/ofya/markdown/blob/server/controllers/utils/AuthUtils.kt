package com.ofya.markdown.blob.server.controllers.utils

import com.ofya.markdown.blob.server.dtos.error.ServerError
import com.ofya.markdown.blob.server.entities.User
import org.springframework.security.core.context.SecurityContextHolder

fun getAuthedUserId(): Long {
    val authentication = SecurityContextHolder.getContext().authentication
    if (authentication != null && authentication.principal is User) {
        return (authentication.principal as User).id
    }
    throw ServerError("Failed to get user ID from context")
}