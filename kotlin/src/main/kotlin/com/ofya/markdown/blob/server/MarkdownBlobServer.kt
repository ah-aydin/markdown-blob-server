package com.ofya.markdown.blob.server

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MarkdownBlobServer

fun main(args: Array<String>) {
    Dotenv
        .configure()
        .ignoreIfMalformed()
        .ignoreIfMissing()
        .load()
        .entries()
        .forEach { entry -> System.setProperty(entry.key, entry.value) }

    runApplication<MarkdownBlobServer>(*args)
}