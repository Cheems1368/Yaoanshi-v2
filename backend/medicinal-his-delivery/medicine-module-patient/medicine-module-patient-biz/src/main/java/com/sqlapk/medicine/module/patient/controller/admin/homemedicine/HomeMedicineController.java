package com.sqlapk.medicine.module.patient.controller.admin.homemedicine;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicinePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.homemedicine.HomeMedicineConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.homemedicine.HomeMedicineDO;
import com.sqlapk.medicine.module.patient.service.homemedicine.HomeMedicineService;
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

@Tag(name = "管理后台 - 用药清单")
@RestController
@RequestMapping("/patient/home-medicine")
@Validated
public class HomeMedicineController {

    @Resource
    private HomeMedicineService homeMedicineService;

    @PostMapping("/create")
    @Operation(summary = "创建用药清单")
    @PreAuthorize("@ss.hasPermission('patient:home-medicine:create')")
    public CommonResult<Long> createHomeMedicine(@Valid @RequestBody HomeMedicineCreateReqVO createReqVO) {
        return success(homeMedicineService.createHomeMedicine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用药清单")
    @PreAuthorize("@ss.hasPermission('patient:home-medicine:update')")
    public CommonResult<Boolean> updateHomeMedicine(@Valid @RequestBody HomeMedicineUpdateReqVO updateReqVO) {
        homeMedicineService.updateHomeMedicine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用药清单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:home-medicine:delete')")
    public CommonResult<Boolean> deleteHomeMedicine(@RequestParam("id") Long id) {
        homeMedicineService.deleteHomeMedicine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用药清单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:home-medicine:query')")
    public CommonResult<HomeMedicineRespVO> getHomeMedicine(@RequestParam("id") Long id) {
        HomeMedicineDO homeMedicine = homeMedicineService.getHomeMedicine(id);
        return success(HomeMedicineConvert.INSTANCE.convert(homeMedicine));
    }

    @GetMapping("/list")
    @Operation(summary = "获得用药清单列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:home-medicine:query')")
    public CommonResult<List<HomeMedicineRespVO>> getHomeMedicineList(@RequestParam("ids") Collection<Long> ids) {
        List<HomeMedicineDO> list = homeMedicineService.getHomeMedicineList(ids);
        return success(HomeMedicineConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用药清单分页")
    @PreAuthorize("@ss.hasPermission('patient:home-medicine:query')")
    public CommonResult<PageResult<HomeMedicineRespVO>> getHomeMedicinePage(@Valid HomeMedicinePageReqVO pageVO) {
        PageResult<HomeMedicineDO> pageResult = homeMedicineService.getHomeMedicinePage(pageVO);
        return success(HomeMedicineConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用药清单 Excel")
    @PreAuthorize("@ss.hasPermission('patient:home-medicine:export')")
    @OperateLog(type = EXPORT)
    public void exportHomeMedicineExcel(@Valid HomeMedicineExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<HomeMedicineDO> list = homeMedicineService.getHomeMedicineList(exportReqVO);
        // 导出 Excel
        List<HomeMedicineExcelVO> datas = HomeMedicineConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "用药清单.xls", "数据", HomeMedicineExcelVO.class, datas);
    }

}
