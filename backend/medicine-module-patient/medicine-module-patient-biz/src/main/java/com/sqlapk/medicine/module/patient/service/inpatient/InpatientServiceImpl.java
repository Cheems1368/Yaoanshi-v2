package com.sqlapk.medicine.module.patient.service.inpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatient.InpatientConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatient.InpatientDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientadvice.InpatientAdviceDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientconsultation.InpatientConsultationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatienteducation.InpatientEducationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientevaluation.InpatientEvaluationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientround.InpatientRoundDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatient.InpatientMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientadvice.InpatientAdviceMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientconsultation.InpatientConsultationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatienteducation.InpatientEducationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientevaluation.InpatientEvaluationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientround.InpatientRoundMapper;
import com.sqlapk.medicine.module.patient.service.inpatientadvice.InpatientAdviceService;
import com.sqlapk.medicine.module.patient.service.inpatientconsultation.InpatientConsultationService;
import com.sqlapk.medicine.module.patient.service.inpatienteducation.InpatientEducationService;
import com.sqlapk.medicine.module.patient.service.inpatientevaluation.InpatientEvaluationService;
import com.sqlapk.medicine.module.patient.service.inpatientround.InpatientRoundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_NOT_EXISTS;

/**
 * 住院药学服务 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class InpatientServiceImpl implements InpatientService {

    @Resource
    private InpatientMapper inpatientMapper;
    @Resource
    private InpatientConsultationService consultationService;
    @Resource
    private InpatientConsultationMapper inpatientConsultationMapper;
    @Resource
    private InpatientEducationService educationService;
    @Resource
    private InpatientEducationMapper inpatientEducationMapper;
    @Resource
    private InpatientEvaluationService evaluationService;
    @Resource
    private InpatientEvaluationMapper inpatientEvaluationMapper;
    @Resource
    private InpatientRoundService inpatientRoundService;
    @Resource
    private InpatientRoundMapper inpatientRoundMapper;
    @Resource
    private InpatientAdviceService inpatientAdviceService;
    @Resource
    private InpatientAdviceMapper inpatientAdviceMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInpatient(InpatientCreateReqVO createReqVO) {
        // 插入
        InpatientDO inpatient = InpatientConvert.INSTANCE.convert(createReqVO);
        inpatientMapper.insert(inpatient);

        createReqVO.getAdvice().forEach(item -> {
            item.setInpatientId(inpatient.getId());
            inpatientAdviceService.createInpatientAdvice(item);
        });

        createReqVO.getEducation().forEach(item -> {
            item.setInpatientId(inpatient.getId());
            educationService.createInpatientEducation(item);
        });

        createReqVO.getEvaluation().forEach(item -> {
            item.setInpatientId(inpatient.getId());
            evaluationService.createInpatientEvaluation(item);
        });

        createReqVO.getRound().forEach(item -> {
            item.setInpatientId(inpatient.getId());
            inpatientRoundService.createInpatientRound(item);
        });

        createReqVO.getConsultation().forEach(item -> {
            item.setInpatientId(inpatient.getId());
            consultationService.createInpatientConsultation(item);
        });
        // 返回
        return inpatient.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInpatient(InpatientUpdateReqVO updateReqVO) {
        // 校验存在
        validateInpatientExists(updateReqVO.getId());
        // 更新
        InpatientDO updateObj = InpatientConvert.INSTANCE.convert(updateReqVO);
        inpatientMapper.updateById(updateObj);
        //delete before
        //advice
        LambdaQueryWrapperX<InpatientAdviceDO> adviceQuery = new LambdaQueryWrapperX<InpatientAdviceDO>().eq(InpatientAdviceDO::getInpatientId, updateReqVO.getId());
        inpatientAdviceMapper.delete(adviceQuery);
        //education
        LambdaQueryWrapperX<InpatientEducationDO> educationQuery = new LambdaQueryWrapperX<InpatientEducationDO>().eq(InpatientEducationDO::getInpatientId, updateReqVO.getId());
        inpatientEducationMapper.delete(educationQuery);
        //evaluation
        LambdaQueryWrapperX<InpatientEvaluationDO> evaluationQuery = new LambdaQueryWrapperX<InpatientEvaluationDO>().eq(InpatientEvaluationDO::getInpatientId, updateReqVO.getId());
        inpatientEvaluationMapper.delete(evaluationQuery);
        //consultation
        LambdaQueryWrapperX<InpatientConsultationDO> consultationQuery = new LambdaQueryWrapperX<InpatientConsultationDO>().eq(InpatientConsultationDO::getInpatientId, updateReqVO.getId());
        inpatientConsultationMapper.delete(consultationQuery);
        //round
        LambdaQueryWrapperX<InpatientRoundDO> roundQuery = new LambdaQueryWrapperX<InpatientRoundDO>().eq(InpatientRoundDO::getInpatientId, updateReqVO.getId());
        inpatientRoundMapper.delete(roundQuery);

        updateReqVO.getAdvice().forEach(item -> {
            item.setInpatientId(updateReqVO.getId());
            inpatientAdviceService.createInpatientAdvice(item);
        });

        updateReqVO.getEducation().forEach(item -> {
            item.setInpatientId(updateReqVO.getId());
            educationService.createInpatientEducation(item);
        });

        updateReqVO.getEvaluation().forEach(item -> {
            item.setInpatientId(updateReqVO.getId());
            evaluationService.createInpatientEvaluation(item);
        });

        updateReqVO.getRound().forEach(item -> {
            item.setInpatientId(updateReqVO.getId());
            inpatientRoundService.createInpatientRound(item);
        });

        updateReqVO.getConsultation().forEach(item -> {
            item.setInpatientId(updateReqVO.getId());
            consultationService.createInpatientConsultation(item);
        });
    }

    @Override
    public void deleteInpatient(Long id) {
        // 校验存在
        validateInpatientExists(id);
        // 删除
        inpatientMapper.deleteById(id);
    }

    private void validateInpatientExists(Long id) {
        if (inpatientMapper.selectById(id) == null) {
            throw exception(INPATIENT_NOT_EXISTS);
        }
    }

    @Override
    public InpatientDO getInpatient(Long id) {
        return inpatientMapper.selectById(id);
    }

    @Override
    public List<InpatientDO> getInpatientList(Collection<Long> ids) {
        return inpatientMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<InpatientDO> getInpatientPage(InpatientPageReqVO pageReqVO) {
        return inpatientMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InpatientDO> getInpatientList(InpatientExportReqVO exportReqVO) {
        return inpatientMapper.selectList(exportReqVO);
    }

}
