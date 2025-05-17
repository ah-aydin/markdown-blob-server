package com.ofya.markdown.blob.server.controllers.advices

import com.ofya.markdown.blob.server.dtos.error.ClientError
import com.ofya.markdown.blob.server.dtos.error.ClientErrorType
import com.ofya.markdown.blob.server.dtos.error.ServerError
import com.ofya.markdown.blob.server.security.jwt.JwtError
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

typealias ErrorResponseBody = Map<String, Any>
typealias ErrorResponse = ResponseEntity<ErrorResponseBody>

fun buildErrorBody(errorType: Any, message: Any): ErrorResponseBody {
    return mapOf(
        "error_type" to errorType.toString(),
        "message" to message
    )
}

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ClientError::class)
    fun handleClientError(clientError: ClientError): ErrorResponse {
        val responseBody = buildErrorBody(
            clientError.getType(),
            clientError.message
        )
        return ResponseEntity(responseBody, clientError.getStatus())
    }

    @ExceptionHandler(ServerError::class)
    fun handleServerError(serverError: ServerError): ErrorResponse {
        log.error("", serverError)
        val responseBody = buildErrorBody(
            "INTERNAL_ERROR",
            serverError.message
        )
        return ResponseEntity(responseBody, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(authenticationException: AuthenticationException): ErrorResponse {
        val responseBody = when (authenticationException) {
            is DisabledException -> buildErrorBody(ClientErrorType.USER_NOT_VERIFIED, "User is not verified")
            is LockedException -> buildErrorBody(ClientErrorType.USER_NOT_VERIFIED, "User is not verified")
            is BadCredentialsException -> buildErrorBody(
                ClientErrorType.BAD_CREDENTIALS,
                "Wrong email and/or password"
            )

            else -> buildErrorBody("", "Unknown authentication exception")
        }

        return ResponseEntity(responseBody, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(JwtException::class)
    fun handleJwtException(jwtException: JwtException): ErrorResponse {
        val responseBody = when (jwtException) {
            is MalformedJwtException -> buildErrorBody(JwtError.JET_MALFORMED, "JWT is malformed")
            is ExpiredJwtException -> buildErrorBody(JwtError.JWT_EXPIRED, "JWT has expired")
            is UnsupportedJwtException -> buildErrorBody(JwtError.JWT_UNSUPPORTED, "Given JWT is unsupported")
            is SignatureException -> buildErrorBody(JwtError.JWT_WRONG_SIGNATURE, "Given JWT has wrong signature")
            else -> buildErrorBody(JwtError.JWT_UNKNOWN_ERROR, "Unknown JWT error")
        }
        return ResponseEntity(responseBody, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(exception: Exception): ErrorResponse {
        log.error("", exception)
        val responseBody = buildErrorBody(
            "INTERNAL_ERROR",
            exception.message.toString()
        )
        return ResponseEntity(responseBody, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errors =
            ex.bindingResult.fieldErrors.associate { error -> error.field to error.defaultMessage }
        return ResponseEntity(
            mapOf("error_type" to ClientErrorType.INVALID_INPUT, "errors" to errors),
            ClientErrorType.INVALID_INPUT.httpStatus
        )
    }
}