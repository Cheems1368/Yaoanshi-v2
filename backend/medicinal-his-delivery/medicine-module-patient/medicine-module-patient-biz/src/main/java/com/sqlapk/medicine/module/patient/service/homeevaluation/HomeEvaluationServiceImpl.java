package com.sqlapk.medicine.module.patient.service.homeevaluation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.homeevaluation.HomeEvaluationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeevaluation.HomeEvaluationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.homeevaluation.HomeEvaluationMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.HOME_EVALUATION_NOT_EXISTS;

/**
 * 居家随访评估 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class HomeEvaluationServiceImpl implements HomeEvaluationService {

    @Resource
    private HomeEvaluationMapper homeEvaluationMapper;

    @Override
    public Long createHomeEvaluation(HomeEvaluationCreateReqVO createReqVO) {
        // 插入
        HomeEvaluationDO homeEvaluation = HomeEvaluationConvert.INSTANCE.convert(createReqVO);
        homeEvaluationMapper.insert(homeEvaluation);
        // 返回
        return homeEvaluation.getId();
    }

    @Override
    public void updateHomeEvaluation(HomeEvaluationUpdateReqVO updateReqVO) {
        // 校验存在
        validateHomeEvaluationExists(updateReqVO.getId());
        // 更新
        HomeEvaluationDO updateObj = HomeEvaluationConvert.INSTANCE.convert(updateReqVO);
        homeEvaluationMapper.updateById(updateObj);
    }

    @Override
    public void deleteHomeEvaluation(Long id) {
        // 校验存在
        validateHomeEvaluationExists(id);
        // 删除
        homeEvaluationMapper.deleteById(id);
    }

    private void validateHomeEvaluationExists(Long id) {
        if (homeEvaluationMapper.selectById(id) == null) {
            throw exception(HOME_EVALUATION_NOT_EXISTS);
        }
    }

    @Override
    public HomeEvaluationDO getHomeEvaluation(Long id) {
        return homeEvaluationMapper.selectById(id);
    }

    @Override
    public List<HomeEvaluationDO> getHomeEvaluationList(Collection<Long> ids) {
        return homeEvaluationMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<HomeEvaluationDO> getHomeEvaluationPage(HomeEvaluationPageReqVO pageReqVO) {
        return homeEvaluationMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HomeEvaluationDO> getHomeEvaluationList(HomeEvaluationExportReqVO exportReqVO) {
        return homeEvaluationMapper.selectList(exportReqVO);
    }

}
