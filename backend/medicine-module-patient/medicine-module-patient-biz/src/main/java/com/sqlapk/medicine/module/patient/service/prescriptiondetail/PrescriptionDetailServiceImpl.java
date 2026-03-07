package com.sqlapk.medicine.module.patient.service.prescriptiondetail;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.prescriptiondetail.PrescriptionDetailConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptiondetail.PrescriptionDetailDO;
import com.sqlapk.medicine.module.patient.dal.mysql.prescriptiondetail.PrescriptionDetailMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.PRESCRIPTION_DETAIL_NOT_EXISTS;

/**
 * 患者处方 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class PrescriptionDetailServiceImpl implements PrescriptionDetailService {

    @Resource
    private PrescriptionDetailMapper prescriptionDetailMapper;

    @Resource
    private PrescriptionDetailConvert prescriptionDetailConvert;

    @Override
    public Long createPrescriptionDetail(PrescriptionDetailCreateReqVO createReqVO) {
        // 插入
        PrescriptionDetailDO prescriptionDetail = prescriptionDetailConvert.convert(createReqVO);
        prescriptionDetailMapper.insert(prescriptionDetail);
        // 返回
        return prescriptionDetail.getId();
    }

    @Override
    public void updatePrescriptionDetail(PrescriptionDetailUpdateReqVO updateReqVO) {
        // 校验存在
        validatePrescriptionDetailExists(updateReqVO.getId());
        // 更新
        PrescriptionDetailDO updateObj = prescriptionDetailConvert.convert(updateReqVO);
        prescriptionDetailMapper.updateById(updateObj);
    }

    @Override
    public void deletePrescriptionDetail(Long id) {
        // 校验存在
        validatePrescriptionDetailExists(id);
        // 删除
        prescriptionDetailMapper.deleteById(id);
    }

    private void validatePrescriptionDetailExists(Long id) {
        if (prescriptionDetailMapper.selectById(id) == null) {
            throw exception(PRESCRIPTION_DETAIL_NOT_EXISTS);
        }
    }

    @Override
    public PrescriptionDetailDO getPrescriptionDetail(Long id) {
        return prescriptionDetailMapper.selectById(id);
    }

    @Override
    public List<PrescriptionDetailDO> getPrescriptionDetailList(Collection<Long> ids) {
        return prescriptionDetailMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PrescriptionDetailDO> getPrescriptionDetailPage(PrescriptionDetailPageReqVO pageReqVO) {
        return prescriptionDetailMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PrescriptionDetailDO> getPrescriptionDetailList(PrescriptionDetailExportReqVO exportReqVO) {
        return prescriptionDetailMapper.selectList(exportReqVO);
    }

}
