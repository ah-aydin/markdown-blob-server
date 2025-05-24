package com.ofya.markdown.blob.server.repositories

import com.ofya.markdown.blob.server.entities.ActivationToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ActivationTokenRepository : JpaRepository<ActivationToken, Long> {

    fun findByToken(token: String): Optional<ActivationToken>
}