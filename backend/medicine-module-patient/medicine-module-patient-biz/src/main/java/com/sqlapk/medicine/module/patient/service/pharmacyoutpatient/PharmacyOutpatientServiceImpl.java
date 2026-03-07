package com.sqlapk.medicine.module.patient.service.pharmacyoutpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.pharmacyoutpatient.PharmacyOutpatientConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient.PharmacyOutpatientDO;
import com.sqlapk.medicine.module.patient.dal.mysql.pharmacyoutpatient.PharmacyOutpatientMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.PHARMACY_OUTPATIENT_NOT_EXISTS;

/**
 * 药学门诊 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class PharmacyOutpatientServiceImpl implements PharmacyOutpatientService {

    @Resource
    private PharmacyOutpatientMapper pharmacyOutpatientMapper;

    @Resource
    private PharmacyOutpatientConvert pharmacyOutpatientConvert;

    @Override
    public Long createPharmacyOutpatient(PharmacyOutpatientCreateReqVO createReqVO) {
        // 插入
        PharmacyOutpatientDO pharmacyOutpatient = pharmacyOutpatientConvert.convert(createReqVO);
        pharmacyOutpatientMapper.insert(pharmacyOutpatient);
        // 返回
        return pharmacyOutpatient.getId();
    }

    @Override
    public void updatePharmacyOutpatient(PharmacyOutpatientUpdateReqVO updateReqVO) {
        // 校验存在
        validatePharmacyOutpatientExists(updateReqVO.getId());
        // 更新
        PharmacyOutpatientDO updateObj = pharmacyOutpatientConvert.convert(updateReqVO);
        pharmacyOutpatientMapper.updateById(updateObj);
    }

    @Override
    public void deletePharmacyOutpatient(Long id) {
        // 校验存在
        validatePharmacyOutpatientExists(id);
        // 删除
        pharmacyOutpatientMapper.deleteById(id);
    }

    private void validatePharmacyOutpatientExists(Long id) {
        if (pharmacyOutpatientMapper.selectById(id) == null) {
            throw exception(PHARMACY_OUTPATIENT_NOT_EXISTS);
        }
    }

    @Override
    public PharmacyOutpatientDO getPharmacyOutpatient(Long id) {
        return pharmacyOutpatientMapper.selectById(id);
    }

    @Override
    public List<PharmacyOutpatientDO> getPharmacyOutpatientList(Collection<Long> ids) {
        return pharmacyOutpatientMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PharmacyOutpatientDO> getPharmacyOutpatientPage(PharmacyOutpatientPageReqVO pageReqVO) {
        return pharmacyOutpatientMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PharmacyOutpatientDO> getPharmacyOutpatientList(PharmacyOutpatientExportReqVO exportReqVO) {
        return pharmacyOutpatientMapper.selectList(exportReqVO);
    }

}
