package com.ofya.markdown.blob.server.services

import org.springframework.stereotype.Service

@Service
//class EmailService(private val mailSender: JavaMailSender) {
class EmailService {

    fun sendVerificationEmail(recipientEmail: String, verificationToken: String) {
        println("NOT SENDING EMAIL")
//        val message = SimpleMailMessage()
//        message.setTo(recipientEmail)
//        message.subject = "Verify you account"
//        message.text = "Use the following verification code to activate you're account: $verificationToken"
//        message.from = "noreply@blobdrive.com"
//        mailSender.send(message)
    }
}