package com.ofya.markdown.blob.server.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.util.Date

@Entity
@Table(
    name = "activation_tokens", indexes = [
        Index(name = "idx_expires_at", columnList = "expiresAt")
    ]
)
data class ActivationToken(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val token: String,

    @Column(nullable = false)
    val userId: Long,

    @CreationTimestamp
    @Column(nullable = false)
    val createdAt: Date = Date(),

    @Column(nullable = false)
    val expiresAt: Date = Date()
) {

    fun isExpired(): Boolean {
        return expiresAt.before(Date())
    }
}