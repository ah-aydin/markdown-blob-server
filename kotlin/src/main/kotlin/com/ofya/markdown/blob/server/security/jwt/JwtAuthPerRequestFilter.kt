package com.ofya.markdown.blob.server.security.jwt

import com.ofya.markdown.blob.server.security.AuthenticatedUser
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver

class JwtAuthPerRequestFilter(
) :
    OncePerRequestFilter() {

    @Autowired
    private lateinit var jwtUtils: JwtUtils

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
                val userClaims = jwtUtils.validateAccessToken(jwtToken)
                val authenticatedUser = AuthenticatedUser(id = userClaims.id, email = userClaims.email)

                val authentication =
                    UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.authorities)
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