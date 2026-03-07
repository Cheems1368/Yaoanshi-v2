package com.sqlapk.medicine.module.patient.service.surveys;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import com.sqlapk.medicine.module.patient.controller.admin.surveys.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.surveys.SurveysDO;
import com.sqlapk.medicine.framework.common.pojo.PageResult;

import com.sqlapk.medicine.module.patient.convert.surveys.SurveysConvert;
import com.sqlapk.medicine.module.patient.dal.mysql.surveys.SurveysMapper;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.*;

/**
 * 问卷 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class SurveysServiceImpl implements SurveysService {

    @Resource
    private SurveysMapper surveysMapper;

    @Override
    public Long createSurveys(SurveysCreateReqVO createReqVO) {
        // 插入
        SurveysDO surveys = SurveysConvert.INSTANCE.convert(createReqVO);
        surveysMapper.insert(surveys);
        // 返回
        return surveys.getId();
    }

    @Override
    public void updateSurveys(SurveysUpdateReqVO updateReqVO) {
        // 校验存在
        validateSurveysExists(updateReqVO.getId());
        // 更新
        SurveysDO updateObj = SurveysConvert.INSTANCE.convert(updateReqVO);
        surveysMapper.updateById(updateObj);
    }

    @Override
    public void deleteSurveys(Long id) {
        // 校验存在
        validateSurveysExists(id);
        // 删除
        surveysMapper.deleteById(id);
    }

    private void validateSurveysExists(Long id) {
        if (surveysMapper.selectById(id) == null) {
            throw exception(SURVEYS_NOT_EXISTS);
        }
    }

    @Override
    public SurveysDO getSurveys(Long id) {
        return surveysMapper.selectById(id);
    }

    @Override
    public List<SurveysDO> getSurveysList(Collection<Long> ids) {
        return surveysMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SurveysDO> getSurveysPage(SurveysPageReqVO pageReqVO) {
        return surveysMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SurveysDO> getSurveysList(SurveysExportReqVO exportReqVO) {
        return surveysMapper.selectList(exportReqVO);
    }

}
