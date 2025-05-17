package com.ofya.markdown.blob.server.envcontext

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EnvContextConfig(
    @Value("\${env.type}") private val envType: String
) {

    @Bean
    fun envContext(): EnvContext {
        val envContextType = when (envType.uppercase()) {
            "QA" -> EnvContextType.QA
            "PROD" -> EnvContextType.PROD
            else -> throw IllegalArgumentException("Invalid env type")
        }
        return EnvContext(envContextType = envContextType)
    }
}