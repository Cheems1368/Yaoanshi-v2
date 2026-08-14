package com.medicine.service.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.api.dto.helporder.HelpOrderDTO;
import com.medicine.api.dto.helporder.HelpOrderReplyRequest;
import com.medicine.api.dto.helporder.HelpOrderVoiceAttachmentRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.file.FileAsset;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.helporder.HelpOrderEvent;
import com.medicine.domain.helporder.HelpOrderEventAttachment;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.repository.file.FileAssetMapper;
import com.medicine.repository.helporder.HelpOrderEventAttachmentMapper;
import com.medicine.repository.helporder.HelpOrderEventMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.identity.AppAccountMapper;
import com.medicine.repository.identity.DoctorProfileMapper;
import com.medicine.repository.identity.PharmacistProfileMapper;
import com.medicine.service.auth.AppPatientService;
import com.medicine.service.helporder.HelpOrderVoiceTranscriptionService;
import com.medicine.service.notification.NotificationDispatchService;
import com.medicine.service.storage.S3CompatibleObjectStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppHelpOrderServiceTest {

    @Mock
    private HelpOrderMapper helpOrderMapper;

    @Mock
    private HelpOrderEventMapper helpOrderEventMapper;

    @Mock
    private HelpOrderEventAttachmentMapper helpOrderEventAttachmentMapper;

    @Mock
    private FileAssetMapper fileAssetMapper;

    @Mock
    private S3CompatibleObjectStorage objectStorage;

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

    @Mock
    private HelpOrderVoiceTranscriptionService voiceTranscriptionService;

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
        assertThat(result.getDescription()).isEmpty();
        ArgumentCaptor<HelpOrderEvent> eventCaptor = ArgumentCaptor.forClass(HelpOrderEvent.class);
        verify(helpOrderEventMapper).insert(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getContent()).isEmpty();
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
    void shouldCreateHelpOrderWithUpToThreeOwnedImages() {
        AppPatientService.AccessContext context = accessContext();
        AtomicReference<HelpOrder> insertedOrder = new AtomicReference<>();
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(context);
        when(fileAssetMapper.selectByIdAndTenant(1L, 10L)).thenReturn(helpOrderImage(10L, 101L));
        when(fileAssetMapper.selectByIdAndTenant(1L, 11L)).thenReturn(helpOrderImage(11L, 101L));
        when(helpOrderMapper.insert(any(HelpOrder.class))).thenAnswer(invocation -> {
            HelpOrder order = invocation.getArgument(0);
            order.setId(910L);
            insertedOrder.set(order);
            return 1;
        });
        when(helpOrderEventMapper.insert(any(HelpOrderEvent.class))).thenAnswer(invocation -> {
            HelpOrderEvent event = invocation.getArgument(0);
            event.setId(710L);
            return 1;
        });
        when(helpOrderMapper.selectById(1L, 910L)).thenAnswer(invocation -> insertedOrder.get());
        when(helpOrderEventMapper.selectByHelpOrderId(1L, 910L)).thenReturn(List.of());

        HelpOrderDTO request = new HelpOrderDTO();
        request.setCategory("ADVERSE_REACTION");
        request.setTitle("服药后皮下淤血");
        request.setAttachmentFileAssetIds(List.of(10L, 11L));

        HelpOrderDTO result = appHelpOrderService.create("13800138000", request);

        assertThat(result.getId()).isEqualTo(910L);
        ArgumentCaptor<HelpOrderEventAttachment> attachmentCaptor = ArgumentCaptor.forClass(HelpOrderEventAttachment.class);
        verify(helpOrderEventAttachmentMapper, times(2)).insert(attachmentCaptor.capture());
        assertThat(attachmentCaptor.getAllValues()).extracting(HelpOrderEventAttachment::getFileAssetId)
                .containsExactly(10L, 11L);
    }

    @Test
    void shouldCreateHelpOrderWithSingleVoiceAttachment() {
        AppPatientService.AccessContext context = accessContext();
        AtomicReference<HelpOrder> insertedOrder = new AtomicReference<>();
        HelpOrderVoiceAttachmentRequest voiceRequest = new HelpOrderVoiceAttachmentRequest();
        voiceRequest.setPlaybackFileAssetId(20L);
        voiceRequest.setTranscriptSourceFileAssetId(21L);
        voiceRequest.setDurationSeconds(6);
        HelpOrderVoiceTranscriptionService.VoiceFiles voiceFiles =
                new HelpOrderVoiceTranscriptionService.VoiceFiles(helpOrderAudio(20L, "HELP_ORDER_AUDIO"), helpOrderAudio(21L, "HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE"), 6);

        when(appPatientService.requireAccessContext("13800138000")).thenReturn(context);
        when(voiceTranscriptionService.validateVoiceFiles(context, voiceRequest)).thenReturn(voiceFiles);
        when(helpOrderMapper.insert(any(HelpOrder.class))).thenAnswer(invocation -> {
            HelpOrder order = invocation.getArgument(0);
            order.setId(920L);
            insertedOrder.set(order);
            return 1;
        });
        when(helpOrderEventMapper.insert(any(HelpOrderEvent.class))).thenAnswer(invocation -> {
            HelpOrderEvent event = invocation.getArgument(0);
            event.setId(720L);
            return 1;
        });
        when(helpOrderEventAttachmentMapper.insert(any(HelpOrderEventAttachment.class))).thenAnswer(invocation -> {
            HelpOrderEventAttachment attachment = invocation.getArgument(0);
            attachment.setId(820L);
            return 1;
        });
        when(helpOrderMapper.selectById(1L, 920L)).thenAnswer(invocation -> insertedOrder.get());
        when(helpOrderEventMapper.selectByHelpOrderId(1L, 920L)).thenReturn(List.of());

        HelpOrderDTO request = new HelpOrderDTO();
        request.setCategory("MEDICATION_CONSULTATION");
        request.setTitle("语音求助");
        request.setVoiceAttachment(voiceRequest);

        HelpOrderDTO result = appHelpOrderService.create("13800138000", request);

        assertThat(result.getId()).isEqualTo(920L);
        ArgumentCaptor<HelpOrderEventAttachment> attachmentCaptor = ArgumentCaptor.forClass(HelpOrderEventAttachment.class);
        verify(helpOrderEventAttachmentMapper).insert(attachmentCaptor.capture());
        assertThat(attachmentCaptor.getValue().getAttachmentType()).isEqualTo("AUDIO");
        assertThat(attachmentCaptor.getValue().getFileAssetId()).isEqualTo(20L);
        verify(voiceTranscriptionService).createPendingTranscript(eq(1L), eq(920L), eq(820L), eq(voiceFiles), any(LocalDateTime.class));
    }

    @Test
    void shouldRejectAttachmentOwnedByAnotherAccount() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(fileAssetMapper.selectByIdAndTenant(1L, 10L)).thenReturn(helpOrderImage(10L, 999L));

        HelpOrderDTO request = new HelpOrderDTO();
        request.setCategory("ADVERSE_REACTION");
        request.setTitle("服药后皮下淤血");
        request.setAttachmentFileAssetIds(List.of(10L));

        BusinessException exception = catchThrowableOfType(
                () -> appHelpOrderService.create("13800138000", request),
                BusinessException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode());
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

    @Test
    void shouldExposeDoctorDecisionResultInPatientTimeline() {
        HelpOrder order = ownOrder();
        order.setStatus("CLOSED");
        order.setServiceNotice("doctor final decision");

        HelpOrderEvent event = new HelpOrderEvent();
        event.setId(801L);
        event.setHelpOrderId(order.getId());
        event.setEventType("DOCTOR_DECISION");
        event.setOperatorType("DOCTOR");
        event.setOperatorId(11L);
        event.setContent("doctor final decision");
        event.setMetadataJson("{\"decisionResult\":\"ADOPTED\"}");

        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(helpOrderMapper.selectById(1L, order.getId())).thenReturn(order);
        when(helpOrderEventMapper.selectByHelpOrderId(1L, order.getId())).thenReturn(List.of(event));

        HelpOrderDTO result = appHelpOrderService.getDetail("13800138000", order.getId());

        assertThat(result.getEvents()).hasSize(1);
        assertThat(result.getEvents().get(0).getEventType()).isEqualTo("DOCTOR_DECISION");
        assertThat(result.getEvents().get(0).getDecisionResult()).isEqualTo("ADOPTED");
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

    private FileAsset helpOrderImage(Long id, Long uploadedById) {
        FileAsset fileAsset = new FileAsset();
        fileAsset.setId(id);
        fileAsset.setBusinessType("HELP_ORDER_IMAGE");
        fileAsset.setUploadedByType("PATIENT");
        fileAsset.setUploadedById(uploadedById);
        fileAsset.setObjectKey("help-orders/images/test-" + id + ".png");
        fileAsset.setOriginalFilename("test-" + id + ".png");
        fileAsset.setMimeType("image/png");
        fileAsset.setSizeBytes(1024L);
        return fileAsset;
    }

    private FileAsset helpOrderAudio(Long id, String businessType) {
        FileAsset fileAsset = new FileAsset();
        fileAsset.setId(id);
        fileAsset.setBusinessType(businessType);
        fileAsset.setUploadedByType("PATIENT");
        fileAsset.setUploadedById(101L);
        fileAsset.setObjectKey("help-orders/audio/test-" + id + ".webm");
        fileAsset.setOriginalFilename("test-" + id + ".webm");
        fileAsset.setMimeType("audio/webm");
        fileAsset.setSizeBytes(1024L);
        return fileAsset;
    }
}
