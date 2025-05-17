package com.ofya.markdown.blob.server.security.jwt

import com.ofya.markdown.blob.server.entities.User
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

    fun generateAccessTokenForUser(user: User): String {
        return generateTokenForUser(user, JwtTokenType.ACCESS, (accessTokenLifetime ?: 0))
    }

    fun generateRefreshTokenForUser(user: User): String {
        return generateTokenForUser(user, JwtTokenType.REFRESH, (refreshTokenLifetime ?: 0))
    }

    private fun generateTokenForUser(user: User, jwtTokenType: JwtTokenType, tokenLifetime: Long): String {
        return Jwts
            .builder()
            .subject(user.username)
            .issuedAt(Date())
            .claim("type", jwtTokenType)
            .claim("user_id", user.id)
            .expiration(Date(Date().time + tokenLifetime))
            .signWith(getKey())
            .compact()
    }

    @Throws(
        MalformedJwtException::class, ExpiredJwtException::class, UnsupportedJwtException::class,
        IllegalArgumentException::class, SignatureException::class
    )
    fun validateAccessToken(token: String): UserClaims {
        return validateToken(token, JwtTokenType.ACCESS)
    }

    @Throws(
        MalformedJwtException::class, ExpiredJwtException::class, UnsupportedJwtException::class,
        IllegalArgumentException::class, SignatureException::class
    )
    fun validateRefreshToken(token: String): UserClaims {
        return validateToken(token, JwtTokenType.REFRESH)
    }

    private fun validateToken(token: String, jwtTokenType: JwtTokenType): UserClaims {
        val claims = Jwts
            .parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token).payload

        val tokenType = claims["type"]
        if (tokenType != jwtTokenType.name) {
            throw UnsupportedJwtException("Token type is not REFRESH")
        }

        val userId = claims["user_id"] as Int
        val username = claims.subject
        return UserClaims(id = userId.toLong(), email = username)
    }

    private fun getKey(): SecretKey {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
    }
}