package com.sqlapk.medicine.module.patient.convert.outpatientmedicine;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientmedicine.OutpatientMedicineDO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 药学门诊 Convert
 *
 * @author 管理员 */
@Component
public class OutpatientMedicineConvert {

    public OutpatientMedicineDO convert(OutpatientMedicineCreateReqVO bean) {
        if (bean == null) {
            return null;
        }
        return OutpatientMedicineDO.builder()
                .outpatientId(bean.getOutpatientId())
                .meicineId(bean.getMeicineId())
                .build();
    }

    public OutpatientMedicineDO convert(OutpatientMedicineUpdateReqVO bean) {
        if (bean == null) {
            return null;
        }
        return OutpatientMedicineDO.builder()
                .id(bean.getId())
                .outpatientId(bean.getOutpatientId())
                .meicineId(bean.getMeicineId())
                .build();
    }

    public OutpatientMedicineRespVO convert(OutpatientMedicineDO bean) {
        if (bean == null) {
            return null;
        }
        OutpatientMedicineRespVO respVO = new OutpatientMedicineRespVO();
        respVO.setId(bean.getId());
        respVO.setOutpatientId(bean.getOutpatientId());
        respVO.setMeicineId(bean.getMeicineId());
        respVO.setCreateTime(bean.getCreateTime());
        return respVO;
    }

    public List<OutpatientMedicineRespVO> convertList(List<OutpatientMedicineDO> list) {
        if (list == null) {
            return null;
        }
        return list.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public PageResult<OutpatientMedicineRespVO> convertPage(PageResult<OutpatientMedicineDO> page) {
        if (page == null) {
            return null;
        }
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

    public List<OutpatientMedicineExcelVO> convertList02(List<OutpatientMedicineDO> list) {
        if (list == null) {
            return null;
        }
        return list.stream()
                .map(this::convertToExcel)
                .collect(Collectors.toList());
    }

    private OutpatientMedicineExcelVO convertToExcel(OutpatientMedicineDO bean) {
        if (bean == null) {
            return null;
        }
        OutpatientMedicineExcelVO excelVO = new OutpatientMedicineExcelVO();
        excelVO.setId(bean.getId());
        excelVO.setOutpatientId(bean.getOutpatientId());
        excelVO.setMeicineId(bean.getMeicineId());
        excelVO.setCreateTime(bean.getCreateTime());
        return excelVO;
    }

}
