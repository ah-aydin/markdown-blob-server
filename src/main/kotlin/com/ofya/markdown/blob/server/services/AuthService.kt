package com.ofya.markdown.blob.server.services

import com.ofya.markdown.blob.server.dtos.auth.LoginRequest
import com.ofya.markdown.blob.server.dtos.auth.LoginResponse
import com.ofya.markdown.blob.server.dtos.auth.RefreshRequest
import com.ofya.markdown.blob.server.dtos.auth.RefreshResponse
import com.ofya.markdown.blob.server.dtos.auth.UserResponse
import com.ofya.markdown.blob.server.dtos.auth.UserSignupRequest
import com.ofya.markdown.blob.server.dtos.error.ClientError
import com.ofya.markdown.blob.server.dtos.error.ClientErrorType
import com.ofya.markdown.blob.server.entities.ActivationToken
import com.ofya.markdown.blob.server.entities.User
import com.ofya.markdown.blob.server.repositories.ActivationTokenRepository
import com.ofya.markdown.blob.server.repositories.UserRepository
import com.ofya.markdown.blob.server.security.jwt.JwtUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID

@Service
class AuthService(
    private val activationTokenRepository: ActivationTokenRepository,
    private val authenticationManager: AuthenticationManager,
    private val emailService: EmailService,
    private val jwtUtils: JwtUtils,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository
) {

    @Value("\${spring.auth.verifyTokenLifetime}")
    var verifyTokenLifetime: Long? = null

    fun signup(userSignupRequest: UserSignupRequest): UserResponse {
        if (userRepository.findByEmail(userSignupRequest.email).isPresent) {
            throw ClientError(
                ClientErrorType.USER_ALREADY_IN_SYSTEM,
                "User with email '${userSignupRequest.email}' is already registered"
            )
        }
        val user = userRepository.save(
            User(
                email = userSignupRequest.email,
                passwordHash = passwordEncoder.encode(userSignupRequest.password)
            )
        )

        val activationToken = UUID
            .randomUUID()
            .toString()
        val savedToken = activationTokenRepository.save(
            ActivationToken(
                token = activationToken, userId = user.id, expiresAt =
                    Date(Date().time + (verifyTokenLifetime ?: 0))
            )
        )
        emailService.sendVerificationEmail(user.email, savedToken.token)

        return UserResponse(user.id, user.email, user.getRoles())
    }

    fun login(loginRequest: LoginRequest): LoginResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.email,
                loginRequest.password
            )
        )

        SecurityContextHolder.getContext().authentication = authentication
        val user = authentication.principal as User
        val accessToken = jwtUtils.generateAccessTokenForUser(user)
        val refreshToken = jwtUtils.generateRefreshTokenForUser(user)
        val roles = user.authorities
            .map { it -> it.authority }
            .toList()

        return LoginResponse(accessToken, refreshToken, roles)
    }

    fun refresh(refreshRequest: RefreshRequest): RefreshResponse {
        val userClaims = jwtUtils.validateRefreshToken(refreshRequest.refreshToken)
        val user = userRepository
            .findById(userClaims.id)
            // TODO create precondition utils
            .get()

        val accessToken = jwtUtils.generateAccessTokenForUser(user)

        return RefreshResponse(accessToken)
    }

    fun verify(token: String) {
        val activationTokenMaybe = activationTokenRepository.findByToken(token)
        if (activationTokenMaybe.isEmpty) {
            throw ClientError(
                ClientErrorType.INVALID_INPUT,
                "Given token is not valid"
            )
        }

        val activationToken = activationTokenMaybe.get()
        val userId = activationToken.userId

        if (activationToken.isExpired()) {
            activationTokenRepository.deleteById(activationToken.id)
            userRepository.deleteById(userId)
            throw ClientError(
                ClientErrorType.EXPIRED,
                "The activation token has expired"
            )
        }
        activationTokenRepository.deleteById(activationToken.id)
        userRepository.setVerifiedToTrue(userId)
    }
}