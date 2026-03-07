package com.sqlapk.medicine.module.patient.service.inpatientevaluation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatientevaluation.InpatientEvaluationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientevaluation.InpatientEvaluationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientevaluation.InpatientEvaluationMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_EVALUATION_NOT_EXISTS;

/**
 * 住院药学用药评估 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class InpatientEvaluationServiceImpl implements InpatientEvaluationService {

    @Resource
    private InpatientEvaluationMapper inpatientEvaluationMapper;

    @Override
    public Long createInpatientEvaluation(InpatientEvaluationCreateReqVO createReqVO) {
        // 插入
        InpatientEvaluationDO inpatientEvaluation = InpatientEvaluationConvert.INSTANCE.convert(createReqVO);
        inpatientEvaluationMapper.insert(inpatientEvaluation);
        // 返回
        return inpatientEvaluation.getId();
    }

    @Override
    public void updateInpatientEvaluation(InpatientEvaluationUpdateReqVO updateReqVO) {
        // 校验存在
        validateInpatientEvaluationExists(updateReqVO.getId());
        // 更新
        InpatientEvaluationDO updateObj = InpatientEvaluationConvert.INSTANCE.convert(updateReqVO);
        inpatientEvaluationMapper.updateById(updateObj);
    }

    @Override
    public void deleteInpatientEvaluation(Long id) {
        // 校验存在
        validateInpatientEvaluationExists(id);
        // 删除
        inpatientEvaluationMapper.deleteById(id);
    }

    private void validateInpatientEvaluationExists(Long id) {
        if (inpatientEvaluationMapper.selectById(id) == null) {
            throw exception(INPATIENT_EVALUATION_NOT_EXISTS);
        }
    }

    @Override
    public InpatientEvaluationDO getInpatientEvaluation(Long id) {
        return inpatientEvaluationMapper.selectById(id);
    }

    @Override
    public List<InpatientEvaluationDO> getInpatientEvaluationList(Collection<Long> ids) {
        return inpatientEvaluationMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<InpatientEvaluationDO> getInpatientEvaluationPage(InpatientEvaluationPageReqVO pageReqVO) {
        return inpatientEvaluationMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InpatientEvaluationDO> getInpatientEvaluationList(InpatientEvaluationExportReqVO exportReqVO) {
        return inpatientEvaluationMapper.selectList(exportReqVO);
    }

}
