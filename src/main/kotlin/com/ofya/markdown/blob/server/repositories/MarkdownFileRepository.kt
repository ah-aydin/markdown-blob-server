package com.ofya.markdown.blob.server.repositories

import com.ofya.markdown.blob.server.entities.MarkdownFile
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MarkdownFileRepository : JpaRepository<MarkdownFile, Long> {

    fun findByUserId(userId: Long, pageable: Pageable): Page<MarkdownFile>

    @Transactional
    fun deleteByUserIdAndFileName(userId: Long, fileName: String)

    @Query("SELECT COUNT(*) > 0 FROM MarkdownFile WHERE userId = :userId AND fileName = :fileName")
    fun doesFileBelongToUser(userId: Long, fileName: String): Boolean

    @Modifying
    @Transactional
    @Query("UPDATE MarkdownFile SET updatedAt = :updatedAt WHERE userId = :userId AND fileName = :fileName")
    fun updateUpdatedAtByUserIdAndFileName(userId: Long, fileName: String, updatedAt: LocalDateTime)
}