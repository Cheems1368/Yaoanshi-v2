package com.medicine.api.controller;

import com.medicine.api.dto.notification.AdminNotificationSocketTicketResponse;
import com.medicine.api.dto.notification.NotificationLogDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.model.CommonResult;
import com.medicine.common.model.PageResult;
import com.medicine.service.admin.AdminNotificationService;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.notification.AdminNotificationSocketTicketService;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/notifications")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;
    private final AdminNotificationSocketTicketService socketTicketService;

    public AdminNotificationController(
            AdminNotificationService adminNotificationService,
            AdminNotificationSocketTicketService socketTicketService
    ) {
        this.adminNotificationService = adminNotificationService;
        this.socketTicketService = socketTicketService;
    }

    @GetMapping("/logs")
    public CommonResult<PageResult<NotificationLogDTO>> list(
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "deliveryStatus", required = false) String deliveryStatus,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            Authentication authentication
    ) {
        return CommonResult.ok(
                adminNotificationService.list((JwtClaims) authentication.getDetails(), businessType, deliveryStatus, pageNo, pageSize)
        );
    }

    @PostMapping("/socket-ticket")
    public CommonResult<AdminNotificationSocketTicketResponse> createSocketTicket(Authentication authentication) {
        if (!(authentication.getCredentials() instanceof String token) || !StringUtils.hasText(token)) {
            throw new BusinessException(401, "UNAUTHORIZED");
        }

        JwtClaims claims = authentication.getDetails() instanceof JwtClaims jwtClaims ? jwtClaims : null;
        AdminNotificationSocketTicketService.Ticket ticket = socketTicketService.issue(
                token,
                claims == null ? null : claims.getExpiresAt()
        );
        if (ticket == null) {
            throw new BusinessException(401, "UNAUTHORIZED");
        }

        AdminNotificationSocketTicketResponse response = new AdminNotificationSocketTicketResponse();
        response.setTicket(ticket.value());
        response.setExpiresInSeconds(ticket.expiresInSeconds());
        return CommonResult.ok(response);
    }
}
