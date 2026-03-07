package com.sqlapk.medicine.module.patient.service.outpatientcheck;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.outpatientcheck.OutpatientCheckConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientcheck.OutpatientCheckDO;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatientcheck.OutpatientCheckMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.OUTPATIENT_CHECK_NOT_EXISTS;

/**
 * 处方审核 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class OutpatientCheckServiceImpl implements OutpatientCheckService {

    @Resource
    private OutpatientCheckMapper outpatientCheckMapper;

    @Override
    public Long createOutpatientCheck(OutpatientCheckCreateReqVO createReqVO) {
        // 插入
        OutpatientCheckDO outpatientCheck = OutpatientCheckConvert.INSTANCE.convert(createReqVO);
        outpatientCheckMapper.insert(outpatientCheck);
        // 返回
        return outpatientCheck.getId();
    }

    @Override
    public void updateOutpatientCheck(OutpatientCheckUpdateReqVO updateReqVO) {
        // 校验存在
        validateOutpatientCheckExists(updateReqVO.getId());
        // 更新
        OutpatientCheckDO updateObj = OutpatientCheckConvert.INSTANCE.convert(updateReqVO);
        outpatientCheckMapper.updateById(updateObj);
    }

    @Override
    public void deleteOutpatientCheck(Long id) {
        // 校验存在
        validateOutpatientCheckExists(id);
        // 删除
        outpatientCheckMapper.deleteById(id);
    }

    private void validateOutpatientCheckExists(Long id) {
        if (outpatientCheckMapper.selectById(id) == null) {
            throw exception(OUTPATIENT_CHECK_NOT_EXISTS);
        }
    }

    @Override
    public OutpatientCheckDO getOutpatientCheck(Long id) {
        return outpatientCheckMapper.selectById(id);
    }

    @Override
    public List<OutpatientCheckDO> getOutpatientCheckList(Collection<Long> ids) {
        return outpatientCheckMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<OutpatientCheckDO> getOutpatientCheckPage(OutpatientCheckPageReqVO pageReqVO) {
        return outpatientCheckMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OutpatientCheckDO> getOutpatientCheckList(OutpatientCheckExportReqVO exportReqVO) {
        return outpatientCheckMapper.selectList(exportReqVO);
    }

}
