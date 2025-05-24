package com.ofya.markdown.blob.server.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.Date

@Entity
@Table(
    name = "markdown_files",
    indexes = [
        Index(name = "idx_user_Id", columnList = "userId"),
    ],
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["userId", "fileName"])
    ]
)
data class MarkdownFile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 512)
    val fileName: String,

    @CreationTimestamp
    @Column(nullable = false)
    val createdAt: Date = Date(),

    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: Date = Date(),
)