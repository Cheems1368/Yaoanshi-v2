package com.sqlapk.medicine.module.patient.service.inpatientadvice;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdvicePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatientadvice.InpatientAdviceConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientadvice.InpatientAdviceDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientadvice.InpatientAdviceMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_ADVICE_NOT_EXISTS;

/**
 * 住院药学不合理用药 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class InpatientAdviceServiceImpl implements InpatientAdviceService {

    @Resource
    private InpatientAdviceMapper inpatientAdviceMapper;

    @Override
    public Long createInpatientAdvice(InpatientAdviceCreateReqVO createReqVO) {
        // 插入
        InpatientAdviceDO inpatientAdvice = InpatientAdviceConvert.INSTANCE.convert(createReqVO);
        inpatientAdviceMapper.insert(inpatientAdvice);
        // 返回
        return inpatientAdvice.getId();
    }

    @Override
    public void updateInpatientAdvice(InpatientAdviceUpdateReqVO updateReqVO) {
        // 校验存在
        validateInpatientAdviceExists(updateReqVO.getId());
        // 更新
        InpatientAdviceDO updateObj = InpatientAdviceConvert.INSTANCE.convert(updateReqVO);
        inpatientAdviceMapper.updateById(updateObj);
    }

    @Override
    public void deleteInpatientAdvice(Long id) {
        // 校验存在
        validateInpatientAdviceExists(id);
        // 删除
        inpatientAdviceMapper.deleteById(id);
    }

    private void validateInpatientAdviceExists(Long id) {
        if (inpatientAdviceMapper.selectById(id) == null) {
            throw exception(INPATIENT_ADVICE_NOT_EXISTS);
        }
    }

    @Override
    public InpatientAdviceDO getInpatientAdvice(Long id) {
        return inpatientAdviceMapper.selectById(id);
    }

    @Override
    public List<InpatientAdviceDO> getInpatientAdviceList(Collection<Long> ids) {
        return inpatientAdviceMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<InpatientAdviceDO> getInpatientAdvicePage(InpatientAdvicePageReqVO pageReqVO) {
        return inpatientAdviceMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InpatientAdviceDO> getInpatientAdviceList(InpatientAdviceExportReqVO exportReqVO) {
        return inpatientAdviceMapper.selectList(exportReqVO);
    }

}
