package com.sqlapk.medicine.module.system.controller.admin.helporder;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderCreateReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderPageReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderRespDTO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderCreateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderPageReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderRespVO;
import com.sqlapk.medicine.module.system.convert.helporder.HelpOrderConvert;
import com.sqlapk.medicine.module.system.service.helporder.HelpOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

/**
 * 工单测试控制器 - 用于功能验证，不进行权限验证
 *
 * @author sqlapk
 */
@Tag(name = "管理后台 - 工单测试")
@RestController
@RequestMapping("/app/help-order")
@Validated
public class HelpOrderTestController {

    @Resource
    private HelpOrderService helpOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建工单")
    public CommonResult<Long> createHelpOrder(@Valid @RequestBody HelpOrderCreateReqVO createReqVO) {
        HelpOrderCreateReqDTO createReqDTO = HelpOrderConvert.INSTANCE.convert(createReqVO);
        return success(helpOrderService.createHelpOrder(createReqDTO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得工单")
    @PermitAll
    public CommonResult<HelpOrderRespVO> getHelpOrder(@RequestParam("id") Long id) {
        HelpOrderRespDTO helpOrder = helpOrderService.getHelpOrder(id);
        return success(HelpOrderConvert.INSTANCE.convert(helpOrder));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工单分页")
    @PermitAll
    public CommonResult<PageResult<HelpOrderRespVO>> getHelpOrderPage(@Valid HelpOrderPageReqVO pageVO) {
        HelpOrderPageReqDTO pageReqDTO = HelpOrderConvert.INSTANCE.convert(pageVO);
        PageResult<HelpOrderRespDTO> pageResult = helpOrderService.getHelpOrderPage(pageReqDTO);
        return success(HelpOrderConvert.INSTANCE.convertRespPage(pageResult));
    }

    @PostMapping("/assign")
    @Operation(summary = "分配药师")
    @PermitAll
    public CommonResult<HelpOrderRespVO> assignToPharmacist(@RequestParam("id") Long id, 
                                                           @RequestParam("pharmacistId") Long pharmacistId) {
        HelpOrderRespDTO helpOrder = helpOrderService.assignToPharmacist(id, pharmacistId);
        return success(HelpOrderConvert.INSTANCE.convert(helpOrder));
    }

    @PostMapping("/escalate")
    @Operation(summary = "升级医生")
    @PermitAll
    public CommonResult<HelpOrderRespVO> escalateToDoctor(@RequestParam("id") Long id, 
                                                         @RequestParam("doctorId") Long doctorId) {
        HelpOrderRespDTO helpOrder = helpOrderService.escalateToDoctor(id, doctorId);
        return success(HelpOrderConvert.INSTANCE.convert(helpOrder));
    }

    @PostMapping("/reject")
    @Operation(summary = "驳回工单")
    @PermitAll
    public CommonResult<HelpOrderRespVO> rejectHelpOrder(@RequestParam("id") Long id, 
                                                        @RequestParam("reason") String reason) {
        HelpOrderRespDTO helpOrder = helpOrderService.rejectHelpOrder(id, reason);
        return success(HelpOrderConvert.INSTANCE.convert(helpOrder));
    }
}
