package com.ofya.markdown.blob.server.storage.markdown

import com.ofya.markdown.blob.server.dtos.error.ServerError
import org.slf4j.LoggerFactory
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse

class MarkdownFileStorageClient(
    private val s3Client: S3Client,
    private val basePath: String,
    private val bucketName: String
) {

    private val log = LoggerFactory.getLogger(MarkdownFileStorageClient::class.java)

    fun doesObjectExist(filePath: String): Boolean {
        val headObjectRequest = HeadObjectRequest
            .builder()
            .bucket(bucketName)
            .key(buildFullPath(filePath))
            .build()

        try {
            s3Client.headObject(headObjectRequest)
            return true
        } catch (_: NoSuchKeyException) {
            return false
        } catch (e: Exception) {
            throw e
        }
    }

    fun putObject(filePath: String, fileBytes: ByteArray): PutObjectResponse {
        val key = buildFullPath(filePath)
        try {
            val putObjectRequest = PutObjectRequest
                .builder()
                .bucket(bucketName)
                .key(key)
                .contentType("file/text")
                .contentLength(fileBytes.size.toLong())
                .build()
            val requestBody = RequestBody.fromBytes(fileBytes)

            return s3Client.putObject(putObjectRequest, requestBody)
        } catch (e: Exception) {
            val message = "Error uploading file to storage. bucketName:$bucketName key:$key"
            log.error(message, e)
            throw ServerError(message)
        }
    }

    fun getObject(filePath: String): ByteArray {
        val key = buildFullPath(filePath)
        try {
            val getObjectRequest = GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(key)
                .build()

            return s3Client
                .getObject(
                    getObjectRequest, ResponseTransformer.toBytes()
                )
                .asByteArray()
        } catch (e: Exception) {
            val message = "Error downloading file from storage. bucketName:$bucketName key:$key"
            log.error(message, e)
            throw ServerError(message)
        }
    }

    fun deleteObject(filePath: String): DeleteObjectsResponse? {
        val key = buildFullPath(filePath)
        try {
            val delete = Delete
                .builder()
                .objects(
                    listOf(
                        ObjectIdentifier
                            .builder()
                            .key(key)
                            .build()
                    )
                )
                .build()

            val deleteObjectsRequest = DeleteObjectsRequest
                .builder()
                .bucket(bucketName)
                .delete(
                    delete
                )
                .build()

            return s3Client.deleteObjects(deleteObjectsRequest)
        } catch (e: Exception) {
            val message = "Error deleting file from storage. bucketName:$bucketName key:$key"
            log.error(message, e)
            throw ServerError(message)
        }
    }

    private fun buildFullPath(filePath: String): String {
        return basePath + filePath
    }
}
