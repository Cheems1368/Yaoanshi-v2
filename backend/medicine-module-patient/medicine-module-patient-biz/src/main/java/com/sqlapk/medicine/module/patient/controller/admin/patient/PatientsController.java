package com.sqlapk.medicine.module.patient.controller.admin.patient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsSimpleRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.patient.PatientsConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import com.sqlapk.medicine.module.patient.service.patient.PatientsService;
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
import java.util.Map;
import java.util.stream.Collectors;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;
import static com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 患者")
@RestController
@RequestMapping("/patient/list")
@Validated
public class PatientsController {

    @Resource
    private PatientsService patientsService;

    @Resource
    private PatientsConvert patientsConvert;

    @PostMapping("/create")
    @Operation(summary = "创建患者")
    @PreAuthorize("@ss.hasPermission('patient:list:create')")
    public CommonResult<Long> creates(@Valid @RequestBody PatientsCreateReqVO createReqVO) {
        return success(patientsService.creates(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新患者")
    @PreAuthorize("@ss.hasPermission('patient:list:update')")
    public CommonResult<Boolean> updates(@Valid @RequestBody PatientsUpdateReqVO updateReqVO) {
        patientsService.updates(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除患者")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:list:delete')")
    public CommonResult<Boolean> deletes(@RequestParam("id") Long id) {
        patientsService.deletes(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得患者")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:list:query')")
    public CommonResult<PatientsRespVO> gets(@RequestParam("id") Long id) {
        PatientsDO s = patientsService.gets(id);
        return success(patientsConvert.convert(s));
    }

    @GetMapping("/list")
    @Operation(summary = "获得患者列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:list:query')")
    public CommonResult<List<PatientsRespVO>> getsList(@RequestParam("ids") Collection<Long> ids) {
        List<PatientsDO> list = patientsService.getsList(ids);
        return success(patientsConvert.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得患者分页")
    @PreAuthorize("@ss.hasPermission('patient:list:query')")
    public CommonResult<PageResult<PatientsRespVO>> getsPage(@Valid PatientsPageReqVO pageVO) {
        PageResult<PatientsDO> pageResult = patientsService.getsPage(pageVO);
        return success(patientsConvert.convertPage(pageResult));
    }

    @GetMapping("/list-simple")
    @Operation(summary = "获得患者分页")
    @PreAuthorize("@ss.hasPermission('patient:list:query')")
    public CommonResult<List<PatientsSimpleRespVO>> getSimpleRepo() {
        List<PatientsSimpleRespVO> pageResult = patientsService.getPatientSimple();
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出患者 Excel")
    @PreAuthorize("@ss.hasPermission('patient:list:export')")
    @OperateLog(type = EXPORT)
    public void exportsExcel(@Valid PatientsExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<PatientsDO> list = patientsService.getsList(exportReqVO);
        // 导出 Excel
        List<PatientsExcelVO> datas = patientsConvert.convertList02(list);
        ExcelUtils.write(response, "患者.xls", "数据", PatientsExcelVO.class, datas);
    }

    @GetMapping("/source")
    @Operation(summary = "导出患者 Excel")
    public CommonResult<ArrayNode> source() {
        List<PatientsDO> patientSource = patientsService.getsList(new PatientsExportReqVO());
        Map<Short, Long> patientSourceMap = patientSource.stream().collect(Collectors.groupingBy(PatientsDO::getPatientType, Collectors.counting()));
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();
        patientSourceMap.forEach((k,v) -> {
            ObjectNode json = mapper.createObjectNode();
            json.put("value", v);
            json.put("name", getName(k).toString());
            result.add(json);
        });

        return success(result);
    }

    private Object getName(Short v) {
        if (v == 0) {
            return "门诊";
        }

        if (v == 1) {
            return "住院";
        }

        if (v == 2) {
            return "居家";
        }

        return "其他";
    }

}
