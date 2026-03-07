package com.sqlapk.medicine.module.system.controller.admin.helporder;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderCreateReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderPageReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderRespDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderUpdateReqDTO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderCreateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderExcelVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderExportReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderPageReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderRespVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderUpdateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderAssignReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderEscalateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderRejectReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderStatusUpdateReqVO;
import com.sqlapk.medicine.module.system.convert.helporder.HelpOrderConvert;
import com.sqlapk.medicine.module.system.dal.dataobject.helporder.HelpOrderDO;
import com.sqlapk.medicine.module.system.service.helporder.HelpOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;
import static com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 工单")
@RestController
@RequestMapping("/admin-api/help-orders")
@Validated
public class HelpOrderController {

    @Resource
    private HelpOrderService helpOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建工单")
    @PreAuthorize("@ss.hasPermission('system:help-order:create')")
    public CommonResult<Long> createHelpOrder(@Valid @RequestBody HelpOrderCreateReqVO createReqVO) {
        HelpOrderCreateReqDTO createReqDTO = HelpOrderConvert.INSTANCE.convert(createReqVO);
        return success(helpOrderService.createHelpOrder(createReqDTO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工单")
    @PreAuthorize("@ss.hasPermission('system:help-order:update')")
    public CommonResult<Boolean> updateHelpOrder(@Valid @RequestBody HelpOrderUpdateReqVO updateReqVO) {
        HelpOrderUpdateReqDTO updateReqDTO = HelpOrderConvert.INSTANCE.convert(updateReqVO);
        helpOrderService.updateHelpOrder(updateReqDTO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:help-order:delete')")
    public CommonResult<Boolean> deleteHelpOrder(@RequestParam("id") Long id) {
        helpOrderService.deleteHelpOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:help-order:query')")
    public CommonResult<HelpOrderRespVO> getHelpOrder(@RequestParam("id") Long id) {
        HelpOrderRespDTO helpOrder = helpOrderService.getHelpOrder(id);
        return success(HelpOrderConvert.INSTANCE.convert(helpOrder));
    }

    @GetMapping("/list")
    @Operation(summary = "获得工单列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('system:help-order:query')")
    public CommonResult<List<HelpOrderRespVO>> getHelpOrderList(@RequestParam("ids") List<Long> ids) {
        List<HelpOrderRespDTO> list = helpOrderService.getHelpOrderList(ids);
        return success(HelpOrderConvert.INSTANCE.convertRespList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工单分页")
    @PreAuthorize("@ss.hasPermission('system:help-order:query')")
    public CommonResult<PageResult<HelpOrderRespVO>> getHelpOrderPage(@Valid HelpOrderPageReqVO pageVO) {
        HelpOrderPageReqDTO pageReqDTO = HelpOrderConvert.INSTANCE.convert(pageVO);
        PageResult<HelpOrderRespDTO> pageResult = helpOrderService.getHelpOrderPage(pageReqDTO);
        return success(HelpOrderConvert.INSTANCE.convertRespPage(pageResult));
    }

    @PostMapping("/assign")
    @Operation(summary = "分配药师")
    @PreAuthorize("@ss.hasPermission('system:help-order:assign')")
    public CommonResult<HelpOrderRespVO> assignToPharmacist(@Valid @RequestBody HelpOrderAssignReqVO assignReqVO) {
        HelpOrderRespDTO helpOrder = helpOrderService.assignToPharmacist(assignReqVO.getId(), assignReqVO.getPharmacistId());
        return success(HelpOrderConvert.INSTANCE.convert(helpOrder));
    }

    @PostMapping("/escalate")
    @Operation(summary = "升级医生")
    @PreAuthorize("@ss.hasPermission('system:help-order:escalate')")
    public CommonResult<HelpOrderRespVO> escalateToDoctor(@Valid @RequestBody HelpOrderEscalateReqVO escalateReqVO) {
        HelpOrderRespDTO helpOrder = helpOrderService.escalateToDoctor(
            escalateReqVO.getId(), 
            escalateReqVO.getNeedDoctor(), 
            escalateReqVO.getDoctorId()
        );
        return success(HelpOrderConvert.INSTANCE.convert(helpOrder));
    }

    @PostMapping("/reject")
    @Operation(summary = "驳回工单")
    @PreAuthorize("@ss.hasPermission('system:help-order:reject')")
    public CommonResult<HelpOrderRespVO> rejectHelpOrder(@Valid @RequestBody HelpOrderRejectReqVO rejectReqVO) {
        HelpOrderRespDTO helpOrder = helpOrderService.rejectHelpOrder(rejectReqVO.getId(), rejectReqVO.getReason());
        return success(HelpOrderConvert.INSTANCE.convert(helpOrder));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新工单状态")
    @PreAuthorize("@ss.hasPermission('system:help-order:update')")
    public CommonResult<HelpOrderRespVO> updateHelpOrderStatus(@Valid @RequestBody HelpOrderStatusUpdateReqVO statusUpdateReqVO) {
        HelpOrderRespDTO helpOrder = helpOrderService.updateHelpOrderStatus(
            statusUpdateReqVO.getId(), 
            statusUpdateReqVO.getStatus(), 
            statusUpdateReqVO.getReason()
        );
        return success(HelpOrderConvert.INSTANCE.convert(helpOrder));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工单 Excel")
    @PreAuthorize("@ss.hasPermission('system:help-order:export')")
    @OperateLog(type = EXPORT)
    public void exportHelpOrderExcel(@Valid HelpOrderExportReqVO exportReqVO,
                                   HttpServletResponse response) throws IOException {
        // 1. 获取导出数据
        List<HelpOrderDO> list = helpOrderService.getHelpOrderListForExport(exportReqVO);
        
        // 2. 转换为Excel VO
        List<HelpOrderExcelVO> excelList = HelpOrderConvert.INSTANCE.convertToExcelList(list);
        
        // 3. 生成Excel文件
        String fileName = String.format("工单导出_%s.xlsx", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        
        ExcelUtils.write(response, fileName, "工单数据", HelpOrderExcelVO.class, excelList);
    }

}
