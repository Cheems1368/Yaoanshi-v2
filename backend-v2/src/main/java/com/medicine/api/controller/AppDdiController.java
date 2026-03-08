package com.medicine.api.controller;

import com.medicine.api.dto.ddi.AppDdiScreenRequest;
import com.medicine.api.dto.ddi.AppDdiScreenResult;
import com.medicine.common.model.CommonResult;
import com.medicine.service.app.AppDdiScreeningService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-api/ddi")
public class AppDdiController {

    private final AppDdiScreeningService appDdiScreeningService;

    public AppDdiController(AppDdiScreeningService appDdiScreeningService) {
        this.appDdiScreeningService = appDdiScreeningService;
    }

    @PostMapping("/screen")
    public CommonResult<AppDdiScreenResult> screen(
            @RequestBody(required = false) AppDdiScreenRequest request,
            Authentication authentication
    ) {
        return CommonResult.ok(appDdiScreeningService.screenForSubject(authentication.getName(), request));
    }
}
