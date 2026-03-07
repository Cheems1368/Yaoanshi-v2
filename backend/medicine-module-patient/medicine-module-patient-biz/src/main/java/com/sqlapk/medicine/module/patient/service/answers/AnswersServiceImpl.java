package com.sqlapk.medicine.module.patient.service.answers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.AnswersCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.AnswersExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.AnswersPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.AnswersUpdateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsExportReqVO;
import com.sqlapk.medicine.module.patient.convert.answers.AnswersConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.answers.AnswersDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.answers.AnswersMapper;
import com.sqlapk.medicine.module.patient.enums.QuestionnaireId;
import com.sqlapk.medicine.module.patient.service.patient.PatientsService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.ANSWERS_NOT_EXISTS;

/**
 * 问卷 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class AnswersServiceImpl implements AnswersService {

    @Resource
    private AnswersMapper answersMapper;
    @Resource
    private PatientsService patientsService;

    @Override
    public Long createAnswers(AnswersCreateReqVO createReqVO) {
        // 插入
        AnswersDO answers = AnswersConvert.INSTANCE.convert(createReqVO);
        PatientsExportReqVO patientsExportReqVO = new PatientsExportReqVO();
        patientsExportReqVO.setUserId(createReqVO.getUserId());
        List<PatientsDO> patientsDOS = patientsService.getsList(patientsExportReqVO);
        PatientsDO patientsDO = patientsDOS.get(0);
        answers.setPatientId(patientsDO.getId());
        calculateScore(answers);
        answersMapper.insert(answers);
        // 返回
        return answers.getId();
    }

    private void calculateScore(AnswersDO answers) {
        if (answers.getQuestionId() == QuestionnaireId.PATIENT_INFO.getValue()) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode content;
        try {
            content = mapper.readTree(answers.getContent());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON content", e);
        }
        if (answers.getQuestionId() == QuestionnaireId.CHRONIC_DISEASE.getValue()) {
            Map<String, String> rightValue = new HashMap<>();
            rightValue.put("1", "B");
            rightValue.put("2", "F");
            rightValue.put("3", "A");
            rightValue.put("4", "D");
            rightValue.put("5", "A");
            rightValue.put("6", "D");
            rightValue.put("7", "D");
            rightValue.put("8", "C");
            rightValue.put("9", "A");
            rightValue.put("10", "A");
            AtomicReference<Double> score = new AtomicReference<>(0.0);
            content.fieldNames().forEachRemaining(item -> {
                if (content.get(item).asText().equals(rightValue.get(item))) {
                    score.set(score.get() + 10);
                }
            });
            answers.setValue(score.get());
            return;
        }

        if (answers.getQuestionId() == QuestionnaireId.SATISFACTION.getValue()) {
            AtomicReference<Double> score = new AtomicReference<>(0.0);
            content.fieldNames().forEachRemaining(item -> {
               score.set(score.get() + content.get(item).asDouble());
            });
            answers.setValue(score.get());
            return;
        }

        if (answers.getQuestionId() == QuestionnaireId.COMPLIANCE.getValue()) {
            Map<String, Boolean> rightValue = new HashMap<>();
            rightValue.put("1",false);
            rightValue.put("2",false);
            rightValue.put("3",false);
            rightValue.put("4",false);
            rightValue.put("5",true);
            rightValue.put("6",false);
            rightValue.put("7",false);
            AtomicReference<Double> score = new AtomicReference<>(0.0);
            content.fieldNames().forEachRemaining(item -> {
                JsonNode node = content.get(item);
                if (node.isBoolean()) {
                    if (node.asBoolean() == rightValue.get(item)) {
                        score.set(score.get() + 1);
                    }
                }

                if (node.isTextual()) {
                    score.set(score.get() + node.asDouble());
                }
            });
            answers.setValue(score.get());
        }
    }

    @Override
    public void updateAnswers(AnswersUpdateReqVO updateReqVO) {
        // 校验存在
        validateAnswersExists(updateReqVO.getId());
        // 更新
        AnswersDO updateObj = AnswersConvert.INSTANCE.convert(updateReqVO);
        answersMapper.updateById(updateObj);
    }

    @Override
    public void deleteAnswers(Long id) {
        // 校验存在
        validateAnswersExists(id);
        // 删除
        answersMapper.deleteById(id);
    }

    private void validateAnswersExists(Long id) {
        if (answersMapper.selectById(id) == null) {
            throw exception(ANSWERS_NOT_EXISTS);
        }
    }

    @Override
    public AnswersDO getAnswers(Long id) {
        return answersMapper.selectById(id);
    }

    @Override
    public List<AnswersDO> getAnswersList(Collection<Long> ids) {
        return answersMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<AnswersDO> getAnswersPage(AnswersPageReqVO pageReqVO) {
        return answersMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AnswersDO> getAnswersList(AnswersExportReqVO exportReqVO) {
        return answersMapper.selectList(exportReqVO);
    }

}
