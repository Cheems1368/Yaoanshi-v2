package com.medicine.api.controller;

import com.medicine.api.dto.auth.BindPatientRequest;
import com.medicine.api.dto.identity.AppFamilyBindingDTO;
import com.medicine.api.dto.identity.AppPatientHealthProfileDTO;
import com.medicine.api.dto.identity.AppPatientHealthProfileUpdateRequest;
import com.medicine.api.dto.identity.AppPatientProfileDTO;
import com.medicine.api.dto.identity.AppPatientProfileUpdateRequest;
import com.medicine.api.dto.identity.AppPatientSummaryDTO;
import com.medicine.api.dto.identity.CreateFamilyBindingRequest;
import com.medicine.common.model.CommonResult;
import com.medicine.service.app.AppPatientProfileService;
import com.medicine.service.auth.AppPatientService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/app-api/patients")
public class AppPatientController {

    private final AppPatientService appPatientService;
    private final AppPatientProfileService appPatientProfileService;

    public AppPatientController(AppPatientService appPatientService, AppPatientProfileService appPatientProfileService) {
        this.appPatientService = appPatientService;
        this.appPatientProfileService = appPatientProfileService;
    }

    @PostMapping("/bind")
    public CommonResult<AppPatientSummaryDTO> bindPatient(@RequestBody BindPatientRequest request, Authentication authentication) {
        return CommonResult.ok(appPatientService.bindPatient(authentication.getName(), request));
    }

    @GetMapping("/me")
    public CommonResult<AppPatientSummaryDTO> me(Authentication authentication) {
        return CommonResult.ok(appPatientService.getCurrentPatient(authentication.getName()));
    }

    @GetMapping("/me/profile")
    public CommonResult<AppPatientProfileDTO> profile(Authentication authentication) {
        return CommonResult.ok(appPatientProfileService.getProfile(authentication.getName()));
    }

    @PutMapping("/me/profile")
    public CommonResult<AppPatientProfileDTO> updateProfile(
            @RequestBody AppPatientProfileUpdateRequest request,
            Authentication authentication
    ) {
        return CommonResult.ok(appPatientProfileService.updateProfile(authentication.getName(), request));
    }

    @GetMapping("/me/health-profile")
    public CommonResult<AppPatientHealthProfileDTO> healthProfile(Authentication authentication) {
        return CommonResult.ok(appPatientProfileService.getHealthProfile(authentication.getName()));
    }

    @PutMapping("/me/health-profile")
    public CommonResult<AppPatientHealthProfileDTO> updateHealthProfile(
            @RequestBody AppPatientHealthProfileUpdateRequest request,
            Authentication authentication
    ) {
        return CommonResult.ok(appPatientProfileService.updateHealthProfile(authentication.getName(), request));
    }

    @GetMapping("/me/family-bindings")
    public CommonResult<List<AppFamilyBindingDTO>> familyBindings(Authentication authentication) {
        return CommonResult.ok(appPatientService.getFamilyBindings(authentication.getName()));
    }

    @PostMapping("/me/family-bindings")
    public CommonResult<AppFamilyBindingDTO> createFamilyBinding(
            @RequestBody CreateFamilyBindingRequest request,
            Authentication authentication
    ) {
        return CommonResult.ok(appPatientService.createFamilyBinding(authentication.getName(), request));
    }

    @DeleteMapping("/me/family-bindings/{id}")
    public CommonResult<Boolean> unbindFamilyBinding(@PathVariable("id") Long id, Authentication authentication) {
        return CommonResult.ok(appPatientService.unbindFamilyBinding(authentication.getName(), id));
    }
}
