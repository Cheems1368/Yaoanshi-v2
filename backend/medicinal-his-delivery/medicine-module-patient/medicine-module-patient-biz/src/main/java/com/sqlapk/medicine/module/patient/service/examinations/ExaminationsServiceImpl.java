package com.sqlapk.medicine.module.patient.service.examinations;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.examinations.ExaminationsConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.examinations.ExaminationsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.examinations.ExaminationsMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.EXAMINATIONS_NOT_EXISTS;

/**
 * 检查项目 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class ExaminationsServiceImpl implements ExaminationsService {

    @Resource
    private ExaminationsMapper examinationsMapper;

    @Override
    public Long createExaminations(ExaminationsCreateReqVO createReqVO) {
        // 插入
        ExaminationsDO examinations = ExaminationsConvert.INSTANCE.convert(createReqVO);
        examinationsMapper.insert(examinations);
        // 返回
        return examinations.getId();
    }

    @Override
    public void updateExaminations(ExaminationsUpdateReqVO updateReqVO) {
        // 校验存在
        validateExaminationsExists(updateReqVO.getId());
        // 更新
        ExaminationsDO updateObj = ExaminationsConvert.INSTANCE.convert(updateReqVO);
        examinationsMapper.updateById(updateObj);
    }

    @Override
    public void deleteExaminations(Long id) {
        // 校验存在
        validateExaminationsExists(id);
        // 删除
        examinationsMapper.deleteById(id);
    }

    private void validateExaminationsExists(Long id) {
        if (examinationsMapper.selectById(id) == null) {
            throw exception(EXAMINATIONS_NOT_EXISTS);
        }
    }

    @Override
    public ExaminationsDO getExaminations(Long id) {
        return examinationsMapper.selectById(id);
    }

    @Override
    public List<ExaminationsDO> getExaminationsList(Collection<Long> ids) {
        return examinationsMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ExaminationsDO> getExaminationsPage(ExaminationsPageReqVO pageReqVO) {
        return examinationsMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ExaminationsDO> getExaminationsList(ExaminationsExportReqVO exportReqVO) {
        return examinationsMapper.selectList(exportReqVO);
    }

}
