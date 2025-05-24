package com.ofya.markdown.blob.server.storage.markdown

import com.ofya.markdown.blob.server.envcontext.EnvContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class MarkdownFileStorageConfiguration(
    private val envContext: EnvContext,

    @Value("\${storage.markdown.region}")
    private val region: String,

    @Value("\${storage.markdown.accessKeyId}")
    private val accessKeyId: String,

    @Value("\${storage.markdown.secretKey}")
    private val secretKey: String,

    @Value("\${storage.markdown.serviceEndpoint}")
    private val serviceEndpoint: String,

    @Value("\${storage.markdown.bucketName}")
    private val bucketName: String
) {

    @Bean
    fun markdownFileStorageClient(): MarkdownFileStorageClient {
        val credentials = AwsBasicCredentials
            .builder()
            .accessKeyId(accessKeyId)
            .secretAccessKey(secretKey)
            .build()
        val s3Client = S3Client
            .builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .endpointOverride(java.net.URI.create(serviceEndpoint))
            .region(Region.of(region))
            .build()

        val basePath = when (envContext.isQa()) {
            true -> "qa/"
            false -> "prod/"
        }

        return MarkdownFileStorageClient(s3Client, basePath, bucketName)
    }
}