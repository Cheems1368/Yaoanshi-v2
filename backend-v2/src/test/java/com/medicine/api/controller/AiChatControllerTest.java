package com.medicine.api.controller;

import com.medicine.api.dto.ai.AiConsentGrantRequest;
import com.medicine.api.dto.ai.AiDisclosureDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.service.ai.AiChatService;
import com.medicine.service.ai.AiConsentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatControllerTest {

    @Mock
    private AiChatService chatService;
    @Mock
    private AiConsentService consentService;

    private AiChatController controller;
    private TestingAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        controller = new AiChatController(chatService, consentService);
        authentication = new TestingAuthenticationToken("13800000000", "token");
    }

    @Test
    void shouldReturnExplicitDisclosureDto() {
        AiDisclosureDTO disclosure = new AiDisclosureDTO();
        disclosure.setPolicyVersion("sec-005-v2");
        disclosure.setConsentGranted(false);
        when(consentService.getDisclosure("13800000000")).thenReturn(disclosure);

        CommonResult<AiDisclosureDTO> result = controller.getDisclosure(authentication);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getPolicyVersion()).isEqualTo("sec-005-v2");
        assertThat(result.getData().isConsentGranted()).isFalse();
    }

    @Test
    void shouldPassPolicyVersionWhenGrantingConsent() {
        AiConsentGrantRequest request = new AiConsentGrantRequest();
        request.setPolicyVersion("sec-005-v2");

        CommonResult<Boolean> result = controller.grantConsent(request, authentication);

        assertThat(result.getData()).isTrue();
        verify(consentService).grantConsent("13800000000", request);
    }
}
