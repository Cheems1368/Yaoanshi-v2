package com.sqlapk.medicine.module.patient.service.surveys;

import java.util.*;
import javax.validation.*;
import com.sqlapk.medicine.module.patient.controller.admin.surveys.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.surveys.SurveysDO;
import com.sqlapk.medicine.framework.common.pojo.PageResult;

/**
 * 问卷 Service 接口
 *
 * @author 管理员
 */
public interface SurveysService {

    /**
     * 创建问卷
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSurveys(@Valid SurveysCreateReqVO createReqVO);

    /**
     * 更新问卷
     *
     * @param updateReqVO 更新信息
     */
    void updateSurveys(@Valid SurveysUpdateReqVO updateReqVO);

    /**
     * 删除问卷
     *
     * @param id 编号
     */
    void deleteSurveys(Long id);

    /**
     * 获得问卷
     *
     * @param id 编号
     * @return 问卷
     */
    SurveysDO getSurveys(Long id);

    /**
     * 获得问卷列表
     *
     * @param ids 编号
     * @return 问卷列表
     */
    List<SurveysDO> getSurveysList(Collection<Long> ids);

    /**
     * 获得问卷分页
     *
     * @param pageReqVO 分页查询
     * @return 问卷分页
     */
    PageResult<SurveysDO> getSurveysPage(SurveysPageReqVO pageReqVO);

    /**
     * 获得问卷列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 问卷列表
     */
    List<SurveysDO> getSurveysList(SurveysExportReqVO exportReqVO);

}
