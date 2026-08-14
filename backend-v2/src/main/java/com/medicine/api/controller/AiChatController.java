package com.medicine.api.controller;

import com.medicine.api.dto.ai.AiChatRequest;
import com.medicine.api.dto.ai.AiChatResponse;
import com.medicine.common.model.CommonResult;
import com.medicine.service.ai.AiChatService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @GetMapping("/consent")
    public CommonResult<Boolean> getConsentStatus(Authentication authentication) {
        return CommonResult.ok(aiChatService.checkConsentStatus(authentication.getName()));
    }

    @PostMapping("/consent")
    public CommonResult<Boolean> grantConsent(Authentication authentication) {
        aiChatService.grantConsent(authentication.getName());
        return CommonResult.ok(true);
    }

    @PostMapping("/chat")
    public CommonResult<AiChatResponse> chat(@RequestBody(required = false) AiChatRequest request,
                                             Authentication authentication) {
        return CommonResult.ok(aiChatService.chat(authentication.getName(), request));
    }
}
