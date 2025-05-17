package com.ofya.markdown.blob.server.storage.markdown

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectResult
import com.amazonaws.util.IOUtils
import com.ofya.markdown.blob.server.dtos.error.ServerError
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream

class MarkdownFileStorageClient(
    private val amazonS3Client: AmazonS3,
    private val basePath: String,
    private val bucketName: String
) {

    private val logger = LoggerFactory.getLogger(MarkdownFileStorageClient::class.java)

    fun doesObjectExist(filePath: String): Boolean {
        return amazonS3Client.doesObjectExist(bucketName, buildFullPath(filePath))
    }

    fun putObject(filePath: String, inputStream: ByteArrayInputStream, metadata: ObjectMetadata): PutObjectResult {
        val key = buildFullPath(filePath)
        try {
            return amazonS3Client.putObject(bucketName, key, inputStream, metadata)
        } catch (e: Exception) {
            val message = "Error uploading file to storage. bucketName:$bucketName key:$key"
            logger.error(message, e)
            throw ServerError(message)
        }
    }

    fun getObject(filePath: String): ByteArray {
        val key = buildFullPath(filePath)
        try {
            return IOUtils.toByteArray(amazonS3Client.getObject(bucketName, key).objectContent)
        } catch (e: Exception) {
            val message = "Error downloading file from storage. bucketName:$bucketName key:$key"
            logger.error(message, e)
            throw ServerError(message)
        }
    }

    fun deleteObject(filePath: String) {
        val key = buildFullPath(filePath)
        try {
            amazonS3Client.deleteObject(bucketName, key)
        } catch (e: Exception) {
            val message = "Error deleting file from storage. bucketName:$bucketName key:$key"
            logger.error(message, e)
            throw ServerError(message)
        }
    }

    private fun buildFullPath(filePath: String): String {
        return basePath + filePath
    }
}
