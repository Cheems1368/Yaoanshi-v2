package com.sqlapk.medicine.module.patient.service.questions;

import java.util.*;
import javax.validation.*;
import com.sqlapk.medicine.module.patient.controller.admin.questions.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.questions.QuestionsDO;
import com.sqlapk.medicine.framework.common.pojo.PageResult;

/**
 * 问卷问题 Service 接口
 *
 * @author 管理员
 */
public interface QuestionsService {

    /**
     * 创建问卷问题
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQuestions(@Valid QuestionsCreateReqVO createReqVO);

    /**
     * 更新问卷问题
     *
     * @param updateReqVO 更新信息
     */
    void updateQuestions(@Valid QuestionsUpdateReqVO updateReqVO);

    /**
     * 删除问卷问题
     *
     * @param id 编号
     */
    void deleteQuestions(Long id);

    /**
     * 获得问卷问题
     *
     * @param id 编号
     * @return 问卷问题
     */
    QuestionsDO getQuestions(Long id);

    /**
     * 获得问卷问题列表
     *
     * @param ids 编号
     * @return 问卷问题列表
     */
    List<QuestionsDO> getQuestionsList(Collection<Long> ids);

    /**
     * 获得问卷问题分页
     *
     * @param pageReqVO 分页查询
     * @return 问卷问题分页
     */
    PageResult<QuestionsDO> getQuestionsPage(QuestionsPageReqVO pageReqVO);

    /**
     * 获得问卷问题列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 问卷问题列表
     */
    List<QuestionsDO> getQuestionsList(QuestionsExportReqVO exportReqVO);

}
