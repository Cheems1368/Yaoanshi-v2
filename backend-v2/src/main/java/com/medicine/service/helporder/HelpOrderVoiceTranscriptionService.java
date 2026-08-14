package com.medicine.service.helporder;

import com.medicine.api.dto.helporder.HelpOrderAttachmentDTO;
import com.medicine.api.dto.helporder.HelpOrderVoiceAttachmentRequest;
import com.medicine.api.dto.helporder.HelpOrderVoiceTranscriptDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.file.FileAsset;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.helporder.HelpOrderEventAttachment;
import com.medicine.domain.helporder.HelpOrderVoiceTranscript;
import com.medicine.repository.file.FileAssetMapper;
import com.medicine.repository.helporder.HelpOrderEventAttachmentMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.helporder.HelpOrderVoiceTranscriptMapper;
import com.medicine.service.auth.AppPatientService;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import com.medicine.service.speech.SpeechTranscriptionService;
import com.medicine.service.storage.S3CompatibleObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class HelpOrderVoiceTranscriptionService {

    private static final long DEFAULT_TENANT_ID = 1L;
    private static final int MAX_DURATION_SECONDS = 60;

    private final HelpOrderMapper helpOrderMapper;
    private final HelpOrderEventAttachmentMapper helpOrderEventAttachmentMapper;
    private final HelpOrderVoiceTranscriptMapper voiceTranscriptMapper;
    private final FileAssetMapper fileAssetMapper;
    private final S3CompatibleObjectStorage objectStorage;
    private final SpeechTranscriptionService speechTranscriptionService;
    private final AppPatientService appPatientService;
    private final ProfessionalIdentityService professionalIdentityService;

    public HelpOrderVoiceTranscriptionService(
            HelpOrderMapper helpOrderMapper,
            HelpOrderEventAttachmentMapper helpOrderEventAttachmentMapper,
            HelpOrderVoiceTranscriptMapper voiceTranscriptMapper,
            FileAssetMapper fileAssetMapper,
            S3CompatibleObjectStorage objectStorage,
            SpeechTranscriptionService speechTranscriptionService,
            AppPatientService appPatientService,
            ProfessionalIdentityService professionalIdentityService
    ) {
        this.helpOrderMapper = helpOrderMapper;
        this.helpOrderEventAttachmentMapper = helpOrderEventAttachmentMapper;
        this.voiceTranscriptMapper = voiceTranscriptMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.objectStorage = objectStorage;
        this.speechTranscriptionService = speechTranscriptionService;
        this.appPatientService = appPatientService;
        this.professionalIdentityService = professionalIdentityService;
    }

    public VoiceFiles validateVoiceFiles(AppPatientService.AccessContext context, HelpOrderVoiceAttachmentRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getPlaybackFileAssetId() == null || request.getTranscriptSourceFileAssetId() == null
                || request.getDurationSeconds() == null || request.getDurationSeconds() <= 0
                || request.getDurationSeconds() > MAX_DURATION_SECONDS) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID_TYPE.getCode(), ErrorCode.FILE_UPLOAD_INVALID_TYPE.getMessage());
        }
        FileAsset playback = requireOwnedFile(context, request.getPlaybackFileAssetId(), "HELP_ORDER_AUDIO");
        FileAsset source = requireOwnedFile(context, request.getTranscriptSourceFileAssetId(), "HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE");
        return new VoiceFiles(playback, source, request.getDurationSeconds());
    }

    public void createPendingTranscript(Long tenantId, Long helpOrderId, Long attachmentId, VoiceFiles voiceFiles, LocalDateTime now) {
        if (voiceFiles == null) {
            return;
        }
        HelpOrderVoiceTranscript transcript = new HelpOrderVoiceTranscript();
        transcript.setHelpOrderId(helpOrderId);
        transcript.setHelpOrderEventAttachmentId(attachmentId);
        transcript.setPlaybackFileAssetId(voiceFiles.playback().getId());
        transcript.setTranscriptSourceFileAssetId(voiceFiles.source().getId());
        transcript.setDurationSeconds(voiceFiles.durationSeconds());
        transcript.setProvider("XFYUN");
        transcript.setStatus("PENDING");
        applyAudit(transcript, tenantId, "help-order-voice", now);
        voiceTranscriptMapper.insert(transcript);
    }

    public void attachTranscript(Long tenantId, HelpOrderAttachmentDTO attachment) {
        if (attachment == null || attachment.getId() == null || !"AUDIO".equals(attachment.getAttachmentType())) {
            return;
        }
        HelpOrderVoiceTranscript transcript = voiceTranscriptMapper.selectByAttachmentId(tenantId, attachment.getId());
        if (transcript == null) {
            return;
        }
        attachment.setDurationSeconds(transcript.getDurationSeconds());
        attachment.setVoiceTranscript(toDto(transcript));
    }

    public HelpOrderVoiceTranscriptDTO transcribeForApp(String subject, Long orderId, Long attachmentId) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        HelpOrder order = requireOrder(DEFAULT_TENANT_ID, orderId);
        if (!context.patient().getId().equals(order.getPatientId())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode(), ErrorCode.HELP_ORDER_PERMISSION_DENIED.getMessage());
        }
        return transcribe(DEFAULT_TENANT_ID, orderId, attachmentId, context.operatorType(), context.appAccount().getId());
    }

    public HelpOrderVoiceTranscriptDTO transcribeForAdmin(JwtClaims claims, Long orderId, Long attachmentId) {
        if (!professionalIdentityService.isPharmacist(claims)) {
            throw new BusinessException(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode(), ErrorCode.HELP_ORDER_PERMISSION_DENIED.getMessage());
        }
        Long pharmacistId = professionalIdentityService.getOrCreatePharmacistProfile(claims).getId();
        HelpOrder order = requireOrder(claims.getTenantId(), orderId);
        if (!isPharmacistVisible(pharmacistId, order)) {
            throw new BusinessException(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode(), ErrorCode.HELP_ORDER_PERMISSION_DENIED.getMessage());
        }
        return transcribe(claims.getTenantId(), orderId, attachmentId, "PHARMACIST", pharmacistId);
    }

    private HelpOrderVoiceTranscriptDTO transcribe(Long tenantId, Long orderId, Long attachmentId, String operatorType, Long operatorId) {
        HelpOrderEventAttachment attachment = helpOrderEventAttachmentMapper.selectById(tenantId, attachmentId);
        if (attachment == null || !"AUDIO".equals(attachment.getAttachmentType())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_VOICE_NOT_FOUND.getCode(), ErrorCode.HELP_ORDER_VOICE_NOT_FOUND.getMessage());
        }
        HelpOrderVoiceTranscript transcript = voiceTranscriptMapper.selectByAttachmentId(tenantId, attachmentId);
        if (transcript == null || !orderId.equals(transcript.getHelpOrderId())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_VOICE_NOT_FOUND.getCode(), ErrorCode.HELP_ORDER_VOICE_NOT_FOUND.getMessage());
        }
        if ("SUCCESS".equals(transcript.getStatus())) {
            return toDto(transcript);
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        FileAsset source = fileAssetMapper.selectByIdAndTenant(tenantId, transcript.getTranscriptSourceFileAssetId());
        SpeechTranscriptionService.SpeechResult result;
        if (source == null) {
            result = SpeechTranscriptionService.SpeechResult.failure("SPEECH_SOURCE_FILE_MISSING", "XFYUN");
        } else {
            byte[] audioBytes = objectStorage.readObjectBytes(source.getObjectKey());
            result = speechTranscriptionService.transcribePcm16k(extractPcmPayload(audioBytes));
        }

        transcript.setProvider(StringUtils.hasText(result.getProvider()) ? result.getProvider() : "XFYUN");
        transcript.setStatus(resolveStatus(result));
        transcript.setTranscriptText(result.isSuccess() ? result.getText() : null);
        transcript.setErrorCode(result.isSuccess() ? null : result.getErrorCode());
        transcript.setTranscribedByType(operatorType);
        transcript.setTranscribedById(operatorId);
        transcript.setTranscribedAt(now);
        transcript.setUpdater("help-order-voice");
        transcript.setUpdateTime(now);
        voiceTranscriptMapper.updateResult(transcript);
        return toDto(transcript);
    }

    private String resolveStatus(SpeechTranscriptionService.SpeechResult result) {
        if (result.isSuccess()) {
            return "SUCCESS";
        }
        return "SPEECH_API_KEY_MISSING".equals(result.getErrorCode()) ? "UNCONFIGURED" : "FAILED";
    }

    private byte[] extractPcmPayload(byte[] audioBytes) {
        if (audioBytes == null || audioBytes.length < 44
                || audioBytes[0] != 'R' || audioBytes[1] != 'I' || audioBytes[2] != 'F' || audioBytes[3] != 'F'
                || audioBytes[8] != 'W' || audioBytes[9] != 'A' || audioBytes[10] != 'V' || audioBytes[11] != 'E') {
            return audioBytes;
        }
        int offset = 12;
        while (offset + 8 <= audioBytes.length) {
            String chunkId = new String(audioBytes, offset, 4, java.nio.charset.StandardCharsets.US_ASCII);
            int chunkSize = littleEndianInt(audioBytes, offset + 4);
            int dataOffset = offset + 8;
            if ("data".equals(chunkId) && dataOffset + chunkSize <= audioBytes.length) {
                return java.util.Arrays.copyOfRange(audioBytes, dataOffset, dataOffset + chunkSize);
            }
            offset = dataOffset + chunkSize + (chunkSize % 2);
        }
        return audioBytes;
    }

    private int littleEndianInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff)
                | ((bytes[offset + 1] & 0xff) << 8)
                | ((bytes[offset + 2] & 0xff) << 16)
                | ((bytes[offset + 3] & 0xff) << 24);
    }

    private FileAsset requireOwnedFile(AppPatientService.AccessContext context, Long fileAssetId, String businessType) {
        FileAsset fileAsset = fileAssetMapper.selectByIdAndTenant(DEFAULT_TENANT_ID, fileAssetId);
        if (fileAsset == null || !businessType.equals(fileAsset.getBusinessType())) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID_TYPE.getCode(), ErrorCode.FILE_UPLOAD_INVALID_TYPE.getMessage());
        }
        if (!context.operatorType().equals(fileAsset.getUploadedByType())
                || !context.appAccount().getId().equals(fileAsset.getUploadedById())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode(), ErrorCode.HELP_ORDER_PERMISSION_DENIED.getMessage());
        }
        return fileAsset;
    }

    private HelpOrder requireOrder(Long tenantId, Long orderId) {
        HelpOrder order = helpOrderMapper.selectById(tenantId, orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.HELP_ORDER_NOT_FOUND.getCode(), ErrorCode.HELP_ORDER_NOT_FOUND.getMessage());
        }
        return order;
    }

    private boolean isPharmacistVisible(Long pharmacistId, HelpOrder order) {
        if ("SUBMITTED".equals(order.getStatus())) {
            return true;
        }
        if ("PHARMACIST".equals(order.getCurrentHandlerType()) && pharmacistId.equals(order.getCurrentHandlerId())) {
            return true;
        }
        if (pharmacistId.equals(order.getResponsiblePharmacistId())) {
            return true;
        }
        return order.getResponsiblePharmacistId() == null && order.getCurrentHandlerId() == null;
    }

    private HelpOrderVoiceTranscriptDTO toDto(HelpOrderVoiceTranscript transcript) {
        HelpOrderVoiceTranscriptDTO dto = new HelpOrderVoiceTranscriptDTO();
        dto.setId(transcript.getId());
        dto.setProvider(transcript.getProvider());
        dto.setStatus(transcript.getStatus());
        dto.setText(transcript.getTranscriptText());
        dto.setErrorCode(transcript.getErrorCode());
        dto.setTranscribedAt(transcript.getTranscribedAt() == null ? null : transcript.getTranscribedAt().toInstant(ZoneOffset.UTC).toString());
        return dto;
    }

    private void applyAudit(com.medicine.domain.common.BaseEntity entity, Long tenantId, String operator, LocalDateTime now) {
        entity.setCreator(operator);
        entity.setCreateTime(now);
        entity.setUpdater(operator);
        entity.setUpdateTime(now);
        entity.setDeleted(false);
        entity.setTenantId(tenantId);
    }

    public record VoiceFiles(FileAsset playback, FileAsset source, Integer durationSeconds) {
    }
}
