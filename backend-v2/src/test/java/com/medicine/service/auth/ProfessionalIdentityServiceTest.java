package com.medicine.service.auth;

import com.medicine.common.exception.BusinessException;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.identity.SystemUser;
import com.medicine.repository.identity.PharmacistProfileMapper;
import com.medicine.repository.identity.ProfessionalIdentityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalIdentityServiceTest {

    @Mock
    private ProfessionalIdentityRepository professionalIdentityRepository;

    @Mock
    private PharmacistProfileMapper pharmacistProfileMapper;

    @InjectMocks
    private ProfessionalIdentityService professionalIdentityService;

    @Test
    void shouldCreatePharmacistProfileWhenMissing() {
        JwtClaims claims = professionalClaims();
        SystemUser systemUser = new SystemUser();
        systemUser.setId(100L);
        systemUser.setTenantId(1L);
        systemUser.setUsername("medicine");
        systemUser.setNickname("药师A");
        systemUser.setMobile("15601691300");

        when(pharmacistProfileMapper.selectBySystemUserId(1L, 100L)).thenReturn(null);
        when(professionalIdentityRepository.selectActiveSystemUser(1L, 100L)).thenReturn(systemUser);
        doAnswer(invocation -> {
            PharmacistProfile profile = invocation.getArgument(0);
            profile.setId(9001L);
            return 1;
        }).when(pharmacistProfileMapper).insert(any(PharmacistProfile.class));

        PharmacistProfile profile = professionalIdentityService.getOrCreatePharmacistProfile(claims);

        assertThat(profile.getId()).isEqualTo(9001L);
        assertThat(profile.getRealName()).isEqualTo("药师A");
    }

    @Test
    void shouldRejectNonPharmacistRole() {
        JwtClaims claims = professionalClaims();
        claims.setRoles(java.util.List.of("doctor"));

        assertThatThrownBy(() -> professionalIdentityService.getOrCreatePharmacistProfile(claims))
                .isInstanceOf(BusinessException.class)
                .hasMessage("PATIENT_MANAGEMENT_PERMISSION_DENIED");
    }

    private JwtClaims professionalClaims() {
        JwtClaims claims = new JwtClaims();
        claims.setAccountType("PROFESSIONAL");
        claims.setTenantId(1L);
        claims.setUserId(100L);
        claims.setRoles(java.util.List.of("pharmacists"));
        return claims;
    }
}
