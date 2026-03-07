package com.medicine.api.controller;

import com.medicine.api.dto.prescription.FilePresignRequest;
import com.medicine.api.dto.prescription.FilePresignResponse;
import com.medicine.api.dto.prescription.PrescriptionDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.service.admin.AdminPrescriptionService;
import com.medicine.service.app.AppFileService;
import com.medicine.service.auth.JwtClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionControllerTest {

    @Mock
    private AppFileService appFileService;

    @Mock
    private AdminPrescriptionService adminPrescriptionService;

    @InjectMocks
    private AppFileController appFileController;

    @InjectMocks
    private AdminDdiController adminDdiController;

    @Test
    void shouldWrapPresignResultWithCommonResult() {
        FilePresignRequest request = new FilePresignRequest();
        FilePresignResponse response = new FilePresignResponse();
        response.setObjectKey("prescriptions/test.jpg");
        when(appFileService.presign(request)).thenReturn(response);

        CommonResult<FilePresignResponse> result = appFileController.presign(request);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getObjectKey()).isEqualTo("prescriptions/test.jpg");
    }

    @Test
    void shouldPassClaimsToConfirmDdiController() {
        JwtClaims claims = new JwtClaims();
        claims.setTenantId(1L);
        claims.setUserId(100L);
        claims.setAccountType("PROFESSIONAL");
        claims.setRoles(List.of("pharmacists"));

        TestingAuthenticationToken authentication = new TestingAuthenticationToken("pharmacist", "token");
        authentication.setDetails(claims);

        PrescriptionDTO.DdiScreeningDTO dto = new PrescriptionDTO.DdiScreeningDTO();
        dto.setId(1L);
        when(adminPrescriptionService.confirmScreening(claims, 1L, null)).thenReturn(dto);

        CommonResult<PrescriptionDTO.DdiScreeningDTO> result = adminDdiController.confirm(1L, null, authentication);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getId()).isEqualTo(1L);
    }
}
