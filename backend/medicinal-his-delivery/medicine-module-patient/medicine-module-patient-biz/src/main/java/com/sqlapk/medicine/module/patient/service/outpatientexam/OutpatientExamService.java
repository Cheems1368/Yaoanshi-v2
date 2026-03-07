package com.sqlapk.medicine.module.patient.service.outpatientexam;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientexam.OutpatientExamDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 门诊药学检查项目明细 Service 接口
 *
 * @author 管理员
 */
public interface OutpatientExamService {

    /**
     * 创建门诊药学检查项目明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutpatientExam(@Valid OutpatientExamCreateReqVO createReqVO);

    /**
     * 更新门诊药学检查项目明细
     *
     * @param updateReqVO 更新信息
     */
    void updateOutpatientExam(@Valid OutpatientExamUpdateReqVO updateReqVO);

    /**
     * 删除门诊药学检查项目明细
     *
     * @param id 编号
     */
    void deleteOutpatientExam(Long id);

    /**
     * 获得门诊药学检查项目明细
     *
     * @param id 编号
     * @return 门诊药学检查项目明细
     */
    OutpatientExamDO getOutpatientExam(Long id);

    /**
     * 获得门诊药学检查项目明细列表
     *
     * @param ids 编号
     * @return 门诊药学检查项目明细列表
     */
    List<OutpatientExamDO> getOutpatientExamList(Collection<Long> ids);

    /**
     * 获得门诊药学检查项目明细分页
     *
     * @param pageReqVO 分页查询
     * @return 门诊药学检查项目明细分页
     */
    PageResult<OutpatientExamDO> getOutpatientExamPage(OutpatientExamPageReqVO pageReqVO);

    /**
     * 获得门诊药学检查项目明细列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 门诊药学检查项目明细列表
     */
    List<OutpatientExamDO> getOutpatientExamList(OutpatientExamExportReqVO exportReqVO);

}
