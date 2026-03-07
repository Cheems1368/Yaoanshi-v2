package com.sqlapk.medicine.module.patient.convert.pharmacyoutpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient.PharmacyOutpatientDO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 药学门诊 Convert
 *
 * @author 管理员 */
@Component
public class PharmacyOutpatientConvert {

    public PharmacyOutpatientDO convert(PharmacyOutpatientCreateReqVO bean) {
        if (bean == null) {
            return null;
        }
        return PharmacyOutpatientDO.builder()
                .prescriptionId(bean.getPrescriptionId())
                .patientId(bean.getPatientId())
                .medicineName(bean.getMedicineName())
                .medicineUsage(bean.getMedicineUsage())
                .startUsageTime(bean.getStartUsageTime())
                .stopUsageTime(bean.getStopUsageTime())
                .stopReason(bean.getStopReason())
                .mrpType(bean.getMrpType())
                .weight(bean.getWeight())
                .mrpDesc(bean.getMrpDesc())
                .build();
    }

    public PharmacyOutpatientDO convert(PharmacyOutpatientUpdateReqVO bean) {
        if (bean == null) {
            return null;
        }
        return PharmacyOutpatientDO.builder()
                .id(bean.getId())
                .prescriptionId(bean.getPrescriptionId())
                .patientId(bean.getPatientId())
                .medicineName(bean.getMedicineName())
                .medicineUsage(bean.getMedicineUsage())
                .startUsageTime(bean.getStartUsageTime())
                .stopUsageTime(bean.getStopUsageTime())
                .stopReason(bean.getStopReason())
                .mrpType(bean.getMrpType())
                .weight(bean.getWeight())
                .mrpDesc(bean.getMrpDesc())
                .build();
    }

    public PharmacyOutpatientRespVO convert(PharmacyOutpatientDO bean) {
        if (bean == null) {
            return null;
        }
        PharmacyOutpatientRespVO respVO = new PharmacyOutpatientRespVO();
        respVO.setId(bean.getId());
        respVO.setPrescriptionId(bean.getPrescriptionId());
        respVO.setPatientId(bean.getPatientId());
        respVO.setMedicineName(bean.getMedicineName());
        respVO.setMedicineUsage(bean.getMedicineUsage());
        respVO.setStartUsageTime(bean.getStartUsageTime());
        respVO.setStopUsageTime(bean.getStopUsageTime());
        respVO.setStopReason(bean.getStopReason());
        respVO.setMrpType(bean.getMrpType());
        respVO.setWeight(bean.getWeight());
        respVO.setMrpDesc(bean.getMrpDesc());
        respVO.setCreateTime(bean.getCreateTime());
        return respVO;
    }

    public List<PharmacyOutpatientRespVO> convertList(List<PharmacyOutpatientDO> list) {
        if (list == null) {
            return null;
        }
        return list.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public PageResult<PharmacyOutpatientRespVO> convertPage(PageResult<PharmacyOutpatientDO> page) {
        if (page == null) {
            return null;
        }
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

    public List<PharmacyOutpatientExcelVO> convertList02(List<PharmacyOutpatientDO> list) {
        if (list == null) {
            return null;
        }
        return list.stream()
                .map(this::convertToExcel)
                .collect(Collectors.toList());
    }

    private PharmacyOutpatientExcelVO convertToExcel(PharmacyOutpatientDO bean) {
        if (bean == null) {
            return null;
        }
        PharmacyOutpatientExcelVO excelVO = new PharmacyOutpatientExcelVO();
        excelVO.setId(bean.getId());
        excelVO.setPrescriptionId(bean.getPrescriptionId());
        excelVO.setMedicineName(bean.getMedicineName());
        excelVO.setMedicineUsage(bean.getMedicineUsage());
        excelVO.setStartUsageTime(bean.getStartUsageTime());
        excelVO.setStopUsageTime(bean.getStopUsageTime());
        excelVO.setStopReason(bean.getStopReason());
        excelVO.setMrpType(bean.getMrpType());
        excelVO.setWeight(bean.getWeight());
        excelVO.setMrpDesc(bean.getMrpDesc());
        excelVO.setCreateTime(bean.getCreateTime());
        return excelVO;
    }

}
