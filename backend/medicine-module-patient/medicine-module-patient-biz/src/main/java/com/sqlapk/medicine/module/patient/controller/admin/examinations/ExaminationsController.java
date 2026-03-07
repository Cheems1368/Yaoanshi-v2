package com.sqlapk.medicine.module.patient.controller.admin.examinations;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.examinations.ExaminationsConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.examinations.ExaminationsDO;
import com.sqlapk.medicine.module.patient.service.examinations.ExaminationsService;
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

@Tag(name = "管理后台 - 检查项目")
@RestController
@RequestMapping("/patient/examinations")
@Validated
public class ExaminationsController {

    @Resource
    private ExaminationsService examinationsService;

    @PostMapping("/create")
    @Operation(summary = "创建检查项目")
    @PreAuthorize("@ss.hasPermission('patient:examinations:create')")
    public CommonResult<Long> createExaminations(@Valid @RequestBody ExaminationsCreateReqVO createReqVO) {
        return success(examinationsService.createExaminations(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新检查项目")
    @PreAuthorize("@ss.hasPermission('patient:examinations:update')")
    public CommonResult<Boolean> updateExaminations(@Valid @RequestBody ExaminationsUpdateReqVO updateReqVO) {
        examinationsService.updateExaminations(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除检查项目")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:examinations:delete')")
    public CommonResult<Boolean> deleteExaminations(@RequestParam("id") Long id) {
        examinationsService.deleteExaminations(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得检查项目")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:examinations:query')")
    public CommonResult<ExaminationsRespVO> getExaminations(@RequestParam("id") Long id) {
        ExaminationsDO examinations = examinationsService.getExaminations(id);
        return success(ExaminationsConvert.INSTANCE.convert(examinations));
    }

    @GetMapping("/list")
    @Operation(summary = "获得检查项目列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:examinations:query')")
    public CommonResult<List<ExaminationsRespVO>> getExaminationsList(@RequestParam("ids") Collection<Long> ids) {
        List<ExaminationsDO> list = examinationsService.getExaminationsList(ids);
        return success(ExaminationsConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得检查项目分页")
    @PreAuthorize("@ss.hasPermission('patient:examinations:query')")
    public CommonResult<PageResult<ExaminationsRespVO>> getExaminationsPage(@Valid ExaminationsPageReqVO pageVO) {
        PageResult<ExaminationsDO> pageResult = examinationsService.getExaminationsPage(pageVO);
        return success(ExaminationsConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出检查项目 Excel")
    @PreAuthorize("@ss.hasPermission('patient:examinations:export')")
    @OperateLog(type = EXPORT)
    public void exportExaminationsExcel(@Valid ExaminationsExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ExaminationsDO> list = examinationsService.getExaminationsList(exportReqVO);
        // 导出 Excel
        List<ExaminationsExcelVO> datas = ExaminationsConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "检查项目.xls", "数据", ExaminationsExcelVO.class, datas);
    }

}
