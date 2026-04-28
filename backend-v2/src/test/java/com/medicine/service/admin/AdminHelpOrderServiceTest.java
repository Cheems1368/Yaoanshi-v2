package com.medicine.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.helporder.HelpOrderCloseRequest;
import com.medicine.api.dto.helporder.HelpOrderDTO;
import com.medicine.api.dto.helporder.HelpOrderReplyRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.helporder.HelpOrderEvent;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
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
    void shouldListDoctorOrdersWithSingleStatusFilter() {
        JwtClaims claims = doctorClaims();
        HelpOrder order = helpOrder("WAITING_DOCTOR", 1L, "DOCTOR", 11L, 11L);

        when(professionalIdentityService.isPharmacist(claims)).thenReturn(false);
        when(professionalIdentityService.isDoctor(claims)).thenReturn(true);
        when(professionalIdentityService.getOrCreateDoctorProfile(claims)).thenReturn(doctor(11L));
        when(helpOrderMapper.countForAdmin(1L, "DOCTOR", 11L, List.of("WAITING_DOCTOR"))).thenReturn(1L);
        when(helpOrderMapper.searchForAdmin(1L, "DOCTOR", 11L, List.of("WAITING_DOCTOR"), 0, 20)).thenReturn(List.of(order));

        var result = adminHelpOrderService.list(claims, "WAITING_DOCTOR", 1, 20);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getStatus()).isEqualTo("WAITING_DOCTOR");
        verify(helpOrderMapper).countForAdmin(1L, "DOCTOR", 11L, List.of("WAITING_DOCTOR"));
        verify(helpOrderMapper).searchForAdmin(1L, "DOCTOR", 11L, List.of("WAITING_DOCTOR"), 0, 20);
    }

    @Test
    void shouldListAdminOrdersWithMultipleFinishedStatuses() {
        JwtClaims claims = doctorClaims();
        HelpOrder closed = helpOrder("CLOSED", 1L, "DOCTOR", 11L, 11L);
        HelpOrder rejected = helpOrder("REJECTED", 1L, "DOCTOR", 11L, 11L);

        when(professionalIdentityService.isPharmacist(claims)).thenReturn(false);
        when(professionalIdentityService.isDoctor(claims)).thenReturn(true);
        when(professionalIdentityService.getOrCreateDoctorProfile(claims)).thenReturn(doctor(11L));
        when(helpOrderMapper.countForAdmin(1L, "DOCTOR", 11L, List.of("CLOSED", "REJECTED"))).thenReturn(2L);
        when(helpOrderMapper.searchForAdmin(1L, "DOCTOR", 11L, List.of("CLOSED", "REJECTED"), 0, 20)).thenReturn(List.of(closed, rejected));

        var result = adminHelpOrderService.list(claims, "CLOSED,REJECTED", 1, 20);

        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getList()).extracting(HelpOrderDTO::getStatus).containsExactly("CLOSED", "REJECTED");
        verify(helpOrderMapper).countForAdmin(1L, "DOCTOR", 11L, List.of("CLOSED", "REJECTED"));
        verify(helpOrderMapper).searchForAdmin(1L, "DOCTOR", 11L, List.of("CLOSED", "REJECTED"), 0, 20);
    }

    @Test
    void shouldListAdminOrdersWithoutStatusFilterWhenStatusIsBlank() {
        JwtClaims claims = pharmacistClaims();

        when(professionalIdentityService.isPharmacist(claims)).thenReturn(true);
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacist(1L));
        when(helpOrderMapper.countForAdmin(1L, "PHARMACIST", 1L, List.of())).thenReturn(0L);
        when(helpOrderMapper.searchForAdmin(1L, "PHARMACIST", 1L, List.of(), 0, 20)).thenReturn(List.of());

        var result = adminHelpOrderService.list(claims, "   ", 1, 20);

        assertThat(result.getTotal()).isZero();
        assertThat(result.getList()).isEmpty();
        verify(helpOrderMapper).countForAdmin(1L, "PHARMACIST", 1L, List.of());
        verify(helpOrderMapper).searchForAdmin(1L, "PHARMACIST", 1L, List.of(), 0, 20);
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
    void shouldAllowDoctorEffectiveRoleToReplyWaitingDoctorOrder() {
        JwtClaims claims = doctorClaims();
        claims.setRoles(List.of("admin"));
        claims.setEffectiveRole("doctor");
        HelpOrderReplyRequest request = new HelpOrderReplyRequest();
        request.setContent("doctor follow-up reply");
        HelpOrder order = helpOrder("WAITING_DOCTOR", 1L, "DOCTOR", 11L, 11L);

        when(professionalIdentityService.isPharmacist(claims)).thenReturn(false);
        when(professionalIdentityService.isDoctor(claims)).thenReturn(true);
        when(professionalIdentityService.getOrCreateDoctorProfile(claims)).thenReturn(doctor(11L));
        when(helpOrderMapper.selectById(1L, 602L)).thenReturn(order);
        when(helpOrderEventMapper.selectByHelpOrderId(1L, order.getId())).thenReturn(List.of());

        HelpOrderDTO result = adminHelpOrderService.reply(claims, 602L, request);

        assertThat(result.getStatus()).isEqualTo("DOCTOR_IN_PROGRESS");
        ArgumentCaptor<HelpOrder> orderCaptor = ArgumentCaptor.forClass(HelpOrder.class);
        verify(helpOrderMapper).updateState(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo("DOCTOR_IN_PROGRESS");
        assertThat(orderCaptor.getValue().getCurrentHandlerType()).isEqualTo("DOCTOR");
        assertThat(orderCaptor.getValue().getCurrentHandlerId()).isEqualTo(11L);

        ArgumentCaptor<HelpOrderEvent> eventCaptor = ArgumentCaptor.forClass(HelpOrderEvent.class);
        verify(helpOrderEventMapper).insert(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo("DOCTOR_REPLY");
        assertThat(eventCaptor.getValue().getFromStatus()).isEqualTo("WAITING_DOCTOR");
        assertThat(eventCaptor.getValue().getToStatus()).isEqualTo("DOCTOR_IN_PROGRESS");
        assertThat(eventCaptor.getValue().getContent()).isEqualTo("doctor follow-up reply");
        verify(notificationDispatchService).createAndDispatch(
                eq(1L),
                eq("admin-help-order"),
                eq("PATIENT"),
                eq(301L),
                eq("HELP_ORDER_REPLY"),
                eq(order.getId()),
                anyMap(),
                any(LocalDateTime.class)
        );
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
