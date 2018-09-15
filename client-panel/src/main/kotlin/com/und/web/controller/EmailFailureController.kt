package com.und.web.controller

import com.und.model.jpa.EmailFailureAuditLog
import com.und.security.utils.AuthenticationUtils
import com.und.service.EmailFailureHandlerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController("/emailerror")
class EmailFailureController {

    @Autowired
    lateinit var emailFailureHandlerService: EmailFailureHandlerService

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/connection")
    fun connectionErrors(): List<EmailFailureAuditLog> {
        val clientId = AuthenticationUtils.clientID
        return clientId?.let { emailFailureHandlerService.connectionErrors(clientId) } ?: emptyList()

    }
}