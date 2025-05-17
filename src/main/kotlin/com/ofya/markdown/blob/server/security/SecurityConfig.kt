package com.ofya.markdown.blob.server.security

import com.ofya.markdown.blob.server.repositories.UserRepository
import com.ofya.markdown.blob.server.security.jwt.JwtAuthPerRequestFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val userRepository: UserRepository
) {

    @Bean
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { authorizeHttpRequests ->
            authorizeHttpRequests
                .requestMatchers("/api/v1/auth/**")
                .permitAll()
            authorizeHttpRequests
                .anyRequest()
                .authenticated()
        }

        http.addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter::class.java)

        http.cors { configurer -> configurer.disable() }
        http.csrf { configurer -> configurer.disable() }

        http.sessionManagement { configurer ->
            configurer.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
            )
        }
        return http.build()
    }

    // TODO remove this if DB verification for login is removed
    @Bean
    fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username ->
            userRepository
                .findByEmail(username)
                .orElseThrow { UsernameNotFoundException("Use $username not found") }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    // TODO remove this if DB verification for login is removed
    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val authProvider = DaoAuthenticationProvider(userDetailsService())
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun jwtTokenFilter(): JwtAuthPerRequestFilter {
        return JwtAuthPerRequestFilter()
    }
}