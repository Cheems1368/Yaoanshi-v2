package com.sqlapk.medicine.module.patient.service.patient;

import com.sqlapk.medicine.module.patient.controller.app.patient.vo.AppPatientProfileRespVO;
import com.sqlapk.medicine.module.patient.controller.app.patient.vo.AppPatientProfileSaveReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.patient.PatientsMapper;
import com.sqlapk.medicine.module.patient.service.patient.model.PatientCompletenessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * App 端患者档案服务
 */
@Slf4j
@Service
@Validated
public class AppPatientProfileService {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_DATE_TIME;

    @Resource
    private PatientsMapper patientsMapper;

    @Resource
    private PatientProfileCompletenessChecker patientProfileCompletenessChecker;

    /**
     * 保存或更新患者档案
     */
    public Long saveProfile(Long userId, AppPatientProfileSaveReqVO reqVO) {
        PatientsDO entity = patientsMapper.selectByUserId(userId);
        if (entity == null) {
            entity = new PatientsDO();
            entity.setUserId(userId);
            entity.setAuditStatus(0);
        } else {
            entity.setUserId(userId);
        }

        entity.setName(reqVO.getRealName());
        entity.setIdCard(reqVO.getIdNumber());
        entity.setIdType(reqVO.getIdType());
        entity.setEthnicity(reqVO.getEthnicity());
        entity.setMobile(reqVO.getMobile());
        entity.setAddress(reqVO.getAddress());
        entity.setPatientType(reqVO.getPatientType());
        entity.setInsurance(reqVO.getInsurance());
        entity.setBirthday(parseBirthday(reqVO.getBirthday()));
        if (reqVO.getAllergyHistory() != null) {
            entity.setAllergyHistory(trimToNull(reqVO.getAllergyHistory()));
        }
        if (reqVO.getMedicalHistory() != null) {
            entity.setMedicalHistory(trimToNull(reqVO.getMedicalHistory()));
        }
        if (reqVO.getFamilyHistory() != null) {
            entity.setFamilyHistory(trimToNull(reqVO.getFamilyHistory()));
        }
        if (reqVO.getPersonalHistory() != null) {
            entity.setPersonalHistory(trimToNull(reqVO.getPersonalHistory()));
        }
        if (reqVO.getInsuranceCardNumber() != null) {
            entity.setInsuranceCardNumber(trimToNull(reqVO.getInsuranceCardNumber()));
        }
        if (reqVO.getEmergencyContact() != null) {
            entity.setEmergencyContact(trimToNull(reqVO.getEmergencyContact()));
        }
        if (reqVO.getEmergencyContactPhone() != null) {
            entity.setEmergencyPhone(trimToNull(reqVO.getEmergencyContactPhone()));
        }
        if (reqVO.getEmergencyContactRelationship() != null) {
            entity.setEmergencyContactRelationship(trimToNull(reqVO.getEmergencyContactRelationship()));
        }
        if (reqVO.getAllergies() != null) {
            entity.setAllergies(trimToNull(reqVO.getAllergies()));
        }
        if (reqVO.getBloodType() != null) {
            entity.setBloodType(trimToNull(reqVO.getBloodType()));
        }
        if (reqVO.getHeight() != null) {
            entity.setHeight(reqVO.getHeight());
        }
        if (reqVO.getWeight() != null) {
            entity.setWeight(reqVO.getWeight());
        }
        if (reqVO.getAvatar() != null) {
            entity.setAvatar(trimToNull(reqVO.getAvatar()));
        }
        if (reqVO.getAge() != null) {
            entity.setAge(reqVO.getAge());
        }
        if (reqVO.getGender() != null) {
            entity.setGender(reqVO.getGender());
        }
        entity.setIsRealNameVerified(Boolean.TRUE);
        entity.setRealNameVerifiedTime(LocalDateTime.now());

        // 计算档案完整性
        PatientCompletenessResult completenessResult = patientProfileCompletenessChecker.checkCompleteness(entity);
        entity.setCompletenessScore(completenessResult.getCompletenessScore());
        entity.setCompletenessLevel(completenessResult.getCompletenessLevel() != null
                ? completenessResult.getCompletenessLevel().name()
                : null);

        if (entity.getId() == null) {
            patientsMapper.insert(entity);
        } else {
            patientsMapper.updateById(entity);
        }

        return entity.getId();
    }

