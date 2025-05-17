package com.ofya.markdown.blob.server.dtos.error

import org.springframework.http.HttpStatus

class ClientError(private val clientErrorType: ClientErrorType, override val message: String) :
    RuntimeException(message) {

    fun getStatus(): HttpStatus {
        return clientErrorType.httpStatus
    }

    fun getType(): ClientErrorType {
        return clientErrorType
    }
}