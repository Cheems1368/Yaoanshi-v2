package com.sqlapk.medicine.module.patient.service.education;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.eduction.EductionConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.education.EducationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.education.EducationMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.EDUCTION_NOT_EXISTS;

/**
 * 用药教育 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class EducationServiceImpl implements EducationService {

    @Resource
    private EducationMapper eductionMapper;

    @Override
    public Long createEduction(EducationCreateReqVO createReqVO) {
        // 插入
        EducationDO eduction = EductionConvert.INSTANCE.convert(createReqVO);
        eductionMapper.insert(eduction);
        // 返回
        return eduction.getId();
    }

    @Override
    public void updateEduction(EducationUpdateReqVO updateReqVO) {
        // 校验存在
        validateEductionExists(updateReqVO.getId());
        // 更新
        EducationDO updateObj = EductionConvert.INSTANCE.convert(updateReqVO);
        eductionMapper.updateById(updateObj);
    }

    @Override
    public void deleteEduction(Long id) {
        // 校验存在
        validateEductionExists(id);
        // 删除
        eductionMapper.deleteById(id);
    }

    private void validateEductionExists(Long id) {
        if (eductionMapper.selectById(id) == null) {
            throw exception(EDUCTION_NOT_EXISTS);
        }
    }

    @Override
    public EducationDO getEduction(Long id) {
        return eductionMapper.selectById(id);
    }

    @Override
    public List<EducationDO> getEductionList(Collection<Long> ids) {
        return eductionMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<EducationDO> getEductionPage(EducationPageReqVO pageReqVO) {
        return eductionMapper.selectPage(pageReqVO);
    }

    @Override
    public List<EducationDO> getEductionList(EducationExportReqVO exportReqVO) {
        return eductionMapper.selectList(exportReqVO);
    }

}
