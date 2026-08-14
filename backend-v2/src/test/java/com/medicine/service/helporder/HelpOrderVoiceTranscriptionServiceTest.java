package com.medicine.service.helporder;

import com.medicine.api.dto.helporder.HelpOrderVoiceTranscriptDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.file.FileAsset;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.helporder.HelpOrderEventAttachment;
import com.medicine.domain.helporder.HelpOrderVoiceTranscript;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.repository.file.FileAssetMapper;
import com.medicine.repository.helporder.HelpOrderEventAttachmentMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.helporder.HelpOrderVoiceTranscriptMapper;
import com.medicine.service.auth.AppPatientService;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import com.medicine.service.speech.SpeechTranscriptionService;
import com.medicine.service.storage.S3CompatibleObjectStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelpOrderVoiceTranscriptionServiceTest {

    @Mock
    private HelpOrderMapper helpOrderMapper;

    @Mock
    private HelpOrderEventAttachmentMapper helpOrderEventAttachmentMapper;

    @Mock
    private HelpOrderVoiceTranscriptMapper voiceTranscriptMapper;

    @Mock
    private FileAssetMapper fileAssetMapper;

    @Mock
    private S3CompatibleObjectStorage objectStorage;

    @Mock
    private SpeechTranscriptionService speechTranscriptionService;

    @Mock
    private AppPatientService appPatientService;

    @Mock
    private ProfessionalIdentityService professionalIdentityService;

    @InjectMocks
    private HelpOrderVoiceTranscriptionService service;

    @Test
    void shouldReturnExistingSuccessWithoutCallingProvider() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(helpOrderMapper.selectById(1L, 900L)).thenReturn(order());
        when(helpOrderEventAttachmentMapper.selectById(1L, 700L)).thenReturn(audioAttachment());
        HelpOrderVoiceTranscript transcript = transcript("SUCCESS");
        transcript.setTranscriptText("已经转写完成");
        when(voiceTranscriptMapper.selectByAttachmentId(1L, 700L)).thenReturn(transcript);

        HelpOrderVoiceTranscriptDTO result = service.transcribeForApp("13800138000", 900L, 700L);

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getText()).isEqualTo("已经转写完成");
        verify(speechTranscriptionService, never()).transcribePcm16k(org.mockito.ArgumentMatchers.any());
        verify(voiceTranscriptMapper, never()).updateResult(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldMarkTranscriptUnconfiguredWhenProviderKeysMissing() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(helpOrderMapper.selectById(1L, 900L)).thenReturn(order());
        when(helpOrderEventAttachmentMapper.selectById(1L, 700L)).thenReturn(audioAttachment());
        when(voiceTranscriptMapper.selectByAttachmentId(1L, 700L)).thenReturn(transcript("PENDING"));
        when(fileAssetMapper.selectByIdAndTenant(1L, 55L)).thenReturn(sourceFile());
        when(objectStorage.readObjectBytes("help-orders/audio-transcript-source/source.wav")).thenReturn(wavBytes(new byte[] { 1, 2, 3 }));
        when(speechTranscriptionService.transcribePcm16k(org.mockito.ArgumentMatchers.any(byte[].class)))
                .thenReturn(SpeechTranscriptionService.SpeechResult.failure("SPEECH_API_KEY_MISSING", "XFYUN"));

        HelpOrderVoiceTranscriptDTO result = service.transcribeForApp("13800138000", 900L, 700L);

        assertThat(result.getStatus()).isEqualTo("UNCONFIGURED");
        assertThat(result.getErrorCode()).isEqualTo("SPEECH_API_KEY_MISSING");
        ArgumentCaptor<byte[]> audioCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(speechTranscriptionService).transcribePcm16k(audioCaptor.capture());
        assertThat(audioCaptor.getValue()).containsExactly(1, 2, 3);
        ArgumentCaptor<HelpOrderVoiceTranscript> captor = ArgumentCaptor.forClass(HelpOrderVoiceTranscript.class);
        verify(voiceTranscriptMapper).updateResult(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("UNCONFIGURED");
    }

    @Test
    void shouldRejectDoctorFromTriggeringAdminTranscription() {
        JwtClaims claims = new JwtClaims();
        claims.setTenantId(1L);
        claims.setRoles(List.of("doctor"));
        when(professionalIdentityService.isPharmacist(claims)).thenReturn(false);

        BusinessException exception = catchThrowableOfType(
                () -> service.transcribeForAdmin(claims, 900L, 700L),
                BusinessException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.HELP_ORDER_PERMISSION_DENIED.getCode());
    }

    @Test
    void shouldAllowVisiblePharmacistToTranscribe() {
        JwtClaims claims = new JwtClaims();
        claims.setTenantId(1L);
        PharmacistProfile pharmacist = new PharmacistProfile();
        pharmacist.setId(301L);
        HelpOrder order = order();
        order.setResponsiblePharmacistId(301L);

        when(professionalIdentityService.isPharmacist(claims)).thenReturn(true);
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(pharmacist);
        when(helpOrderMapper.selectById(1L, 900L)).thenReturn(order);
        when(helpOrderEventAttachmentMapper.selectById(1L, 700L)).thenReturn(audioAttachment());
        when(voiceTranscriptMapper.selectByAttachmentId(1L, 700L)).thenReturn(transcript("PENDING"));
        when(fileAssetMapper.selectByIdAndTenant(1L, 55L)).thenReturn(sourceFile());
        when(objectStorage.readObjectBytes("help-orders/audio-transcript-source/source.wav")).thenReturn(new byte[] { 4, 5 });
        when(speechTranscriptionService.transcribePcm16k(org.mockito.ArgumentMatchers.any(byte[].class)))
                .thenReturn(SpeechTranscriptionService.SpeechResult.success("患者说头晕", "XFYUN"));

        HelpOrderVoiceTranscriptDTO result = service.transcribeForAdmin(claims, 900L, 700L);

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getText()).isEqualTo("患者说头晕");
    }

    private AppPatientService.AccessContext accessContext() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        account.setAccountType("PATIENT");
        Patient patient = new Patient();
        patient.setId(201L);
        return new AppPatientService.AccessContext(account, patient, "PATIENT");
    }

    private HelpOrder order() {
        HelpOrder order = new HelpOrder();
        order.setId(900L);
        order.setPatientId(201L);
        order.setStatus("PHARMACIST_IN_PROGRESS");
        order.setResponsiblePharmacistId(301L);
        return order;
    }

    private HelpOrderEventAttachment audioAttachment() {
        HelpOrderEventAttachment attachment = new HelpOrderEventAttachment();
        attachment.setId(700L);
        attachment.setAttachmentType("AUDIO");
        return attachment;
    }

    private HelpOrderVoiceTranscript transcript(String status) {
        HelpOrderVoiceTranscript transcript = new HelpOrderVoiceTranscript();
        transcript.setId(800L);
        transcript.setHelpOrderId(900L);
        transcript.setHelpOrderEventAttachmentId(700L);
        transcript.setTranscriptSourceFileAssetId(55L);
        transcript.setDurationSeconds(6);
        transcript.setProvider("XFYUN");
        transcript.setStatus(status);
        transcript.setTenantId(1L);
        return transcript;
    }

    private FileAsset sourceFile() {
        FileAsset fileAsset = new FileAsset();
        fileAsset.setId(55L);
        fileAsset.setObjectKey("help-orders/audio-transcript-source/source.wav");
        fileAsset.setBusinessType("HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE");
        return fileAsset;
    }

    private byte[] wavBytes(byte[] pcm) {
        byte[] bytes = new byte[44 + pcm.length];
        bytes[0] = 'R';
        bytes[1] = 'I';
        bytes[2] = 'F';
        bytes[3] = 'F';
        bytes[8] = 'W';
        bytes[9] = 'A';
        bytes[10] = 'V';
        bytes[11] = 'E';
        bytes[12] = 'f';
        bytes[13] = 'm';
        bytes[14] = 't';
        bytes[15] = ' ';
        bytes[16] = 16;
        bytes[36] = 'd';
        bytes[37] = 'a';
        bytes[38] = 't';
        bytes[39] = 'a';
        bytes[40] = (byte) pcm.length;
        System.arraycopy(pcm, 0, bytes, 44, pcm.length);
        return bytes;
    }
}
