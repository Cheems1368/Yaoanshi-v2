package com.sqlapk.medicine.module.patient.controller.admin.homeconsultation;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.homeconsultation.HomeConsultationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeconsultation.HomeConsultationDO;
import com.sqlapk.medicine.module.patient.service.homeconsultation.HomeConsultationService;
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

@Tag(name = "管理后台 - 居家用药咨询")
@RestController
@RequestMapping("/patient/home-consultation")
@Validated
public class HomeConsultationController {

    @Resource
    private HomeConsultationService homeConsultationService;

    @PostMapping("/create")
    @Operation(summary = "创建居家用药咨询")
    @PreAuthorize("@ss.hasPermission('patient:home-consultation:create')")
    public CommonResult<Long> createHomeConsultation(@Valid @RequestBody HomeConsultationCreateReqVO createReqVO) {
        return success(homeConsultationService.createHomeConsultation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新居家用药咨询")
    @PreAuthorize("@ss.hasPermission('patient:home-consultation:update')")
    public CommonResult<Boolean> updateHomeConsultation(@Valid @RequestBody HomeConsultationUpdateReqVO updateReqVO) {
        homeConsultationService.updateHomeConsultation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除居家用药咨询")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:home-consultation:delete')")
    public CommonResult<Boolean> deleteHomeConsultation(@RequestParam("id") Long id) {
        homeConsultationService.deleteHomeConsultation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得居家用药咨询")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:home-consultation:query')")
    public CommonResult<HomeConsultationRespVO> getHomeConsultation(@RequestParam("id") Long id) {
        HomeConsultationDO homeConsultation = homeConsultationService.getHomeConsultation(id);
        return success(HomeConsultationConvert.INSTANCE.convert(homeConsultation));
    }

    @GetMapping("/list")
    @Operation(summary = "获得居家用药咨询列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:home-consultation:query')")
    public CommonResult<List<HomeConsultationRespVO>> getHomeConsultationList(@RequestParam("ids") Collection<Long> ids) {
        List<HomeConsultationDO> list = homeConsultationService.getHomeConsultationList(ids);
        return success(HomeConsultationConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得居家用药咨询分页")
    @PreAuthorize("@ss.hasPermission('patient:home-consultation:query')")
    public CommonResult<PageResult<HomeConsultationRespVO>> getHomeConsultationPage(@Valid HomeConsultationPageReqVO pageVO) {
        PageResult<HomeConsultationDO> pageResult = homeConsultationService.getHomeConsultationPage(pageVO);
        return success(HomeConsultationConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出居家用药咨询 Excel")
    @PreAuthorize("@ss.hasPermission('patient:home-consultation:export')")
    @OperateLog(type = EXPORT)
    public void exportHomeConsultationExcel(@Valid HomeConsultationExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<HomeConsultationDO> list = homeConsultationService.getHomeConsultationList(exportReqVO);
        // 导出 Excel
        List<HomeConsultationExcelVO> datas = HomeConsultationConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "居家用药咨询.xls", "数据", HomeConsultationExcelVO.class, datas);
    }

}
