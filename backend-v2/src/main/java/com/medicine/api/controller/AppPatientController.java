package com.medicine.api.controller;

import com.medicine.api.dto.auth.BindPatientRequest;
import com.medicine.api.dto.identity.CreateFamilyBindingRequest;
import com.medicine.common.model.CommonResult;
import com.medicine.service.auth.AppPatientService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app-api/patients")
public class AppPatientController {

    private final AppPatientService appPatientService;

    public AppPatientController(AppPatientService appPatientService) {
        this.appPatientService = appPatientService;
    }

    @PostMapping("/bind")
    public CommonResult<Map<String, Object>> bindPatient(@RequestBody BindPatientRequest request, Authentication authentication) {
        return CommonResult.ok(appPatientService.bindPatient(authentication.getName(), request));
    }

    @GetMapping("/me")
    public CommonResult<Map<String, Object>> me(Authentication authentication) {
        return CommonResult.ok(appPatientService.getCurrentPatient(authentication.getName()));
    }

    @GetMapping("/me/family-bindings")
    public CommonResult<List<Map<String, Object>>> familyBindings(Authentication authentication) {
        return CommonResult.ok(appPatientService.getFamilyBindings(authentication.getName()));
    }

    @PostMapping("/me/family-bindings")
    public CommonResult<Map<String, Object>> createFamilyBinding(
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
