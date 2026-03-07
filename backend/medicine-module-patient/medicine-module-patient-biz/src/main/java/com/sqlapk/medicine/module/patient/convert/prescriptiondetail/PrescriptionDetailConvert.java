package com.sqlapk.medicine.module.patient.convert.prescriptiondetail;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptiondetail.PrescriptionDetailDO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 患者处?Convert
 *
 * @author 管理员 */
@Component
public class PrescriptionDetailConvert {

    public PrescriptionDetailDO convert(PrescriptionDetailCreateReqVO bean) {
        if (bean == null) {
            return null;
        }
        return PrescriptionDetailDO.builder()
                .prescriptionId(bean.getPrescriptionId())
                .medicineId(bean.getMedicineId())
                .dosage(bean.getDosage())
                .usageInfo(bean.getUsageInfo())
                .frequency(bean.getFrequency())
                .quality(bean.getQuality())
                .unit(bean.getUnit())
                .build();
    }

    public PrescriptionDetailDO convert(PrescriptionDetailUpdateReqVO bean) {
        if (bean == null) {
            return null;
        }
        return PrescriptionDetailDO.builder()
                .id(bean.getId())
                .prescriptionId(bean.getPrescriptionId())
                .medicineId(bean.getMedicineId())
                .dosage(bean.getDosage())
                .usageInfo(bean.getUsageInfo())
                .frequency(bean.getFrequency())
                .quality(bean.getQuality())
                .unit(bean.getUnit())
                .build();
    }

    public PrescriptionDetailRespVO convert(PrescriptionDetailDO bean) {
        if (bean == null) {
            return null;
        }
        PrescriptionDetailRespVO respVO = new PrescriptionDetailRespVO();
        respVO.setId(bean.getId());
        respVO.setPrescriptionId(bean.getPrescriptionId());
        respVO.setMedicineId(bean.getMedicineId());
        respVO.setDosage(bean.getDosage());
        respVO.setUsageInfo(bean.getUsageInfo());
        respVO.setFrequency(bean.getFrequency());
        respVO.setQuality(bean.getQuality());
        respVO.setUnit(bean.getUnit());
        respVO.setCreateTime(bean.getCreateTime());
        return respVO;
    }

    public List<PrescriptionDetailRespVO> convertList(List<PrescriptionDetailDO> list) {
        if (list == null) {
            return null;
        }
        return list.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public PageResult<PrescriptionDetailRespVO> convertPage(PageResult<PrescriptionDetailDO> page) {
        if (page == null) {
            return null;
        }
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

    public List<PrescriptionDetailExcelVO> convertList02(List<PrescriptionDetailDO> list) {
        if (list == null) {
            return null;
        }
        return list.stream()
                .map(this::convertToExcel)
                .collect(Collectors.toList());
    }

    private PrescriptionDetailExcelVO convertToExcel(PrescriptionDetailDO bean) {
        if (bean == null) {
            return null;
        }
        PrescriptionDetailExcelVO excelVO = new PrescriptionDetailExcelVO();
        excelVO.setId(bean.getId());
        excelVO.setPrescriptionId(bean.getPrescriptionId());
        excelVO.setMedicineId(bean.getMedicineId());
        excelVO.setDosage(bean.getDosage());
        excelVO.setUsage(bean.getUsageInfo());
        excelVO.setFrequency(bean.getFrequency());
        excelVO.setQuality(bean.getQuality());
        excelVO.setUnit(bean.getUnit());
        excelVO.setCreateTime(bean.getCreateTime());
        return excelVO;
    }

}
