package com.sqlapk.medicine.module.patient.service.answers;

import java.util.*;
import javax.validation.*;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.answers.AnswersDO;
import com.sqlapk.medicine.framework.common.pojo.PageResult;

/**
 * 问卷 Service 接口
 *
 * @author 管理员
 */
public interface AnswersService {

    /**
     * 创建问卷
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAnswers(@Valid AnswersCreateReqVO createReqVO);

    /**
     * 更新问卷
     *
     * @param updateReqVO 更新信息
     */
    void updateAnswers(@Valid AnswersUpdateReqVO updateReqVO);

    /**
     * 删除问卷
     *
     * @param id 编号
     */
    void deleteAnswers(Long id);

    /**
     * 获得问卷
     *
     * @param id 编号
     * @return 问卷
     */
    AnswersDO getAnswers(Long id);

    /**
     * 获得问卷列表
     *
     * @param ids 编号
     * @return 问卷列表
     */
    List<AnswersDO> getAnswersList(Collection<Long> ids);

    /**
     * 获得问卷分页
     *
     * @param pageReqVO 分页查询
     * @return 问卷分页
     */
    PageResult<AnswersDO> getAnswersPage(AnswersPageReqVO pageReqVO);

    /**
     * 获得问卷列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 问卷列表
     */
    List<AnswersDO> getAnswersList(AnswersExportReqVO exportReqVO);

}
