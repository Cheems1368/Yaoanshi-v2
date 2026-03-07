package com.sqlapk.medicine.module.patient.service.medicinereconciliation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.medicinereconciliation.MedicineReconciliationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliation.MedicineReconciliationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliationdetail.MedicineReconciliationDetailDO;
import com.sqlapk.medicine.module.patient.dal.mysql.medicinereconciliation.MedicineReconciliationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.medicinereconciliationdetail.MedicineReconciliationDetailMapper;
import com.sqlapk.medicine.module.patient.service.medicinereconciliationdetail.MedicineReconciliationDetailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.MEDICINE_RECONCILIATION_NOT_EXISTS;

/**
 * 药物重整剂量 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class MedicineReconciliationServiceImpl implements MedicineReconciliationService {

    @Resource
    private MedicineReconciliationMapper medicineReconciliationMapper;
    @Resource
    private MedicineReconciliationDetailService medicineReconciliationDetailService;
    @Resource
    private MedicineReconciliationDetailMapper medicineReconciliationDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMedicineReconciliation(MedicineReconciliationCreateReqVO createReqVO) {
        // 插入
        MedicineReconciliationDO medicineReconciliation = MedicineReconciliationConvert.INSTANCE.convert(createReqVO);
        medicineReconciliationMapper.insert(medicineReconciliation);

        createReqVO.getDetails().forEach(item -> {
            item.setReconciliationId(medicineReconciliation.getId());
            medicineReconciliationDetailService.createMedicineReconciliationDetail(item);
        });
        // 返回
        return medicineReconciliation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMedicineReconciliation(MedicineReconciliationUpdateReqVO updateReqVO) {
        // 校验存在
        validateMedicineReconciliationExists(updateReqVO.getId());
        //delete before
        LambdaQueryWrapperX<MedicineReconciliationDetailDO> detailQuery = new LambdaQueryWrapperX<MedicineReconciliationDetailDO>().eq(MedicineReconciliationDetailDO::getReconciliationId, updateReqVO.getId());
        medicineReconciliationDetailMapper.delete(detailQuery);
        // 更新
        MedicineReconciliationDO updateObj = MedicineReconciliationConvert.INSTANCE.convert(updateReqVO);
        medicineReconciliationMapper.updateById(updateObj);
        //save detail
        updateReqVO.getDetails().forEach(item -> {
            item.setReconciliationId(updateReqVO.getId());
            medicineReconciliationDetailService.createMedicineReconciliationDetail(item);
        });
    }

    @Override
    public void deleteMedicineReconciliation(Long id) {
        // 校验存在
        validateMedicineReconciliationExists(id);
        // 删除
        medicineReconciliationMapper.deleteById(id);
    }

    private void validateMedicineReconciliationExists(Long id) {
        if (medicineReconciliationMapper.selectById(id) == null) {
            throw exception(MEDICINE_RECONCILIATION_NOT_EXISTS);
        }
    }

    @Override
    public MedicineReconciliationDO getMedicineReconciliation(Long id) {
        return medicineReconciliationMapper.selectById(id);
    }

    @Override
    public List<MedicineReconciliationDO> getMedicineReconciliationList(Collection<Long> ids) {
        return medicineReconciliationMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MedicineReconciliationDO> getMedicineReconciliationPage(MedicineReconciliationPageReqVO pageReqVO) {
        return medicineReconciliationMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MedicineReconciliationDO> getMedicineReconciliationList(MedicineReconciliationExportReqVO exportReqVO) {
        return medicineReconciliationMapper.selectList(exportReqVO);
    }

}
