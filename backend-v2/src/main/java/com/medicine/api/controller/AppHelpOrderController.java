package com.medicine.api.controller;

import com.medicine.api.dto.helporder.HelpOrderDTO;
import com.medicine.api.dto.helporder.HelpOrderReplyRequest;
import com.medicine.common.model.CommonResult;
import com.medicine.common.model.PageResult;
import com.medicine.service.app.AppHelpOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/app-api/help-orders")
public class AppHelpOrderController {

    private final AppHelpOrderService appHelpOrderService;

    public AppHelpOrderController(AppHelpOrderService appHelpOrderService) {
        this.appHelpOrderService = appHelpOrderService;
    }

    @PostMapping
    public CommonResult<HelpOrderDTO> create(@RequestBody HelpOrderDTO request, Principal principal) {
        return CommonResult.ok(appHelpOrderService.create(principal.getName(), request));
    }

    @GetMapping
    public CommonResult<PageResult<HelpOrderDTO>> list(
            Principal principal,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        return CommonResult.ok(appHelpOrderService.list(principal.getName(), status, category, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public CommonResult<HelpOrderDTO> detail(@PathVariable("id") Long id, Principal principal) {
        return CommonResult.ok(appHelpOrderService.getDetail(principal.getName(), id));
    }

    @PostMapping("/{id}/reply")
    public CommonResult<HelpOrderDTO> reply(
            @PathVariable("id") Long id,
            @RequestBody HelpOrderReplyRequest request,
            Principal principal
    ) {
        return CommonResult.ok(appHelpOrderService.reply(principal.getName(), id, request));
    }
}
