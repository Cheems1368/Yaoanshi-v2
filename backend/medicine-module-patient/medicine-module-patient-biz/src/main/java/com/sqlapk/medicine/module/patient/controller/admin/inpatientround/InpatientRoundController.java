package com.sqlapk.medicine.module.patient.controller.admin.inpatientround;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatientround.InpatientRoundConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientround.InpatientRoundDO;
import com.sqlapk.medicine.module.patient.service.inpatientround.InpatientRoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;
import static com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 住院药学查房记录")
@RestController
@RequestMapping("/patient/inpatient-round")
@Validated
public class InpatientRoundController {

    @Resource
    private InpatientRoundService inpatientRoundService;

    @PostMapping("/create")
    @Operation(summary = "创建住院药学查房记录")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-round:create')")
    public CommonResult<Long> createInpatientRound(@Valid @RequestBody InpatientRoundCreateReqVO createReqVO) {
        return success(inpatientRoundService.createInpatientRound(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新住院药学查房记录")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-round:update')")
    public CommonResult<Boolean> updateInpatientRound(@Valid @RequestBody InpatientRoundUpdateReqVO updateReqVO) {
        inpatientRoundService.updateInpatientRound(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除住院药学查房记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:inpatient-round:delete')")
    public CommonResult<Boolean> deleteInpatientRound(@RequestParam("id") Long id) {
        inpatientRoundService.deleteInpatientRound(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得住院药学查房记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-round:query')")
    public CommonResult<InpatientRoundRespVO> getInpatientRound(@RequestParam("id") Long id) {
        InpatientRoundDO inpatientRound = inpatientRoundService.getInpatientRound(id);
        return success(InpatientRoundConvert.INSTANCE.convert(inpatientRound));
    }

    @GetMapping("/list")
    @Operation(summary = "获得住院药学查房记录列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-round:query')")
    public CommonResult<List<InpatientRoundRespVO>> getInpatientRoundList(@RequestParam("ids") Collection<Long> ids) {
        List<InpatientRoundDO> list = inpatientRoundService.getInpatientRoundList(ids);
        return success(InpatientRoundConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得住院药学查房记录分页")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-round:query')")
    public CommonResult<PageResult<InpatientRoundRespVO>> getInpatientRoundPage(@Valid InpatientRoundPageReqVO pageVO) {
        PageResult<InpatientRoundDO> pageResult = inpatientRoundService.getInpatientRoundPage(pageVO);
        return success(InpatientRoundConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出住院药学查房记录 Excel")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-round:export')")
    @OperateLog(type = EXPORT)
    public void exportInpatientRoundExcel(@Valid InpatientRoundExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<InpatientRoundDO> list = inpatientRoundService.getInpatientRoundList(exportReqVO);
        // 导出 Excel
        List<InpatientRoundExcelVO> datas = InpatientRoundConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "住院药学查房记录.xls", "数据", InpatientRoundExcelVO.class, datas);
    }

}
