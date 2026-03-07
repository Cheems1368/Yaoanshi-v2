package com.sqlapk.medicine.module.system.service.helporder;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderCreateReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderPageReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderRespDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderUpdateReqDTO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderExportReqVO;
import com.sqlapk.medicine.module.system.dal.dataobject.helporder.HelpOrderDO;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;

import javax.validation.Valid;
import java.util.List;

/**
 * 工单 Service 接口
 *
 * @author sqlapk
 */
public interface HelpOrderService {

    /**
     * 创建工单
     *
     * @param createReqDTO 创建信息
     * @return 工单编号
     */
    Long createHelpOrder(@Valid HelpOrderCreateReqDTO createReqDTO);

    /**
     * 更新工单
     *
     * @param updateReqDTO 更新信息
     */
    void updateHelpOrder(@Valid HelpOrderUpdateReqDTO updateReqDTO);

    /**
     * 删除工单
     *
     * @param id 工单编号
     */
    void deleteHelpOrder(Long id);

    /**
     * 获得工单
     *
     * @param id 工单编号
     * @return 工单
     */
    HelpOrderRespDTO getHelpOrder(Long id);

    /**
     * 获得工单列表
     *
     * @param ids 工单编号数组
     * @return 工单列表
     */
    List<HelpOrderRespDTO> getHelpOrderList(List<Long> ids);

    /**
     * 获得工单分页
     *
     * @param pageReqDTO 分页查询
     * @return 工单分页
     */
    PageResult<HelpOrderRespDTO> getHelpOrderPage(HelpOrderPageReqDTO pageReqDTO);

    /**
     * 分配药师
     *
     * @param id 工单编号
     * @param pharmacistId 药师编号
     * @return 工单详情
     */
    HelpOrderRespDTO assignToPharmacist(Long id, Long pharmacistId);

    /**
     * 升级医生
     *
     * @param id 工单编号
     * @param needDoctor 是否需要医生
     * @param doctorId 医生编号
     * @return 工单详情
     */
    HelpOrderRespDTO escalateToDoctor(Long id, Boolean needDoctor, Long doctorId);

    /**
     * 驳回工单
     *
     * @param id 工单编号
     * @param reason 驳回原因
     * @return 工单详情
     */
    HelpOrderRespDTO rejectHelpOrder(Long id, String reason);

    /**
     * 更新工单状态
     *
     * @param id 工单编号
     * @param status 新状态
     * @param reason 状态变更原因
     * @return 工单详情
     */
    HelpOrderRespDTO updateHelpOrderStatus(Long id, HelpOrderStatus status, String reason);

    /**
     * 更新工单状态（支持字符串状态）
     *
     * @param id 工单编号
     * @param statusStr 新状态字符串
     * @param reason 状态变更原因
     * @return 工单详情
     */
    HelpOrderRespDTO updateHelpOrderStatus(Long id, String statusStr, String reason);

    /**
     * 获取导出工单列表
     *
     * @param exportReqVO 导出查询条件
     * @return 工单列表
     */
    List<HelpOrderDO> getHelpOrderListForExport(HelpOrderExportReqVO exportReqVO);
}

