package com.sqlapk.medicine.module.patient.service.home;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.home.HomeConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.home.HomeDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeconsultation.HomeConsultationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeeducation.HomeEducationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeevaluation.HomeEvaluationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homemedicine.HomeMedicineDO;
import com.sqlapk.medicine.module.patient.dal.mysql.home.HomeMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.homeconsultation.HomeConsultationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.homeeducation.HomeEducationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.homeevaluation.HomeEvaluationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.homemedicine.HomeMedicineMapper;
import com.sqlapk.medicine.module.patient.service.homeconsultation.HomeConsultationService;
import com.sqlapk.medicine.module.patient.service.homeeducation.HomeEducationService;
import com.sqlapk.medicine.module.patient.service.homeevaluation.HomeEvaluationService;
import com.sqlapk.medicine.module.patient.service.homemedicine.HomeMedicineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.HOME_NOT_EXISTS;

/**
 * 居家药学服务 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class HomeServiceImpl implements HomeService {

    @Resource
    private HomeMapper homeMapper;
    @Resource
    private HomeConsultationService homeConsultationService;
    @Resource
    private HomeConsultationMapper homeConsultationMapper;
    @Resource
    private HomeEducationService homeEducationService;
    @Resource
    private HomeEducationMapper homeEducationMapper;
    @Resource
    private HomeEvaluationService homeEvaluationService;
    @Resource
    private HomeEvaluationMapper homeEvaluationMapper;
    @Resource
    private HomeMedicineService homeMedicineService;
    @Resource
    private HomeMedicineMapper homeMedicineMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createHome(HomeCreateReqVO createReqVO) {
        // 插入
        HomeDO home = HomeConvert.INSTANCE.convert(createReqVO);
        homeMapper.insert(home);

        createReqVO.getConsultation().forEach(item -> {
            item.setHomeId(home.getId());
            homeConsultationService.createHomeConsultation(item);
        });

        createReqVO.getEducation().forEach(item -> {
            item.setHomeId(home.getId());
            homeEducationService.createHomeEducation(item);
        });

        createReqVO.getEvaluation().forEach(item -> {
            item.setHomeId(home.getId());
            homeEvaluationService.createHomeEvaluation(item);
        });

        createReqVO.getMedicine().forEach(item -> {
            item.setHomeId(home.getId());
            homeMedicineService.createHomeMedicine(item);
        });
        // 返回
        return home.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateHome(HomeUpdateReqVO updateReqVO) {
        // 校验存在
        validateHomeExists(updateReqVO.getId());
        //delete before save
        LambdaQueryWrapperX<HomeConsultationDO> consultationQuery = new LambdaQueryWrapperX<HomeConsultationDO>().eq(HomeConsultationDO::getHomeId, updateReqVO.getId());
        homeConsultationMapper.delete(consultationQuery);
        LambdaQueryWrapperX<HomeEducationDO> educationQuery = new LambdaQueryWrapperX<HomeEducationDO>().eq(HomeEducationDO::getHomeId, updateReqVO.getId());
        homeEducationMapper.delete(educationQuery);
        LambdaQueryWrapperX<HomeEvaluationDO> evaluationQuery = new LambdaQueryWrapperX<HomeEvaluationDO>().eq(HomeEvaluationDO::getHomeId, updateReqVO.getId());
        homeEvaluationMapper.delete(evaluationQuery);
        LambdaQueryWrapperX<HomeMedicineDO> medicineQuery = new LambdaQueryWrapperX<HomeMedicineDO>().eq(HomeMedicineDO::getHomeId, updateReqVO.getId());
        homeMedicineMapper.delete(medicineQuery);
        // 更新
        HomeDO updateObj = HomeConvert.INSTANCE.convert(updateReqVO);
        homeMapper.updateById(updateObj);

        updateReqVO.getConsultation().forEach(item -> {
            item.setHomeId(updateReqVO.getId());
            homeConsultationService.createHomeConsultation(item);
        });

        updateReqVO.getEducation().forEach(item -> {
            item.setHomeId(updateReqVO.getId());
            homeEducationService.createHomeEducation(item);
        });

        updateReqVO.getEvaluation().forEach(item -> {
            item.setHomeId(updateReqVO.getId());
            homeEvaluationService.createHomeEvaluation(item);
        });

        updateReqVO.getMedicine().forEach(item -> {
            item.setHomeId(updateReqVO.getId());
            homeMedicineService.createHomeMedicine(item);
        });
    }

    @Override
    public void deleteHome(Long id) {
        // 校验存在
        validateHomeExists(id);
        // 删除
        homeMapper.deleteById(id);
    }

    private void validateHomeExists(Long id) {
        if (homeMapper.selectById(id) == null) {
            throw exception(HOME_NOT_EXISTS);
        }
    }

    @Override
    public HomeDO getHome(Long id) {
        return homeMapper.selectById(id);
    }

    @Override
    public List<HomeDO> getHomeList(Collection<Long> ids) {
        return homeMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<HomeDO> getHomePage(HomePageReqVO pageReqVO) {
        return homeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HomeDO> getHomeList(HomeExportReqVO exportReqVO) {
        return homeMapper.selectList(exportReqVO);
    }

}
