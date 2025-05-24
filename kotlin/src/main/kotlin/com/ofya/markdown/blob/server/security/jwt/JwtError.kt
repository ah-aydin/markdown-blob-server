package com.ofya.markdown.blob.server.security.jwt

enum class JwtError {
    JET_MALFORMED,
    JWT_EXPIRED,
    JWT_UNSUPPORTED,
    JWT_WRONG_SIGNATURE,
    JWT_UNKNOWN_ERROR
}