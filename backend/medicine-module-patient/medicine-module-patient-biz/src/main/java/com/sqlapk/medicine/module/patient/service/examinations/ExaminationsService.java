package com.sqlapk.medicine.module.patient.service.examinations;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.examinations.ExaminationsDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 检查项目 Service 接口
 *
 * @author 管理员
 */
public interface ExaminationsService {

    /**
     * 创建检查项目
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createExaminations(@Valid ExaminationsCreateReqVO createReqVO);

    /**
     * 更新检查项目
     *
     * @param updateReqVO 更新信息
     */
    void updateExaminations(@Valid ExaminationsUpdateReqVO updateReqVO);

    /**
     * 删除检查项目
     *
     * @param id 编号
     */
    void deleteExaminations(Long id);

    /**
     * 获得检查项目
     *
     * @param id 编号
     * @return 检查项目
     */
    ExaminationsDO getExaminations(Long id);

    /**
     * 获得检查项目列表
     *
     * @param ids 编号
     * @return 检查项目列表
     */
    List<ExaminationsDO> getExaminationsList(Collection<Long> ids);

    /**
     * 获得检查项目分页
     *
     * @param pageReqVO 分页查询
     * @return 检查项目分页
     */
    PageResult<ExaminationsDO> getExaminationsPage(ExaminationsPageReqVO pageReqVO);

    /**
     * 获得检查项目列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 检查项目列表
     */
    List<ExaminationsDO> getExaminationsList(ExaminationsExportReqVO exportReqVO);

}
