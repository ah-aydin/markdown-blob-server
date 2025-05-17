package com.ofya.markdown.blob.server.dtos.auth

import jakarta.validation.constraints.NotBlank

data class RefreshRequest(
    @field:NotBlank(message = "Refresh token cannot be blank")
    val refreshToken: String
)