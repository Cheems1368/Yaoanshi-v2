package com.sqlapk.medicine.module.patient.controller.admin.homeevaluation;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.homeevaluation.HomeEvaluationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeevaluation.HomeEvaluationDO;
import com.sqlapk.medicine.module.patient.service.homeevaluation.HomeEvaluationService;
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

@Tag(name = "管理后台 - 居家随访评估")
@RestController
@RequestMapping("/patient/home-evaluation")
@Validated
public class HomeEvaluationController {

    @Resource
    private HomeEvaluationService homeEvaluationService;

    @PostMapping("/create")
    @Operation(summary = "创建居家随访评估")
    @PreAuthorize("@ss.hasPermission('patient:home-evaluation:create')")
    public CommonResult<Long> createHomeEvaluation(@Valid @RequestBody HomeEvaluationCreateReqVO createReqVO) {
        return success(homeEvaluationService.createHomeEvaluation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新居家随访评估")
    @PreAuthorize("@ss.hasPermission('patient:home-evaluation:update')")
    public CommonResult<Boolean> updateHomeEvaluation(@Valid @RequestBody HomeEvaluationUpdateReqVO updateReqVO) {
        homeEvaluationService.updateHomeEvaluation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除居家随访评估")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:home-evaluation:delete')")
    public CommonResult<Boolean> deleteHomeEvaluation(@RequestParam("id") Long id) {
        homeEvaluationService.deleteHomeEvaluation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得居家随访评估")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:home-evaluation:query')")
    public CommonResult<HomeEvaluationRespVO> getHomeEvaluation(@RequestParam("id") Long id) {
        HomeEvaluationDO homeEvaluation = homeEvaluationService.getHomeEvaluation(id);
        return success(HomeEvaluationConvert.INSTANCE.convert(homeEvaluation));
    }

    @GetMapping("/list")
    @Operation(summary = "获得居家随访评估列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:home-evaluation:query')")
    public CommonResult<List<HomeEvaluationRespVO>> getHomeEvaluationList(@RequestParam("ids") Collection<Long> ids) {
        List<HomeEvaluationDO> list = homeEvaluationService.getHomeEvaluationList(ids);
        return success(HomeEvaluationConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得居家随访评估分页")
    @PreAuthorize("@ss.hasPermission('patient:home-evaluation:query')")
    public CommonResult<PageResult<HomeEvaluationRespVO>> getHomeEvaluationPage(@Valid HomeEvaluationPageReqVO pageVO) {
        PageResult<HomeEvaluationDO> pageResult = homeEvaluationService.getHomeEvaluationPage(pageVO);
        return success(HomeEvaluationConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出居家随访评估 Excel")
    @PreAuthorize("@ss.hasPermission('patient:home-evaluation:export')")
    @OperateLog(type = EXPORT)
    public void exportHomeEvaluationExcel(@Valid HomeEvaluationExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<HomeEvaluationDO> list = homeEvaluationService.getHomeEvaluationList(exportReqVO);
        // 导出 Excel
        List<HomeEvaluationExcelVO> datas = HomeEvaluationConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "居家随访评估.xls", "数据", HomeEvaluationExcelVO.class, datas);
    }

}
