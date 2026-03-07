package com.medicine.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.helporder.HelpOrderCloseRequest;
import com.medicine.api.dto.helporder.HelpOrderReplyRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.identity.DoctorProfile;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.repository.helporder.HelpOrderEventMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.DoctorProfileMapper;
import com.medicine.repository.identity.PharmacistProfileMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import com.medicine.service.notification.NotificationDispatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminHelpOrderServiceTest {

    @Mock
    private HelpOrderMapper helpOrderMapper;

    @Mock
    private HelpOrderEventMapper helpOrderEventMapper;

    @Mock
    private NotificationDispatchService notificationDispatchService;

    @Mock
    private ProfessionalIdentityService professionalIdentityService;

    @Mock
    private PharmacistProfileMapper pharmacistProfileMapper;

    @Mock
    private DoctorProfileMapper doctorProfileMapper;

    @Mock
    private AppAccountMapper appAccountMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AdminHelpOrderService adminHelpOrderService;

    @Test
    void shouldRejectClaimWhenAnotherPharmacistOwnsInProgressOrder() {
        JwtClaims claims = pharmacistClaims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacist(1L));
        when(helpOrderMapper.selectById(1L, 501L)).thenReturn(helpOrder("PHARMACIST_IN_PROGRESS", 2L, "PHARMACIST", 2L, null));

        assertThatThrownBy(() -> adminHelpOrderService.claim(claims, 501L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("HELP_ORDER_PERMISSION_DENIED");

        verify(helpOrderMapper, never()).updateState(any(HelpOrder.class));
    }

    @Test
    void shouldRejectDoctorReplyBeforeEscalation() {
        JwtClaims claims = doctorClaims();
        HelpOrderReplyRequest request = new HelpOrderReplyRequest();
        request.setContent("doctor reply");

        when(professionalIdentityService.isPharmacist(claims)).thenReturn(false);
        when(professionalIdentityService.isDoctor(claims)).thenReturn(true);
        when(professionalIdentityService.getOrCreateDoctorProfile(claims)).thenReturn(doctor(11L));
        when(helpOrderMapper.selectById(1L, 601L)).thenReturn(helpOrder("PHARMACIST_IN_PROGRESS", 1L, "PHARMACIST", 1L, 11L));

        assertThatThrownBy(() -> adminHelpOrderService.reply(claims, 601L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("HELP_ORDER_STATUS_CONFLICT");
    }

    @Test
    void shouldRejectCloseWhenOperatorIsNotCurrentHandler() {
        JwtClaims claims = doctorClaims();
        HelpOrderCloseRequest request = new HelpOrderCloseRequest();
        request.setNote("close");

        when(professionalIdentityService.isPharmacist(claims)).thenReturn(false);
        when(professionalIdentityService.isDoctor(claims)).thenReturn(true);
        when(professionalIdentityService.getOrCreateDoctorProfile(claims)).thenReturn(doctor(11L));
        when(helpOrderMapper.selectById(1L, 701L)).thenReturn(helpOrder("DOCTOR_IN_PROGRESS", 1L, "DOCTOR", 12L, 11L));

        assertThatThrownBy(() -> adminHelpOrderService.close(claims, 701L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("HELP_ORDER_PERMISSION_DENIED");
    }

    private JwtClaims pharmacistClaims() {
        JwtClaims claims = new JwtClaims();
        claims.setTenantId(1L);
        claims.setUserId(100L);
        claims.setAccountType("PROFESSIONAL");
        claims.setRoles(List.of("pharmacists"));
        return claims;
    }

    private JwtClaims doctorClaims() {
        JwtClaims claims = new JwtClaims();
        claims.setTenantId(1L);
        claims.setUserId(101L);
        claims.setAccountType("PROFESSIONAL");
        claims.setRoles(List.of("doctor"));
        return claims;
    }

    private PharmacistProfile pharmacist(Long id) {
        PharmacistProfile profile = new PharmacistProfile();
        profile.setId(id);
        profile.setTenantId(1L);
        return profile;
    }

    private DoctorProfile doctor(Long id) {
        DoctorProfile profile = new DoctorProfile();
        profile.setId(id);
        profile.setTenantId(1L);
        return profile;
    }

    private HelpOrder helpOrder(String status, Long responsiblePharmacistId, String currentHandlerType, Long currentHandlerId, Long doctorId) {
        HelpOrder order = new HelpOrder();
        order.setId(801L);
        order.setTenantId(1L);
        order.setPatientId(301L);
        order.setPatientName("Test Patient");
        order.setStatus(status);
        order.setResponsiblePharmacistId(responsiblePharmacistId);
        order.setCurrentHandlerType(currentHandlerType);
        order.setCurrentHandlerId(currentHandlerId);
        order.setAssignedDoctorId(doctorId);
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
}
