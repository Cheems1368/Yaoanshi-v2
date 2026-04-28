package com.medicine.service.auth;

import com.medicine.common.exception.BusinessException;
import com.medicine.domain.identity.DoctorProfile;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.domain.identity.SystemUser;
import com.medicine.repository.identity.DoctorProfileMapper;
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

    @Mock
    private DoctorProfileMapper doctorProfileMapper;

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

    @Test
    void shouldUseDoctorEffectiveRoleForDualRoleClaims() {
        JwtClaims claims = professionalClaims();
        claims.setRoles(java.util.List.of("admin"));
        claims.setEffectiveRole("doctor");
        SystemUser systemUser = new SystemUser();
        systemUser.setId(100L);
        systemUser.setTenantId(1L);
        systemUser.setUsername("doctor");
        systemUser.setNickname("Doctor A");

        when(doctorProfileMapper.selectBySystemUserId(1L, 100L)).thenReturn(null);
        when(professionalIdentityRepository.selectActiveSystemUser(1L, 100L)).thenReturn(systemUser);
        doAnswer(invocation -> {
            DoctorProfile profile = invocation.getArgument(0);
            profile.setId(9101L);
            return 1;
        }).when(doctorProfileMapper).insert(any(DoctorProfile.class));

        DoctorProfile profile = professionalIdentityService.getOrCreateDoctorProfile(claims);

        assertThat(profile.getId()).isEqualTo(9101L);
        assertThat(professionalIdentityService.isDoctor(claims)).isTrue();
        assertThat(professionalIdentityService.isPharmacist(claims)).isFalse();
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
