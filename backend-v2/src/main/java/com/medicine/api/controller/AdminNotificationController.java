package com.medicine.api.controller;

import com.medicine.api.dto.notification.NotificationLogDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.common.model.PageResult;
import com.medicine.service.admin.AdminNotificationService;
import com.medicine.service.auth.JwtClaims;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/notifications")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    public AdminNotificationController(AdminNotificationService adminNotificationService) {
        this.adminNotificationService = adminNotificationService;
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
}
