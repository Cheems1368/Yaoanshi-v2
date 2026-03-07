package com.sqlapk.medicine.module.patient.service.questions;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import com.sqlapk.medicine.module.patient.controller.admin.questions.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.questions.QuestionsDO;
import com.sqlapk.medicine.framework.common.pojo.PageResult;

import com.sqlapk.medicine.module.patient.convert.questions.QuestionsConvert;
import com.sqlapk.medicine.module.patient.dal.mysql.questions.QuestionsMapper;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.*;

/**
 * 问卷问题 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class QuestionsServiceImpl implements QuestionsService {

    @Resource
    private QuestionsMapper questionsMapper;

    @Override
    public Long createQuestions(QuestionsCreateReqVO createReqVO) {
        // 插入
        QuestionsDO questions = QuestionsConvert.INSTANCE.convert(createReqVO);
        questionsMapper.insert(questions);
        // 返回
        return questions.getId();
    }

    @Override
    public void updateQuestions(QuestionsUpdateReqVO updateReqVO) {
        // 校验存在
        validateQuestionsExists(updateReqVO.getId());
        // 更新
        QuestionsDO updateObj = QuestionsConvert.INSTANCE.convert(updateReqVO);
        questionsMapper.updateById(updateObj);
    }

    @Override
    public void deleteQuestions(Long id) {
        // 校验存在
        validateQuestionsExists(id);
        // 删除
        questionsMapper.deleteById(id);
    }

    private void validateQuestionsExists(Long id) {
        if (questionsMapper.selectById(id) == null) {
            throw exception(QUESTIONS_NOT_EXISTS);
        }
    }

    @Override
    public QuestionsDO getQuestions(Long id) {
        return questionsMapper.selectById(id);
    }

    @Override
    public List<QuestionsDO> getQuestionsList(Collection<Long> ids) {
        return questionsMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<QuestionsDO> getQuestionsPage(QuestionsPageReqVO pageReqVO) {
        return questionsMapper.selectPage(pageReqVO);
    }

    @Override
    public List<QuestionsDO> getQuestionsList(QuestionsExportReqVO exportReqVO) {
        return questionsMapper.selectList(exportReqVO);
    }

}
