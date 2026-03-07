package com.sqlapk.medicine.module.patient.service.homeconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.homeconsultation.HomeConsultationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeconsultation.HomeConsultationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.homeconsultation.HomeConsultationMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.HOME_CONSULTATION_NOT_EXISTS;

/**
 * 居家用药咨询 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class HomeConsultationServiceImpl implements HomeConsultationService {

    @Resource
    private HomeConsultationMapper homeConsultationMapper;

    @Override
    public Long createHomeConsultation(HomeConsultationCreateReqVO createReqVO) {
        // 插入
        HomeConsultationDO homeConsultation = HomeConsultationConvert.INSTANCE.convert(createReqVO);
        homeConsultationMapper.insert(homeConsultation);
        // 返回
        return homeConsultation.getId();
    }

    @Override
    public void updateHomeConsultation(HomeConsultationUpdateReqVO updateReqVO) {
        // 校验存在
        validateHomeConsultationExists(updateReqVO.getId());
        // 更新
        HomeConsultationDO updateObj = HomeConsultationConvert.INSTANCE.convert(updateReqVO);
        homeConsultationMapper.updateById(updateObj);
    }

    @Override
    public void deleteHomeConsultation(Long id) {
        // 校验存在
        validateHomeConsultationExists(id);
        // 删除
        homeConsultationMapper.deleteById(id);
    }

    private void validateHomeConsultationExists(Long id) {
        if (homeConsultationMapper.selectById(id) == null) {
            throw exception(HOME_CONSULTATION_NOT_EXISTS);
        }
    }

    @Override
    public HomeConsultationDO getHomeConsultation(Long id) {
        return homeConsultationMapper.selectById(id);
    }

    @Override
    public List<HomeConsultationDO> getHomeConsultationList(Collection<Long> ids) {
        return homeConsultationMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<HomeConsultationDO> getHomeConsultationPage(HomeConsultationPageReqVO pageReqVO) {
        return homeConsultationMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HomeConsultationDO> getHomeConsultationList(HomeConsultationExportReqVO exportReqVO) {
        return homeConsultationMapper.selectList(exportReqVO);
    }

}
