package com.ofya.markdown.blob.server.dtos.error

class ServerError(override val message: String) : RuntimeException(message)