    /**
     * 获取患者档案
     */
    public AppPatientProfileRespVO getProfile(Long userId, Long patientId) {
        PatientsDO patient = null;
        if (patientId != null) {
            patient = patientsMapper.selectById(patientId);
            if (patient != null && !Objects.equals(patient.getUserId(), userId)) {
                log.warn("拒绝访问其他用户的患者档案, currentUser={}, patientId={}, owner={}", userId, patientId, patient.getUserId());
                patient = null;
            }
        }
        if (patient == null) {
            patient = patientsMapper.selectByUserId(userId);
        }
        if (patient == null) {
            return null;
        }
        return convertToResp(patient);
    }

    private LocalDateTime parseBirthday(String birthday) {
        if (!StringUtils.hasText(birthday)) {
            return null;
        }
        String trimmed = birthday.trim();
        try {
            if (trimmed.length() == 10) {
                LocalDate date = LocalDate.parse(trimmed, ISO_DATE);
                return date.atStartOfDay();
            }
            return LocalDateTime.parse(trimmed, ISO_DATE_TIME);
        } catch (DateTimeParseException ex) {
            log.warn("生日格式无法解析: {}", trimmed, ex);
            return null;
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private AppPatientProfileRespVO convertToResp(PatientsDO patient) {
        AppPatientProfileRespVO respVO = new AppPatientProfileRespVO();
        respVO.setPatientId(patient.getId());
        respVO.setUserId(patient.getUserId());
        respVO.setRealName(patient.getName());
        respVO.setMobile(patient.getMobile());
        respVO.setPhone(patient.getMobile());
        respVO.setIdNumber(patient.getIdCard());
        respVO.setIdType(patient.getIdType());
        respVO.setEthnicity(patient.getEthnicity());
        respVO.setBirthday(patient.getBirthday() != null ? patient.getBirthday().toLocalDate().toString() : null);
        respVO.setPatientType(patient.getPatientType());
        respVO.setInsurance(patient.getInsurance());
        respVO.setAddress(patient.getAddress());
        respVO.setRealNameVerified(Boolean.TRUE.equals(patient.getIsRealNameVerified()));
        respVO.setRealNameVerifiedTime(patient.getRealNameVerifiedTime());
        respVO.setAllergyHistory(patient.getAllergyHistory());
        respVO.setMedicalHistory(patient.getMedicalHistory());
        respVO.setFamilyHistory(patient.getFamilyHistory());
        respVO.setPersonalHistory(patient.getPersonalHistory());
        respVO.setInsuranceCardNumber(patient.getInsuranceCardNumber());
        respVO.setEmergencyContact(patient.getEmergencyContact());
        respVO.setEmergencyContactPhone(patient.getEmergencyPhone());
        respVO.setEmergencyContactRelationship(patient.getEmergencyContactRelationship());
        respVO.setAge(patient.getAge());
        respVO.setGender(convertGender(patient.getGender()));
        respVO.setHeight(patient.getHeight());
        respVO.setWeight(patient.getWeight());
        respVO.setBloodType(patient.getBloodType());
        respVO.setAllergies(patient.getAllergies());
        respVO.setAvatar(patient.getAvatar());
        return respVO;
    }
    private String convertGender(Integer gender) {
        if (gender == null) {
            return null;
        }
        if (gender == 0) {
            return "男";
        }
        if (gender == 1) {
            return "女";
        }
        return "未知";
    }

}
