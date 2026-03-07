package com.sqlapk.medicine.module.system.api.helporder;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderCreateReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderPageReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderRespDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderUpdateReqDTO;
import com.sqlapk.medicine.module.system.service.helporder.HelpOrderService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 工单 API 实现类
 *
 * @author sqlapk
 */
@Service
@Validated
public class HelpOrderApiImpl implements HelpOrderApi {

    @Resource
    private HelpOrderService helpOrderService;

    @Override
    public Long createHelpOrder(@Valid HelpOrderCreateReqDTO createReqDTO) {
        return helpOrderService.createHelpOrder(createReqDTO);
    }

    @Override
    public void updateHelpOrder(@Valid HelpOrderUpdateReqDTO updateReqDTO) {
        helpOrderService.updateHelpOrder(updateReqDTO);
    }

    @Override
    public void deleteHelpOrder(Long id) {
        helpOrderService.deleteHelpOrder(id);
    }

    @Override
    public HelpOrderRespDTO getHelpOrder(Long id) {
        return helpOrderService.getHelpOrder(id);
    }

    @Override
    public List<HelpOrderRespDTO> getHelpOrderList(List<Long> ids) {
        return helpOrderService.getHelpOrderList(ids);
    }

    @Override
    public PageResult<HelpOrderRespDTO> getHelpOrderPage(HelpOrderPageReqDTO pageReqDTO) {
        return helpOrderService.getHelpOrderPage(pageReqDTO);
    }

    @Override
    public HelpOrderRespDTO assignToPharmacist(Long id, Long pharmacistId) {
        return helpOrderService.assignToPharmacist(id, pharmacistId);
    }

    @Override
    public HelpOrderRespDTO escalateToDoctor(Long id, Long doctorId) {
        return helpOrderService.escalateToDoctor(id, true, doctorId);
    }

    @Override
    public HelpOrderRespDTO rejectHelpOrder(Long id, String reason) {
        return helpOrderService.rejectHelpOrder(id, reason);
    }
}
