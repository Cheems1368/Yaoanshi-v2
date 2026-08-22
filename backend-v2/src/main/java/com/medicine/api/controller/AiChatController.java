package com.medicine.api.controller;

import com.medicine.api.dto.ai.AiChatRequest;
import com.medicine.api.dto.ai.AiChatResponse;
import com.medicine.api.dto.ai.AiConsentGrantRequest;
import com.medicine.api.dto.ai.AiDisclosureDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.service.ai.AiChatService;
import com.medicine.service.ai.AiConsentService;
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
    private final AiConsentService aiConsentService;

    public AiChatController(AiChatService aiChatService, AiConsentService aiConsentService) {
        this.aiChatService = aiChatService;
        this.aiConsentService = aiConsentService;
    }

    @GetMapping("/disclosure")
    public CommonResult<AiDisclosureDTO> getDisclosure(Authentication authentication) {
        return CommonResult.ok(aiConsentService.getDisclosure(authentication.getName()));
    }

    @GetMapping("/consent")
    public CommonResult<Boolean> getConsentStatus(Authentication authentication) {
        return CommonResult.ok(aiConsentService.hasCurrentConsent(authentication.getName()));
    }

    @PostMapping("/consent")
    public CommonResult<Boolean> grantConsent(@RequestBody(required = false) AiConsentGrantRequest request,
                                               Authentication authentication) {
        aiConsentService.grantConsent(authentication.getName(), request);
        return CommonResult.ok(true);
    }

    @PostMapping("/chat")
    public CommonResult<AiChatResponse> chat(@RequestBody(required = false) AiChatRequest request,
                                             Authentication authentication) {
        return CommonResult.ok(aiChatService.chat(authentication.getName(), request));
    }
}
