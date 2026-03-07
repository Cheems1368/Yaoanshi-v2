package com.sqlapk.medicine.module.patient.controller.admin.homeeducation;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.homeeducation.HomeEducationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeeducation.HomeEducationDO;
import com.sqlapk.medicine.module.patient.service.homeeducation.HomeEducationService;
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

@Tag(name = "管理后台 - 居家用药教育")
@RestController
@RequestMapping("/patient/home-education")
@Validated
public class HomeEducationController {

    @Resource
    private HomeEducationService homeEducationService;

    @PostMapping("/create")
    @Operation(summary = "创建居家用药教育")
    @PreAuthorize("@ss.hasPermission('patient:home-education:create')")
    public CommonResult<Long> createHomeEducation(@Valid @RequestBody HomeEducationCreateReqVO createReqVO) {
        return success(homeEducationService.createHomeEducation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新居家用药教育")
    @PreAuthorize("@ss.hasPermission('patient:home-education:update')")
    public CommonResult<Boolean> updateHomeEducation(@Valid @RequestBody HomeEducationUpdateReqVO updateReqVO) {
        homeEducationService.updateHomeEducation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除居家用药教育")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:home-education:delete')")
    public CommonResult<Boolean> deleteHomeEducation(@RequestParam("id") Long id) {
        homeEducationService.deleteHomeEducation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得居家用药教育")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:home-education:query')")
    public CommonResult<HomeEducationRespVO> getHomeEducation(@RequestParam("id") Long id) {
        HomeEducationDO homeEducation = homeEducationService.getHomeEducation(id);
        return success(HomeEducationConvert.INSTANCE.convert(homeEducation));
    }

    @GetMapping("/list")
    @Operation(summary = "获得居家用药教育列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:home-education:query')")
    public CommonResult<List<HomeEducationRespVO>> getHomeEducationList(@RequestParam("ids") Collection<Long> ids) {
        List<HomeEducationDO> list = homeEducationService.getHomeEducationList(ids);
        return success(HomeEducationConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得居家用药教育分页")
    @PreAuthorize("@ss.hasPermission('patient:home-education:query')")
    public CommonResult<PageResult<HomeEducationRespVO>> getHomeEducationPage(@Valid HomeEducationPageReqVO pageVO) {
        PageResult<HomeEducationDO> pageResult = homeEducationService.getHomeEducationPage(pageVO);
        return success(HomeEducationConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出居家用药教育 Excel")
    @PreAuthorize("@ss.hasPermission('patient:home-education:export')")
    @OperateLog(type = EXPORT)
    public void exportHomeEducationExcel(@Valid HomeEducationExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<HomeEducationDO> list = homeEducationService.getHomeEducationList(exportReqVO);
        // 导出 Excel
        List<HomeEducationExcelVO> datas = HomeEducationConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "居家用药教育.xls", "数据", HomeEducationExcelVO.class, datas);
    }

}
