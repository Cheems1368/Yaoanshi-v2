package com.sqlapk.medicine.module.patient.service.inpatientround;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientround.InpatientRoundDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 住院药学查房记录 Service 接口
 *
 * @author 管理员
 */
public interface InpatientRoundService {

    /**
     * 创建住院药学查房记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInpatientRound(@Valid InpatientRoundCreateReqVO createReqVO);

    /**
     * 更新住院药学查房记录
     *
     * @param updateReqVO 更新信息
     */
    void updateInpatientRound(@Valid InpatientRoundUpdateReqVO updateReqVO);

    /**
     * 删除住院药学查房记录
     *
     * @param id 编号
     */
    void deleteInpatientRound(Long id);

    /**
     * 获得住院药学查房记录
     *
     * @param id 编号
     * @return 住院药学查房记录
     */
    InpatientRoundDO getInpatientRound(Long id);

    /**
     * 获得住院药学查房记录列表
     *
     * @param ids 编号
     * @return 住院药学查房记录列表
     */
    List<InpatientRoundDO> getInpatientRoundList(Collection<Long> ids);

    /**
     * 获得住院药学查房记录分页
     *
     * @param pageReqVO 分页查询
     * @return 住院药学查房记录分页
     */
    PageResult<InpatientRoundDO> getInpatientRoundPage(InpatientRoundPageReqVO pageReqVO);

    /**
     * 获得住院药学查房记录列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 住院药学查房记录列表
     */
    List<InpatientRoundDO> getInpatientRoundList(InpatientRoundExportReqVO exportReqVO);

}
