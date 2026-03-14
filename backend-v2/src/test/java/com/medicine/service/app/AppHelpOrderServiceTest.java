package com.medicine.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.helporder.HelpOrderDTO;
import com.medicine.api.dto.helporder.HelpOrderReplyRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.repository.helporder.HelpOrderEventMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.DoctorProfileMapper;
import com.medicine.repository.identity.PharmacistProfileMapper;
import com.medicine.service.auth.AppPatientService;
import com.medicine.service.notification.NotificationDispatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppHelpOrderServiceTest {

    @Mock
    private HelpOrderMapper helpOrderMapper;

    @Mock
    private HelpOrderEventMapper helpOrderEventMapper;

    @Mock
    private NotificationDispatchService notificationDispatchService;

    @Mock
    private AppPatientService appPatientService;

    @Mock
    private AppAccountMapper appAccountMapper;

    @Mock
    private PharmacistProfileMapper pharmacistProfileMapper;

    @Mock
    private DoctorProfileMapper doctorProfileMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AppHelpOrderService appHelpOrderService;

    @Test
    void shouldCreateHelpOrderWithoutDescriptionAndDispatchNotification() {
        AppPatientService.AccessContext context = accessContext();
        AtomicReference<HelpOrder> insertedOrder = new AtomicReference<>();
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(context);
        when(helpOrderMapper.insert(any(HelpOrder.class))).thenAnswer(invocation -> {
            HelpOrder order = invocation.getArgument(0);
            order.setId(900L);
            insertedOrder.set(order);
            return 1;
        });
        when(helpOrderMapper.selectById(1L, 900L)).thenAnswer(invocation -> insertedOrder.get());
        when(helpOrderEventMapper.selectByHelpOrderId(1L, 900L)).thenReturn(List.of());

        HelpOrderDTO request = new HelpOrderDTO();
        request.setCategory("medication");
        request.setTitle("Need help");
        request.setPriority("urgent");

        HelpOrderDTO result = appHelpOrderService.create("13800138000", request);

        assertThat(result.getId()).isEqualTo(900L);
        assertThat(result.getCategory()).isEqualTo("MEDICATION");
        assertThat(result.getPriority()).isEqualTo("URGENT");
        assertThat(result.getStatus()).isEqualTo("SUBMITTED");
        assertThat(result.getDescription()).isNull();
        verify(notificationDispatchService).createAndDispatch(
                eq(1L),
                eq("app-help-order"),
                eq("PHARMACIST"),
                eq(301L),
                eq("HELP_ORDER_SUBMITTED"),
                eq(900L),
                anyMap(),
                any(LocalDateTime.class)
        );
    }

    @Test
    void shouldRejectHelpOrderDetailForAnotherPatient() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(helpOrderMapper.selectById(1L, 901L)).thenReturn(orderOwnedByAnotherPatient());

        BusinessException exception = catchThrowableOfType(
                () -> appHelpOrderService.getDetail("13800138000", 901L),
                BusinessException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.HELP_ORDER_NOT_FOUND.getCode());
    }

    @Test
    void shouldAppendPatientReplyAndNotifyCurrentHandler() {
        AppPatientService.AccessContext context = accessContext();
        HelpOrder order = ownOrder();
        order.setStatus("PHARMACIST_IN_PROGRESS");
        order.setCurrentHandlerType("PHARMACIST");
        order.setCurrentHandlerId(301L);

        when(appPatientService.requireAccessContext("13800138000")).thenReturn(context);
        when(helpOrderMapper.selectById(1L, 902L)).thenReturn(order);
        when(helpOrderEventMapper.selectByHelpOrderId(1L, 902L)).thenReturn(List.of());

        HelpOrderReplyRequest request = new HelpOrderReplyRequest();
        request.setContent("follow-up question");

        HelpOrderDTO result = appHelpOrderService.reply("13800138000", 902L, request);

        assertThat(result.getId()).isEqualTo(902L);
        verify(notificationDispatchService).createAndDispatch(
                eq(1L),
                eq("app-help-order"),
                eq("PHARMACIST"),
                eq(301L),
                eq("HELP_ORDER_PATIENT_REPLY"),
                eq(902L),
                anyMap(),
                any(LocalDateTime.class)
        );
    }

    @Test
    void shouldRejectPatientReplyWhenOrderClosed() {
        AppPatientService.AccessContext context = accessContext();
        HelpOrder order = ownOrder();
        order.setStatus("CLOSED");

        when(appPatientService.requireAccessContext("13800138000")).thenReturn(context);
        when(helpOrderMapper.selectById(1L, 903L)).thenReturn(order);

        HelpOrderReplyRequest request = new HelpOrderReplyRequest();
        request.setContent("follow-up question");

        BusinessException exception = catchThrowableOfType(
                () -> appHelpOrderService.reply("13800138000", 903L, request),
                BusinessException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.HELP_ORDER_STATUS_CONFLICT.getCode());
    }

    private AppPatientService.AccessContext accessContext() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        account.setMobile("13800138000");
        account.setAccountType("PATIENT");

        Patient patient = new Patient();
        patient.setId(201L);
        patient.setBoundByPharmacistId(301L);

        return new AppPatientService.AccessContext(account, patient, "PATIENT");
    }

    private HelpOrder orderOwnedByAnotherPatient() {
        HelpOrder order = new HelpOrder();
        order.setId(901L);
        order.setPatientId(999L);
        return order;
    }

    private HelpOrder ownOrder() {
        HelpOrder order = new HelpOrder();
        order.setId(902L);
        order.setPatientId(201L);
        order.setSubmittedByType("PATIENT");
        order.setSubmittedById(101L);
        order.setCategory("MEDICATION_CONSULTATION");
        order.setTitle("Need help");
        order.setStatus("SUBMITTED");
        order.setPriority("NORMAL");
        return order;
    }
}
