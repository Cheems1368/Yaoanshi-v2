package com.sqlapk.medicine.module.patient.service.outpatientexam;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.outpatientexam.OutpatientExamConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientexam.OutpatientExamDO;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatientexam.OutpatientExamMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.OUTPATIENT_EXAM_NOT_EXISTS;

/**
 * 门诊药学检查项目明细 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class OutpatientExamServiceImpl implements OutpatientExamService {

    @Resource
    private OutpatientExamMapper outpatientExamMapper;

    @Override
    public Long createOutpatientExam(OutpatientExamCreateReqVO createReqVO) {
        // 插入
        OutpatientExamDO outpatientExam = OutpatientExamConvert.INSTANCE.convert(createReqVO);
        outpatientExamMapper.insert(outpatientExam);
        // 返回
        return outpatientExam.getId();
    }

    @Override
    public void updateOutpatientExam(OutpatientExamUpdateReqVO updateReqVO) {
        // 校验存在
        validateOutpatientExamExists(updateReqVO.getId());
        // 更新
        OutpatientExamDO updateObj = OutpatientExamConvert.INSTANCE.convert(updateReqVO);
        outpatientExamMapper.updateById(updateObj);
    }

    @Override
    public void deleteOutpatientExam(Long id) {
        // 校验存在
        validateOutpatientExamExists(id);
        // 删除
        outpatientExamMapper.deleteById(id);
    }

    private void validateOutpatientExamExists(Long id) {
        if (outpatientExamMapper.selectById(id) == null) {
            throw exception(OUTPATIENT_EXAM_NOT_EXISTS);
        }
    }

    @Override
    public OutpatientExamDO getOutpatientExam(Long id) {
        return outpatientExamMapper.selectById(id);
    }

    @Override
    public List<OutpatientExamDO> getOutpatientExamList(Collection<Long> ids) {
        return outpatientExamMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<OutpatientExamDO> getOutpatientExamPage(OutpatientExamPageReqVO pageReqVO) {
        return outpatientExamMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OutpatientExamDO> getOutpatientExamList(OutpatientExamExportReqVO exportReqVO) {
        return outpatientExamMapper.selectList(exportReqVO);
    }

}
