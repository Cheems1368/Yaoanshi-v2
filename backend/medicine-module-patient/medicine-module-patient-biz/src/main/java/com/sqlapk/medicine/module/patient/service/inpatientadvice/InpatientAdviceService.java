package com.sqlapk.medicine.module.patient.service.inpatientadvice;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdvicePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientadvice.InpatientAdviceDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 住院药学不合理用药 Service 接口
 *
 * @author 管理员
 */
public interface InpatientAdviceService {

    /**
     * 创建住院药学不合理用药
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInpatientAdvice(@Valid InpatientAdviceCreateReqVO createReqVO);

    /**
     * 更新住院药学不合理用药
     *
     * @param updateReqVO 更新信息
     */
    void updateInpatientAdvice(@Valid InpatientAdviceUpdateReqVO updateReqVO);

    /**
     * 删除住院药学不合理用药
     *
     * @param id 编号
     */
    void deleteInpatientAdvice(Long id);

    /**
     * 获得住院药学不合理用药
     *
     * @param id 编号
     * @return 住院药学不合理用药
     */
    InpatientAdviceDO getInpatientAdvice(Long id);

    /**
     * 获得住院药学不合理用药列表
     *
     * @param ids 编号
     * @return 住院药学不合理用药列表
     */
    List<InpatientAdviceDO> getInpatientAdviceList(Collection<Long> ids);

    /**
     * 获得住院药学不合理用药分页
     *
     * @param pageReqVO 分页查询
     * @return 住院药学不合理用药分页
     */
    PageResult<InpatientAdviceDO> getInpatientAdvicePage(InpatientAdvicePageReqVO pageReqVO);

    /**
     * 获得住院药学不合理用药列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 住院药学不合理用药列表
     */
    List<InpatientAdviceDO> getInpatientAdviceList(InpatientAdviceExportReqVO exportReqVO);

}
