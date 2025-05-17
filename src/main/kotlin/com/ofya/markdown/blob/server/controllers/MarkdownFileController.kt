package com.ofya.markdown.blob.server.controllers

import com.ofya.markdown.blob.server.controllers.utils.getAuthedUserId
import com.ofya.markdown.blob.server.entities.MarkdownFile
import com.ofya.markdown.blob.server.services.MarkdownFileStorageService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/markdown-file-storage")
class MarkdownFileController(
    val markdownFileStorageService: MarkdownFileStorageService
) {

    @GetMapping
    fun getFiles(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<Page<MarkdownFile>> {
        return ResponseEntity.ok(
            markdownFileStorageService.getFilesForUser(
                getAuthedUserId(),
                page,
                size
            )
        )
    }

    @PostMapping("/{fileName}")
    fun upload(@PathVariable fileName: String, @RequestBody fileBytes: ByteArray): ResponseEntity<Void> {
        markdownFileStorageService.upload(getAuthedUserId(), fileName, fileBytes)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build()
    }

    @GetMapping("/{fileName}")
    fun download(@PathVariable fileName: String): ResponseEntity<ByteArray> {
        return ResponseEntity.ok(markdownFileStorageService.download(getAuthedUserId(), fileName))
    }

    @DeleteMapping("/{fileName}")
    fun delete(@PathVariable fileName: String): ResponseEntity<Void> {
        markdownFileStorageService.delete(getAuthedUserId(), fileName)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build()
    }
}