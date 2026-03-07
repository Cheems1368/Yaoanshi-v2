package com.sqlapk.medicine.module.patient.controller.admin.home;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeUpdateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.convert.home.HomeConvert;
import com.sqlapk.medicine.module.patient.convert.homeconsultation.HomeConsultationConvert;
import com.sqlapk.medicine.module.patient.convert.homeeducation.HomeEducationConvert;
import com.sqlapk.medicine.module.patient.convert.homeevaluation.HomeEvaluationConvert;
import com.sqlapk.medicine.module.patient.convert.homemedicine.HomeMedicineConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.home.HomeDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeconsultation.HomeConsultationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeeducation.HomeEducationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeevaluation.HomeEvaluationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homemedicine.HomeMedicineDO;
import com.sqlapk.medicine.module.patient.dal.mysql.homeconsultation.HomeConsultationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.homeeducation.HomeEducationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.homeevaluation.HomeEvaluationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.homemedicine.HomeMedicineMapper;
import com.sqlapk.medicine.module.patient.service.home.HomeService;
import com.sqlapk.medicine.module.patient.service.homeconsultation.HomeConsultationService;
import com.sqlapk.medicine.module.patient.service.homeeducation.HomeEducationService;
import com.sqlapk.medicine.module.patient.service.homeevaluation.HomeEvaluationService;
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

@Tag(name = "管理后台 - 居家药学服务")
@RestController
@RequestMapping("/patient/home")
@Validated
public class HomeController {

    @Resource
    private HomeService homeService;
    @Resource
    private HomeConsultationService homeConsultationService;
    @Resource
    private HomeConsultationMapper homeConsultationMapper;
    @Resource
    private HomeEducationService homeEducationService;
    @Resource
    private HomeEducationMapper homeEducationMapper;
    @Resource
    private HomeEvaluationService homeEvaluationService;
    @Resource
    private HomeEvaluationMapper homeEvaluationMapper;
    @Resource
    private HomeMedicineService homeMedicineService;
    @Resource
    private HomeMedicineMapper homeMedicineMapper;

    @PostMapping("/create")
    @Operation(summary = "创建居家药学服务")
    @PreAuthorize("@ss.hasPermission('patient:home:create')")
    public CommonResult<Long> createHome(@Valid @RequestBody HomeCreateReqVO createReqVO) {
        return success(homeService.createHome(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新居家药学服务")
    @PreAuthorize("@ss.hasPermission('patient:home:update')")
    public CommonResult<Boolean> updateHome(@Valid @RequestBody HomeUpdateReqVO updateReqVO) {
        homeService.updateHome(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除居家药学服务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:home:delete')")
    public CommonResult<Boolean> deleteHome(@RequestParam("id") Long id) {
        homeService.deleteHome(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得居家药学服务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:home:query')")
    public CommonResult<HomeRespVO> getHome(@RequestParam("id") Long id) {
        HomeDO home = homeService.getHome(id);
        HomeRespVO result = HomeConvert.INSTANCE.convert(home);

        HomeConsultationExportReqVO consultation = new HomeConsultationExportReqVO();
        consultation.setHomeId(id);
        List<HomeConsultationDO> homeConsultationList = homeConsultationService.getHomeConsultationList(consultation);
        result.setConsultation(HomeConsultationConvert.INSTANCE.convertList(homeConsultationList));

        HomeEducationExportReqVO education = new HomeEducationExportReqVO();
        education.setHomeId(id);
        List<HomeEducationDO> homeEducationList = homeEducationService.getHomeEducationList(education);
        result.setEducation(HomeEducationConvert.INSTANCE.convertList(homeEducationList));

        HomeEvaluationExportReqVO evaluation= new HomeEvaluationExportReqVO();
        evaluation.setHomeId(id);
        List<HomeEvaluationDO> homeEvaluationList = homeEvaluationService.getHomeEvaluationList(evaluation);
        result.setEvaluation(HomeEvaluationConvert.INSTANCE.convertList(homeEvaluationList));

        HomeMedicineExportReqVO medicine= new HomeMedicineExportReqVO();
        medicine.setHomeId(id);
        List<HomeMedicineDO> homeMedicineList = homeMedicineService.getHomeMedicineList(medicine);
        result.setMedicine(HomeMedicineConvert.INSTANCE.convertList(homeMedicineList));

        return success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获得居家药学服务列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:home:query')")
    public CommonResult<List<HomeRespVO>> getHomeList(@RequestParam("ids") Collection<Long> ids) {
        List<HomeDO> list = homeService.getHomeList(ids);
        return success(HomeConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得居家药学服务分页")
    @PreAuthorize("@ss.hasPermission('patient:home:query')")
    public CommonResult<PageResult<HomeRespVO>> getHomePage(@Valid HomePageReqVO pageVO) {
        PageResult<HomeDO> pageResult = homeService.getHomePage(pageVO);
        return success(HomeConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出居家药学服务 Excel")
    @PreAuthorize("@ss.hasPermission('patient:home:export')")
    @OperateLog(type = EXPORT)
    public void exportHomeExcel(@Valid HomeExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<HomeDO> list = homeService.getHomeList(exportReqVO);
        // 导出 Excel
        List<HomeExcelVO> datas = HomeConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "居家药学服务.xls", "数据", HomeExcelVO.class, datas);
    }

}
