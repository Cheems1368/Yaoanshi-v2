package com.sqlapk.medicine.module.patient.service.inpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatient.InpatientDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 住院药学服务 Service 接口
 *
 * @author 管理员
 */
public interface InpatientService {

    /**
     * 创建住院药学服务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInpatient(@Valid InpatientCreateReqVO createReqVO);

    /**
     * 更新住院药学服务
     *
     * @param updateReqVO 更新信息
     */
    void updateInpatient(@Valid InpatientUpdateReqVO updateReqVO);

    /**
     * 删除住院药学服务
     *
     * @param id 编号
     */
    void deleteInpatient(Long id);

    /**
     * 获得住院药学服务
     *
     * @param id 编号
     * @return 住院药学服务
     */
    InpatientDO getInpatient(Long id);

    /**
     * 获得住院药学服务列表
     *
     * @param ids 编号
     * @return 住院药学服务列表
     */
    List<InpatientDO> getInpatientList(Collection<Long> ids);

    /**
     * 获得住院药学服务分页
     *
     * @param pageReqVO 分页查询
     * @return 住院药学服务分页
     */
    PageResult<InpatientDO> getInpatientPage(InpatientPageReqVO pageReqVO);

    /**
     * 获得住院药学服务列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 住院药学服务列表
     */
    List<InpatientDO> getInpatientList(InpatientExportReqVO exportReqVO);

}
