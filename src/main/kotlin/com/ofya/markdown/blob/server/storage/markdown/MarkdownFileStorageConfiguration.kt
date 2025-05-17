package com.ofya.markdown.blob.server.storage.markdown

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.ofya.markdown.blob.server.envcontext.EnvContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
        val credentials = BasicAWSCredentials(accessKeyId, secretKey)
        val amazonS3Client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(
                AwsClientBuilder.EndpointConfiguration(
                    serviceEndpoint,
                    region
                )
            )
            .build()

        val basePath = when (envContext.isQa()) {
            true -> "qa/"
            false -> "prod/"
        }

        return MarkdownFileStorageClient(amazonS3Client, basePath, bucketName)
    }
}