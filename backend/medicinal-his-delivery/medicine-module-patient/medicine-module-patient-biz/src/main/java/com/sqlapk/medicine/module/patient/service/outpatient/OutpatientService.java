package com.sqlapk.medicine.module.patient.service.outpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientPatientInfoRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatient.OutpatientDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 门诊药学服务 Service 接口
 *
 * @author 管理员
 */
public interface OutpatientService {

    /**
     * 创建门诊药学服务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutpatient(@Valid OutpatientCreateReqVO createReqVO);

    /**
     * 更新门诊药学服务
     *
     * @param updateReqVO 更新信息
     */
    void updateOutpatient(@Valid OutpatientUpdateReqVO updateReqVO);

    /**
     * 删除门诊药学服务
     *
     * @param id 编号
     */
    void deleteOutpatient(Long id);

    /**
     * 获得门诊药学服务
     *
     * @param id 编号
     * @return 门诊药学服务
     */
    OutpatientDO getOutpatient(Long id);

    /**
     * 获得门诊药学服务列表
     *
     * @param ids 编号
     * @return 门诊药学服务列表
     */
    List<OutpatientDO> getOutpatientList(Collection<Long> ids);

    /**
     * 获得门诊药学服务分页
     *
     * @param pageReqVO 分页查询
     * @return 门诊药学服务分页
     */
    PageResult<OutpatientDO> getOutpatientPage(OutpatientPageReqVO pageReqVO);

    /**
     * 获得门诊药学服务列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 门诊药学服务列表
     */
    List<OutpatientDO> getOutpatientList(OutpatientExportReqVO exportReqVO);

    OutpatientPatientInfoRespVO getPatientInfo(Long patientId);
}
