package com.ofya.markdown.blob.server.envcontext

data class EnvContext(
    private val envContextType: EnvContextType
) {

    fun isQa(): Boolean {
        return envContextType == EnvContextType.QA
    }

    fun isProd(): Boolean {
        return envContextType == EnvContextType.PROD
    }
}