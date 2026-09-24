package com.medicine.api.controller;

import com.medicine.api.dto.notification.AppNotificationSocketTicketResponse;
import com.medicine.api.dto.notification.NotificationLogDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.model.CommonResult;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.notification.AppNotificationSocketTicketService;
import com.medicine.service.app.AppNotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/app-api/notifications")
public class AppNotificationController {

    private final AppNotificationSocketTicketService socketTicketService;
    private final AppNotificationService appNotificationService;

    public AppNotificationController(AppNotificationSocketTicketService socketTicketService, AppNotificationService appNotificationService) {
        this.socketTicketService = socketTicketService;
        this.appNotificationService = appNotificationService;
    }

    @GetMapping("/logs")
    public CommonResult<List<NotificationLogDTO>> logs(
            @RequestParam(value = "afterId", required = false) Long afterId,
            @RequestParam(value = "limit", defaultValue = "50") int limit,
            Authentication authentication
    ) {
        return CommonResult.ok(appNotificationService.list(authentication.getName(), afterId, limit));
    }

    @GetMapping("/logs/latest-id")
    public CommonResult<Long> latestLogId(Authentication authentication) {
        return CommonResult.ok(appNotificationService.latestId(authentication.getName()));
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
