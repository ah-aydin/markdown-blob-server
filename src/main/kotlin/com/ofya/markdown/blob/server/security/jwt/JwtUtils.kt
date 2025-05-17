package com.ofya.markdown.blob.server.security.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtUtils {

    @Value("\${spring.auth.jwt.secret}")
    lateinit var jwtSecret: String

    @Value("\${spring.auth.jwt.accessTokenLifetime}")
    var accessTokenLifetime: Long? = null

    @Value("\${spring.auth.jwt.refreshTokenLifetime}")
    var refreshTokenLifetime: Long? = null

    fun getJwtFromHeader(request: HttpServletRequest): String? {
        val authToken = request.getHeader("Authorization")
        if (authToken != null && authToken.startsWith("Bearer ")) {
            return authToken.substring("Bearer ".length)
        }
        return null
    }

    fun generateAccessTokenForUser(username: String): String {
        // TODO include user ID and roles in claims
        return Jwts
            .builder()
            .subject(username)
            .claim("type", JwtTokenType.ACCESS)
            .issuedAt(Date())
            .expiration(Date(Date().time + (accessTokenLifetime ?: 0)))
            .signWith(getKey())
            .compact()
    }

    fun generateRefreshTokenForUser(username: String): String {
        // TODO include user ID and roles in claims
        return Jwts
            .builder()
            .subject(username)
            .issuedAt(Date())
            .claim("type", JwtTokenType.REFRESH)
            .expiration(Date(Date().time + (refreshTokenLifetime ?: 0)))
            .signWith(getKey())
            .compact()
    }

    @Throws(
        MalformedJwtException::class, ExpiredJwtException::class, UnsupportedJwtException::class,
        IllegalArgumentException::class, SignatureException::class
    )
    fun validateAccessToken(token: String) {
        val claims = Jwts
            .parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token).payload
        val tokenType = claims["type"]
        if (tokenType != JwtTokenType.ACCESS.name) {
            throw UnsupportedJwtException("Token type is not ACCESS")
        }
    }

    @Throws(
        MalformedJwtException::class, ExpiredJwtException::class, UnsupportedJwtException::class,
        IllegalArgumentException::class, SignatureException::class
    )
    fun validateRefreshToken(token: String) {
        val claims = Jwts
            .parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token).payload
        val tokenType = claims["type"]
        if (tokenType != JwtTokenType.REFRESH.name) {
            throw UnsupportedJwtException("Token type is not REFRESH")
        }
    }

    fun getUsernameFromToken(token: String): String {
        return Jwts
            .parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token).payload.subject
    }

    private fun getKey(): SecretKey {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
    }
}