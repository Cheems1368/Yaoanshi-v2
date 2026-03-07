package com.sqlapk.medicine.module.patient.service.inpatientconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatientconsultation.InpatientConsultationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientconsultation.InpatientConsultationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientconsultation.InpatientConsultationMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_CONSULTATION_NOT_EXISTS;

/**
 * 主要用药咨询 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class InpatientConsultationServiceImpl implements InpatientConsultationService {

    @Resource
    private InpatientConsultationMapper inpatientConsultationMapper;

    @Override
    public Long createInpatientConsultation(InpatientConsultationCreateReqVO createReqVO) {
        // 插入
        InpatientConsultationDO inpatientConsultation = InpatientConsultationConvert.INSTANCE.convert(createReqVO);
        inpatientConsultationMapper.insert(inpatientConsultation);
        // 返回
        return inpatientConsultation.getId();
    }

    @Override
    public void updateInpatientConsultation(InpatientConsultationUpdateReqVO updateReqVO) {
        // 校验存在
        validateInpatientConsultationExists(updateReqVO.getId());
        // 更新
        InpatientConsultationDO updateObj = InpatientConsultationConvert.INSTANCE.convert(updateReqVO);
        inpatientConsultationMapper.updateById(updateObj);
    }

    @Override
    public void deleteInpatientConsultation(Long id) {
        // 校验存在
        validateInpatientConsultationExists(id);
        // 删除
        inpatientConsultationMapper.deleteById(id);
    }

    private void validateInpatientConsultationExists(Long id) {
        if (inpatientConsultationMapper.selectById(id) == null) {
            throw exception(INPATIENT_CONSULTATION_NOT_EXISTS);
        }
    }

    @Override
    public InpatientConsultationDO getInpatientConsultation(Long id) {
        return inpatientConsultationMapper.selectById(id);
    }

    @Override
    public List<InpatientConsultationDO> getInpatientConsultationList(Collection<Long> ids) {
        return inpatientConsultationMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<InpatientConsultationDO> getInpatientConsultationPage(InpatientConsultationPageReqVO pageReqVO) {
        return inpatientConsultationMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InpatientConsultationDO> getInpatientConsultationList(InpatientConsultationExportReqVO exportReqVO) {
        return inpatientConsultationMapper.selectList(exportReqVO);
    }

}
