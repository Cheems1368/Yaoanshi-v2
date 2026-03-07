package com.sqlapk.medicine.module.patient.service.outpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.outpatienteducation.OutpatientEducationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatienteducation.OutpatientEducationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatienteducation.OutpatientEducationMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.OUTPATIENT_EDUCATION_NOT_EXISTS;

/**
 * 门诊处方用药教育明细 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class OutpatientEducationServiceImpl implements OutpatientEducationService {

    @Resource
    private OutpatientEducationMapper outpatientEducationMapper;

    @Override
    public Long createOutpatientEducation(OutpatientEducationCreateReqVO createReqVO) {
        // 插入
        OutpatientEducationDO outpatientEducation = OutpatientEducationConvert.INSTANCE.convert(createReqVO);
        outpatientEducationMapper.insert(outpatientEducation);
        // 返回
        return outpatientEducation.getId();
    }

    @Override
    public void updateOutpatientEducation(OutpatientEducationUpdateReqVO updateReqVO) {
        // 校验存在
        validateOutpatientEducationExists(updateReqVO.getId());
        // 更新
        OutpatientEducationDO updateObj = OutpatientEducationConvert.INSTANCE.convert(updateReqVO);
        outpatientEducationMapper.updateById(updateObj);
    }

    @Override
    public void deleteOutpatientEducation(Long id) {
        // 校验存在
        validateOutpatientEducationExists(id);
        // 删除
        outpatientEducationMapper.deleteById(id);
    }

    private void validateOutpatientEducationExists(Long id) {
        if (outpatientEducationMapper.selectById(id) == null) {
            throw exception(OUTPATIENT_EDUCATION_NOT_EXISTS);
        }
    }

    @Override
    public OutpatientEducationDO getOutpatientEducation(Long id) {
        return outpatientEducationMapper.selectById(id);
    }

    @Override
    public List<OutpatientEducationDO> getOutpatientEducationList(Collection<Long> ids) {
        return outpatientEducationMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<OutpatientEducationDO> getOutpatientEducationPage(OutpatientEducationPageReqVO pageReqVO) {
        return outpatientEducationMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OutpatientEducationDO> getOutpatientEducationList(OutpatientEducationExportReqVO exportReqVO) {
        return outpatientEducationMapper.selectList(exportReqVO);
    }

}
