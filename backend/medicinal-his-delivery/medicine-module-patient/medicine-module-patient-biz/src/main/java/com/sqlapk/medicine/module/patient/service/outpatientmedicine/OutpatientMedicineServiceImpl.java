package com.sqlapk.medicine.module.patient.service.outpatientmedicine;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicinePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.outpatientmedicine.OutpatientMedicineConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientmedicine.OutpatientMedicineDO;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatientmedicine.OutpatientMedicineMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.OUTPATIENT_MEDICINE_NOT_EXISTS;

/**
 * 药学门诊 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class OutpatientMedicineServiceImpl implements OutpatientMedicineService {

    @Resource
    private OutpatientMedicineMapper outpatientMedicineMapper;

    @Override
    public Long createOutpatientMedicine(OutpatientMedicineCreateReqVO createReqVO) {
        // 插入
        OutpatientMedicineDO outpatientMedicine = OutpatientMedicineConvert.INSTANCE.convert(createReqVO);
        outpatientMedicineMapper.insert(outpatientMedicine);
        // 返回
        return outpatientMedicine.getId();
    }

    @Override
    public void updateOutpatientMedicine(OutpatientMedicineUpdateReqVO updateReqVO) {
        // 校验存在
        validateOutpatientMedicineExists(updateReqVO.getId());
        // 更新
        OutpatientMedicineDO updateObj = OutpatientMedicineConvert.INSTANCE.convert(updateReqVO);
        outpatientMedicineMapper.updateById(updateObj);
    }

    @Override
    public void deleteOutpatientMedicine(Long id) {
        // 校验存在
        validateOutpatientMedicineExists(id);
        // 删除
        outpatientMedicineMapper.deleteById(id);
    }

    private void validateOutpatientMedicineExists(Long id) {
        if (outpatientMedicineMapper.selectById(id) == null) {
            throw exception(OUTPATIENT_MEDICINE_NOT_EXISTS);
        }
    }

    @Override
    public OutpatientMedicineDO getOutpatientMedicine(Long id) {
        return outpatientMedicineMapper.selectById(id);
    }

    @Override
    public List<OutpatientMedicineDO> getOutpatientMedicineList(Collection<Long> ids) {
        return outpatientMedicineMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<OutpatientMedicineDO> getOutpatientMedicinePage(OutpatientMedicinePageReqVO pageReqVO) {
        return outpatientMedicineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OutpatientMedicineDO> getOutpatientMedicineList(OutpatientMedicineExportReqVO exportReqVO) {
        return outpatientMedicineMapper.selectList(exportReqVO);
    }

}
