package com.medicine.service.speech;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class MockSpeechTranscriptionService implements SpeechTranscriptionService {

    @Override
    public SpeechResult transcribePcm16k(byte[] audioBytes) {
        return SpeechResult.failure("SPEECH_TEST_PROFILE", "XFYUN");
    }
}
