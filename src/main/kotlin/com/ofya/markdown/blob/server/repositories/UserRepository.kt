package com.ofya.markdown.blob.server.repositories

import com.ofya.markdown.blob.server.entities.User
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Repository
interface UserRepository : CrudRepository<User, Long> {

    fun findByEmail(email: String): Optional<User>

    @Transactional
    @Modifying
    @Query("UPDATE User SET verified = TRUE WHERE id = :userId")
    fun setVerifiedToTrue(userId: Long)
}