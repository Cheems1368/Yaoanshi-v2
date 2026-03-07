package com.sqlapk.medicine.module.patient.service.inpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatienteducation.InpatientEducationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatienteducation.InpatientEducationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatienteducation.InpatientEducationMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_EDUCATION_NOT_EXISTS;

/**
 * 住院用药教育 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class InpatientEducationServiceImpl implements InpatientEducationService {

    @Resource
    private InpatientEducationMapper inpatientEducationMapper;

    @Override
    public Long createInpatientEducation(InpatientEducationCreateReqVO createReqVO) {
        // 插入
        InpatientEducationDO inpatientEducation = InpatientEducationConvert.INSTANCE.convert(createReqVO);
        inpatientEducationMapper.insert(inpatientEducation);
        // 返回
        return inpatientEducation.getId();
    }

    @Override
    public void updateInpatientEducation(InpatientEducationUpdateReqVO updateReqVO) {
        // 校验存在
        validateInpatientEducationExists(updateReqVO.getId());
        // 更新
        InpatientEducationDO updateObj = InpatientEducationConvert.INSTANCE.convert(updateReqVO);
        inpatientEducationMapper.updateById(updateObj);
    }

    @Override
    public void deleteInpatientEducation(Long id) {
        // 校验存在
        validateInpatientEducationExists(id);
        // 删除
        inpatientEducationMapper.deleteById(id);
    }

    private void validateInpatientEducationExists(Long id) {
        if (inpatientEducationMapper.selectById(id) == null) {
            throw exception(INPATIENT_EDUCATION_NOT_EXISTS);
        }
    }

    @Override
    public InpatientEducationDO getInpatientEducation(Long id) {
        return inpatientEducationMapper.selectById(id);
    }

    @Override
    public List<InpatientEducationDO> getInpatientEducationList(Collection<Long> ids) {
        return inpatientEducationMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<InpatientEducationDO> getInpatientEducationPage(InpatientEducationPageReqVO pageReqVO) {
        return inpatientEducationMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InpatientEducationDO> getInpatientEducationList(InpatientEducationExportReqVO exportReqVO) {
        return inpatientEducationMapper.selectList(exportReqVO);
    }

}
