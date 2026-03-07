package com.sqlapk.medicine.module.patient.service.homeeducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.homeeducation.HomeEducationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeeducation.HomeEducationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.homeeducation.HomeEducationMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.HOME_EDUCATION_NOT_EXISTS;

/**
 * 居家用药教育 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class HomeEducationServiceImpl implements HomeEducationService {

    @Resource
    private HomeEducationMapper homeEducationMapper;

    @Override
    public Long createHomeEducation(HomeEducationCreateReqVO createReqVO) {
        // 插入
        HomeEducationDO homeEducation = HomeEducationConvert.INSTANCE.convert(createReqVO);
        homeEducationMapper.insert(homeEducation);
        // 返回
        return homeEducation.getId();
    }

    @Override
    public void updateHomeEducation(HomeEducationUpdateReqVO updateReqVO) {
        // 校验存在
        validateHomeEducationExists(updateReqVO.getId());
        // 更新
        HomeEducationDO updateObj = HomeEducationConvert.INSTANCE.convert(updateReqVO);
        homeEducationMapper.updateById(updateObj);
    }

    @Override
    public void deleteHomeEducation(Long id) {
        // 校验存在
        validateHomeEducationExists(id);
        // 删除
        homeEducationMapper.deleteById(id);
    }

    private void validateHomeEducationExists(Long id) {
        if (homeEducationMapper.selectById(id) == null) {
            throw exception(HOME_EDUCATION_NOT_EXISTS);
        }
    }

    @Override
    public HomeEducationDO getHomeEducation(Long id) {
        return homeEducationMapper.selectById(id);
    }

    @Override
    public List<HomeEducationDO> getHomeEducationList(Collection<Long> ids) {
        return homeEducationMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<HomeEducationDO> getHomeEducationPage(HomeEducationPageReqVO pageReqVO) {
        return homeEducationMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HomeEducationDO> getHomeEducationList(HomeEducationExportReqVO exportReqVO) {
        return homeEducationMapper.selectList(exportReqVO);
    }

}
