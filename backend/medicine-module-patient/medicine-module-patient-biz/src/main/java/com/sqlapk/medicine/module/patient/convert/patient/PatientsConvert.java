package com.sqlapk.medicine.module.patient.convert.patient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.api.dto.PatientsRespDTO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsSimpleRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 患者Convert
 *
 * @author 管理员 */
@Component
public class PatientsConvert {

    public PatientsDO convert(PatientsCreateReqVO bean) {
        if (bean == null) {
            return null;
        }
        return PatientsDO.builder()
                .userId(bean.getUserId())
                .address(bean.getAddress())
                .insuranceCardNumber(bean.getInsuranceCardNumber())
                .allergyHistory(bean.getAllergyHistory())
                .medicalHistory(bean.getMedicalHistory())
                .familyHistory(bean.getFamilyHistory())
                .personalHistory(bean.getPersonalHistory())
                .patientType(bean.getPatientType())
                .insurance(bean.getInsurance())
                .birthday(bean.getBirthday())
                .build();
    }

    public PatientsDO convert(PatientsUpdateReqVO bean) {
        if (bean == null) {
            return null;
        }
        return PatientsDO.builder()
                .id(bean.getId())
                .userId(bean.getUserId())
                .address(bean.getAddress())
                .insuranceCardNumber(bean.getInsuranceCardNumber())
                .allergyHistory(bean.getAllergyHistory())
                .medicalHistory(bean.getMedicalHistory())
                .familyHistory(bean.getFamilyHistory())
                .personalHistory(bean.getPersonalHistory())
                .patientType(bean.getPatientType())
                .insurance(bean.getInsurance())
                .birthday(bean.getBirthday())
                .build();
    }

    public PatientsRespVO convert(PatientsDO bean) {
        if (bean == null) {
            return null;
        }
        PatientsRespVO respVO = new PatientsRespVO();
        respVO.setId(bean.getId());
        respVO.setUserId(bean.getUserId());
        respVO.setAddress(bean.getAddress());
        respVO.setInsuranceCardNumber(bean.getInsuranceCardNumber());
        respVO.setAllergyHistory(bean.getAllergyHistory());
        respVO.setMedicalHistory(bean.getMedicalHistory());
        respVO.setFamilyHistory(bean.getFamilyHistory());
        respVO.setPersonalHistory(bean.getPersonalHistory());
        respVO.setPatientType(bean.getPatientType());
        respVO.setInsurance(bean.getInsurance());
        respVO.setBirthday(bean.getBirthday());
        respVO.setCreateTime(bean.getCreateTime());
        return respVO;
    }

    public List<PatientsRespVO> convertList(List<PatientsDO> list) {
        if (list == null) {
            return null;
        }
        return list.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public PageResult<PatientsRespVO> convertPage(PageResult<PatientsDO> page) {
        if (page == null) {
            return null;
        }
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

    public List<PatientsExcelVO> convertList02(List<PatientsDO> list) {
        if (list == null) {
            return null;
        }
        return list.stream()
                .map(this::convertToExcel)
                .collect(Collectors.toList());
    }

    public List<PatientsSimpleRespVO> convertSimple(List<PatientsDO> list) {
        if (list == null) {
            return null;
        }
        return list.stream()
                .map(this::convertToSimple)
                .collect(Collectors.toList());
    }

    public PatientsRespDTO convertToApi(PatientsDO bean) {
        if (bean == null) {
            return null;
        }
        PatientsRespDTO dto = new PatientsRespDTO();
        dto.setId(bean.getId());
        dto.setUserId(bean.getUserId());
        dto.setAddress(bean.getAddress());
        dto.setInsuranceCardNumber(bean.getInsuranceCardNumber());
        dto.setAllergyHistory(bean.getAllergyHistory());
        dto.setMedicalHistory(bean.getMedicalHistory());
        dto.setFamilyHistory(bean.getFamilyHistory());
        dto.setPersonalHistory(bean.getPersonalHistory());
        dto.setPatientType(bean.getPatientType());
        dto.setInsurance(bean.getInsurance());
        dto.setBirthday(bean.getBirthday());
        dto.setName(bean.getName());
        dto.setGender(bean.getGender());
        dto.setMobile(bean.getMobile());
        dto.setIdCard(bean.getIdCard());
        dto.setEmergencyContact(bean.getEmergencyContact());
        dto.setEmergencyPhone(bean.getEmergencyPhone());
        dto.setAuditStatus(bean.getAuditStatus());
        dto.setCompletenessScore(bean.getCompletenessScore());
        dto.setCompletenessLevel(bean.getCompletenessLevel());
        return dto;
    }

    private PatientsExcelVO convertToExcel(PatientsDO bean) {
        if (bean == null) {
            return null;
        }
        PatientsExcelVO excelVO = new PatientsExcelVO();
        excelVO.setId(bean.getId());
        excelVO.setUserId(bean.getUserId());
        excelVO.setAddress(bean.getAddress());
        excelVO.setInsuranceCardNumber(bean.getInsuranceCardNumber());
        excelVO.setAllergyHistory(bean.getAllergyHistory());
        excelVO.setMedicalHistory(bean.getMedicalHistory());
        excelVO.setFamilyHistory(bean.getFamilyHistory());
        excelVO.setPersonalHistory(bean.getPersonalHistory());
        excelVO.setPatientType(bean.getPatientType());
        excelVO.setInsurance(bean.getInsurance());
        excelVO.setBirthday(bean.getBirthday());
        excelVO.setCreateTime(bean.getCreateTime());
        return excelVO;
    }

    private PatientsSimpleRespVO convertToSimple(PatientsDO bean) {
        if (bean == null) {
            return null;
        }
        PatientsSimpleRespVO simpleVO = new PatientsSimpleRespVO();
        simpleVO.setId(bean.getId());
        simpleVO.setUserId(bean.getUserId());
        simpleVO.setInsurance(bean.getInsurance());
        simpleVO.setAddress(bean.getAddress());
        simpleVO.setInsuranceCardNumber(bean.getInsuranceCardNumber());
        simpleVO.setBirthday(bean.getBirthday());
        return simpleVO;
    }

}
