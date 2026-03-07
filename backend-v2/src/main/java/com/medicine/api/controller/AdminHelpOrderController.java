package com.medicine.api.controller;

import com.medicine.api.dto.helporder.HelpOrderCloseRequest;
import com.medicine.api.dto.helporder.HelpOrderDTO;
import com.medicine.api.dto.helporder.HelpOrderEscalateRequest;
import com.medicine.api.dto.helporder.HelpOrderRejectRequest;
import com.medicine.api.dto.helporder.HelpOrderReplyRequest;
import com.medicine.common.model.CommonResult;
import com.medicine.common.model.PageResult;
import com.medicine.service.admin.AdminHelpOrderService;
import com.medicine.service.auth.JwtClaims;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api/help-orders")
public class AdminHelpOrderController {

    private final AdminHelpOrderService adminHelpOrderService;

    public AdminHelpOrderController(AdminHelpOrderService adminHelpOrderService) {
        this.adminHelpOrderService = adminHelpOrderService;
    }

    @GetMapping
    public CommonResult<PageResult<HelpOrderDTO>> list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            Authentication authentication
    ) {
        return CommonResult.ok(adminHelpOrderService.list((JwtClaims) authentication.getDetails(), status, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public CommonResult<HelpOrderDTO> detail(@PathVariable("id") Long id, Authentication authentication) {
        return CommonResult.ok(adminHelpOrderService.getDetail((JwtClaims) authentication.getDetails(), id));
    }

    @PostMapping("/{id}/claim")
    public CommonResult<HelpOrderDTO> claim(@PathVariable("id") Long id, Authentication authentication) {
        return CommonResult.ok(adminHelpOrderService.claim((JwtClaims) authentication.getDetails(), id));
    }

    @PostMapping("/{id}/reply")
    public CommonResult<HelpOrderDTO> reply(
            @PathVariable("id") Long id,
            @RequestBody HelpOrderReplyRequest request,
            Authentication authentication
    ) {
        return CommonResult.ok(adminHelpOrderService.reply((JwtClaims) authentication.getDetails(), id, request));
    }

    @PostMapping("/{id}/escalate")
    public CommonResult<HelpOrderDTO> escalate(
            @PathVariable("id") Long id,
            @RequestBody HelpOrderEscalateRequest request,
            Authentication authentication
    ) {
        return CommonResult.ok(adminHelpOrderService.escalate((JwtClaims) authentication.getDetails(), id, request));
    }

    @PostMapping("/{id}/reject")
    public CommonResult<HelpOrderDTO> reject(
            @PathVariable("id") Long id,
            @RequestBody HelpOrderRejectRequest request,
            Authentication authentication
    ) {
        return CommonResult.ok(adminHelpOrderService.reject((JwtClaims) authentication.getDetails(), id, request));
    }

    @PostMapping("/{id}/close")
    public CommonResult<HelpOrderDTO> close(
            @PathVariable("id") Long id,
            @RequestBody HelpOrderCloseRequest request,
            Authentication authentication
    ) {
        return CommonResult.ok(adminHelpOrderService.close((JwtClaims) authentication.getDetails(), id, request));
    }
}
