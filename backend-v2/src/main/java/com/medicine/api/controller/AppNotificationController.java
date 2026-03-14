package com.medicine.api.controller;

import com.medicine.api.dto.notification.AppNotificationSocketTicketResponse;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.model.CommonResult;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.notification.AppNotificationSocketTicketService;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-api/notifications")
public class AppNotificationController {

    private final AppNotificationSocketTicketService socketTicketService;

    public AppNotificationController(AppNotificationSocketTicketService socketTicketService) {
        this.socketTicketService = socketTicketService;
    }

    @PostMapping("/socket-ticket")
    public CommonResult<AppNotificationSocketTicketResponse> createSocketTicket(Authentication authentication) {
        if (!(authentication.getCredentials() instanceof String token) || !StringUtils.hasText(token)) {
            throw new BusinessException(401, "UNAUTHORIZED");
        }

        JwtClaims claims = authentication.getDetails() instanceof JwtClaims jwtClaims ? jwtClaims : null;
        AppNotificationSocketTicketService.Ticket ticket = socketTicketService.issue(
                token,
                claims == null ? null : claims.getExpiresAt()
        );
        if (ticket == null) {
            throw new BusinessException(401, "UNAUTHORIZED");
        }

        AppNotificationSocketTicketResponse response = new AppNotificationSocketTicketResponse();
        response.setTicket(ticket.value());
        response.setExpiresInSeconds(ticket.expiresInSeconds());
        return CommonResult.ok(response);
    }
}
