package com.medicine.api.controller;

import com.medicine.api.dto.prescription.FilePresignRequest;
import com.medicine.api.dto.prescription.FilePresignResponse;
import com.medicine.common.model.CommonResult;
import com.medicine.service.app.AppFileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-api/files")
public class AppFileController {

    private final AppFileService appFileService;

    public AppFileController(AppFileService appFileService) {
        this.appFileService = appFileService;
    }

    @PostMapping("/presign")
    public CommonResult<FilePresignResponse> presign(@RequestBody FilePresignRequest request) {
        return CommonResult.ok(appFileService.presign(request));
    }
}
