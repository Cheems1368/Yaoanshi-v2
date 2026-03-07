package com.sqlapk.medicine.module.drugs.controller.admin.drug;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsCreateReqVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsExcelVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsExportReqVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsPageReqVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsRespVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsUpdateReqVO;
import com.sqlapk.medicine.module.drugs.convert.drug.DrugsConvert;
import com.sqlapk.medicine.module.drugs.dal.dataobject.drug.DrugsDO;
import com.sqlapk.medicine.module.drugs.service.drug.DrugsService;
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

@Tag(name = "管理后台 - 药品")
@RestController
@RequestMapping("/drugs/")
@Validated
public class DrugsController {

    @Resource
    private DrugsService Service;

    @PostMapping("/create")
    @Operation(summary = "创建药品")
    @PreAuthorize("@ss.hasPermission('drugs::create')")
    public CommonResult<Long> create(@Valid @RequestBody DrugsCreateReqVO createReqVO) {
        return success(Service.create(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新药品")
    @PreAuthorize("@ss.hasPermission('drugs::update')")
    public CommonResult<Boolean> update(@Valid @RequestBody DrugsUpdateReqVO updateReqVO) {
        Service.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除药品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('drugs::delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        Service.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得药品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('drugs::query')")
    public CommonResult<DrugsRespVO> get(@RequestParam("id") Long id) {
        DrugsDO  drugsDO = Service.get(id);
        return success(DrugsConvert.INSTANCE.convert(drugsDO));
    }

    @GetMapping("/list")
    @Operation(summary = "获得药品列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('drugs::query')")
    public CommonResult<List<DrugsRespVO>> getList(@RequestParam("ids") Collection<Long> ids) {
        List<DrugsDO> list = Service.getList(ids);
        return success(DrugsConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得药品分页")
    @PreAuthorize("@ss.hasPermission('drugs::query')")
    public CommonResult<PageResult<DrugsRespVO>> getPage(@Valid DrugsPageReqVO pageVO) {
        PageResult<DrugsDO> pageResult = Service.getPage(pageVO);
        return success(DrugsConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出药品 Excel")
    @PreAuthorize("@ss.hasPermission('drugs::export')")
    @OperateLog(type = EXPORT)
    public void exportExcel(@Valid DrugsExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<DrugsDO> list = Service.getList(exportReqVO);
        // 导出 Excel
        List<DrugsExcelVO> datas = DrugsConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "药品.xls", "数据", DrugsExcelVO.class, datas);
    }

}
