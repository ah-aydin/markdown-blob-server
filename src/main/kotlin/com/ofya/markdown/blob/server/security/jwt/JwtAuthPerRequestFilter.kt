package com.ofya.markdown.blob.server.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver

class JwtAuthPerRequestFilter(
) :
    OncePerRequestFilter() {

    @Autowired
    private lateinit var jwtUtils: JwtUtils

    @Autowired
    private lateinit var userDetailsService: UserDetailsService

    @Autowired
    private lateinit var handlerExceptionResolver: HandlerExceptionResolver

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwtToken = jwtUtils.getJwtFromHeader(request);
            if (jwtToken != null) {
                jwtUtils.validateAccessToken(jwtToken)
                val username = jwtUtils.getUsernameFromToken(jwtToken)
                // TODO get the info from here by JWT claims
                val userDetails = userDetailsService.loadUserByUsername(username)
                // TODO pass an `AuthenticatedUser` that extends `UserDetails` to include the `userId` for use in `AuthUtils.kt`
                val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                val context = SecurityContextHolder.createEmptyContext()
                context.authentication = authentication
                SecurityContextHolder.setContext(context)
            }
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            handlerExceptionResolver.resolveException(request, response, null, e)
        }
    }
}