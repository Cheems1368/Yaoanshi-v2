package com.sqlapk.medicine.module.patient.service.medicinereconciliationdetail;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.medicinereconciliationdetail.MedicineReconciliationDetailConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliationdetail.MedicineReconciliationDetailDO;
import com.sqlapk.medicine.module.patient.dal.mysql.medicinereconciliationdetail.MedicineReconciliationDetailMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.MEDICINE_RECONCILIATION_DETAIL_NOT_EXISTS;

/**
 * 药物重整剂量 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class MedicineReconciliationDetailServiceImpl implements MedicineReconciliationDetailService {

    @Resource
    private MedicineReconciliationDetailMapper medicineReconciliationDetailMapper;

    @Override
    public Long createMedicineReconciliationDetail(MedicineReconciliationDetailCreateReqVO createReqVO) {
        // 插入
        MedicineReconciliationDetailDO medicineReconciliationDetail = MedicineReconciliationDetailConvert.INSTANCE.convert(createReqVO);
        medicineReconciliationDetailMapper.insert(medicineReconciliationDetail);
        // 返回
        return medicineReconciliationDetail.getId();
    }

    @Override
    public void updateMedicineReconciliationDetail(MedicineReconciliationDetailUpdateReqVO updateReqVO) {
        // 校验存在
        validateMedicineReconciliationDetailExists(updateReqVO.getId());
        // 更新
        MedicineReconciliationDetailDO updateObj = MedicineReconciliationDetailConvert.INSTANCE.convert(updateReqVO);
        medicineReconciliationDetailMapper.updateById(updateObj);
    }

    @Override
    public void deleteMedicineReconciliationDetail(Long id) {
        // 校验存在
        validateMedicineReconciliationDetailExists(id);
        // 删除
        medicineReconciliationDetailMapper.deleteById(id);
    }

    private void validateMedicineReconciliationDetailExists(Long id) {
        if (medicineReconciliationDetailMapper.selectById(id) == null) {
            throw exception(MEDICINE_RECONCILIATION_DETAIL_NOT_EXISTS);
        }
    }

    @Override
    public MedicineReconciliationDetailDO getMedicineReconciliationDetail(Long id) {
        return medicineReconciliationDetailMapper.selectById(id);
    }

    @Override
    public List<MedicineReconciliationDetailDO> getMedicineReconciliationDetailList(Collection<Long> ids) {
        return medicineReconciliationDetailMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MedicineReconciliationDetailDO> getMedicineReconciliationDetailPage(MedicineReconciliationDetailPageReqVO pageReqVO) {
        return medicineReconciliationDetailMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MedicineReconciliationDetailDO> getMedicineReconciliationDetailList(MedicineReconciliationDetailExportReqVO exportReqVO) {
        return medicineReconciliationDetailMapper.selectList(exportReqVO);
    }

}
