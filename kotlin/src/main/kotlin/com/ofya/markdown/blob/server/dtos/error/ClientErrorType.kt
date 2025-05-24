package com.ofya.markdown.blob.server.dtos.error

import org.springframework.http.HttpStatus

enum class ClientErrorType(val httpStatus: HttpStatus) {
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    EXPIRED(HttpStatus.GONE),
    FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN),
    FILE_DOES_NOT_EXIST(HttpStatus.NOT_FOUND),
    INVALID_INPUT(HttpStatus.BAD_REQUEST),
    USER_NOT_VERIFIED(HttpStatus.UNAUTHORIZED),
    USER_ALREADY_IN_SYSTEM(HttpStatus.CONFLICT),
}