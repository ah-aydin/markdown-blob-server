package com.ofya.markdown.blob.server.services

import com.ofya.markdown.blob.server.dtos.error.ClientError
import com.ofya.markdown.blob.server.dtos.error.ClientErrorType
import com.ofya.markdown.blob.server.entities.MarkdownFile
import com.ofya.markdown.blob.server.repositories.MarkdownFileRepository
import com.ofya.markdown.blob.server.storage.markdown.MarkdownFileStorageClient
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.time.LocalDateTime

@Service
class MarkdownFileStorageService(
    private val markdownFileStorageClient: MarkdownFileStorageClient,
    private val markdownFileRepository: MarkdownFileRepository
) {

    private val log = LoggerFactory.getLogger(MarkdownFileStorageService::class.java)

    fun getFilesForUser(userId: Long, page: Int, size: Int): Page<MarkdownFile> {
        val pageable = PageRequest.of(
            page,
            size,
            Sort
                .by("createdAt")
                .ascending()
        )
        return markdownFileRepository.findByUserId(userId, pageable)
    }

    @Transactional
    fun upload(userId: Long, fileName: String, fileBytes: ByteArray) {
        val filePath = buildFilePathForUser(userId, fileName)
        var isNew = true
        if (markdownFileStorageClient.doesObjectExist(filePath)
        ) {
            isNew = false;
            validateUserAccessToFile(userId, fileName)
        }

        val inputStream = ByteArrayInputStream(fileBytes)

        markdownFileStorageClient.putObject(filePath, fileBytes)
        if (isNew) {
            markdownFileRepository.save(MarkdownFile(userId = userId, fileName = fileName))
        } else {
            markdownFileRepository.updateUpdatedAtByUserIdAndFileName(userId, fileName, LocalDateTime.now())
        }
        log.info("Uploaded file '$filePath' to storage")
    }

    fun download(userId: Long, fileName: String): ByteArray {
        val filePath = buildFilePathForUser(userId, fileName)
        if (!markdownFileStorageClient.doesObjectExist(filePath)) {
            throw ClientError(ClientErrorType.FILE_DOES_NOT_EXIST, "File '$fileName' does not exist")
        }
        validateUserAccessToFile(userId, fileName)

        return markdownFileStorageClient.getObject(filePath)
    }

    @Transactional
    fun delete(userId: Long, fileName: String) {
        val filePath = buildFilePathForUser(userId, fileName)
        if (!markdownFileStorageClient.doesObjectExist(filePath)) {
            throw ClientError(ClientErrorType.FILE_DOES_NOT_EXIST, "File '$fileName' does not exist")
        }
        validateUserAccessToFile(userId, fileName)

        markdownFileStorageClient.deleteObject(filePath)
        markdownFileRepository.deleteByUserIdAndFileName(userId, fileName)
        log.info("Deleted file '$filePath' to storage")
    }

    private fun buildFilePathForUser(userId: Long, fileName: String): String {
        return "$userId/$fileName"
    }

    private fun validateUserAccessToFile(userId: Long, key: String) {
        if (!markdownFileRepository.doesFileBelongToUser(userId, key)) {
            throw ClientError(ClientErrorType.FILE_ACCESS_DENIED, "User does not have access to the file")
        }
    }
}