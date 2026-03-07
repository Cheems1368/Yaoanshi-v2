package com.sqlapk.medicine.module.patient.controller.admin.prescriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.prescriptions.PrescriptionsConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptions.PrescriptionsDO;
import com.sqlapk.medicine.module.patient.service.prescriptions.PrescriptionsService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;
import static com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 处方")
@RestController
@RequestMapping("/prescription/prescriptions")
@Validated
public class PrescriptionsController {

    @Resource
    private PrescriptionsService prescriptionsService;

    @PostMapping("/create")
    @Operation(summary = "创建处方")
    @PreAuthorize("@ss.hasPermission('patient:prescriptions:create')")
    public CommonResult<Long> createPrescriptions(@Valid @RequestBody PrescriptionsCreateReqVO createReqVO) {
        return success(prescriptionsService.createPrescriptions(createReqVO));
    }

    @PostMapping("/createDetail")
    @Operation(summary = "创建处方")
    @PreAuthorize("@ss.hasPermission('patient:prescriptions:create')")
    public CommonResult<Long> createPrescriptionsDetail(@Valid @RequestBody PrescriptionsCreateReqVO createReqVO) {
        return success(prescriptionsService.createPrescriptions(createReqVO));
    }


    @PutMapping("/update")
    @Operation(summary = "更新处方")
    @PreAuthorize("@ss.hasPermission('patient:prescriptions:update')")
    public CommonResult<Boolean> updatePrescriptions(@Valid @RequestBody PrescriptionsUpdateReqVO updateReqVO) {
        prescriptionsService.updatePrescriptions(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除处方")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:prescriptions:delete')")
    public CommonResult<Boolean> deletePrescriptions(@RequestParam("id") Long id) {
        prescriptionsService.deletePrescriptions(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得处方")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:prescriptions:query')")
    public CommonResult<PrescriptionsRespVO> getPrescriptions(@RequestParam("id") Long id) {
        PrescriptionsDO prescriptions = prescriptionsService.getPrescriptions(id);
        return success(PrescriptionsConvert.INSTANCE.convert(prescriptions));
    }

    @GetMapping("/list")
    @Operation(summary = "获得处方列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:prescriptions:query')")
    public CommonResult<List<PrescriptionsRespVO>> getPrescriptionsList(@RequestParam("ids") Collection<Long> ids) {
        List<PrescriptionsDO> list = prescriptionsService.getPrescriptionsList(ids);
        return success(PrescriptionsConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得处方分页")
    @PreAuthorize("@ss.hasPermission('patient:prescriptions:query')")
    public CommonResult<PageResult<PrescriptionsRespVO>> getPrescriptionsPage(@Valid PrescriptionsPageReqVO pageVO) {
        PageResult<PrescriptionsDO> pageResult = prescriptionsService.getPrescriptionsPage(pageVO);
        return success(PrescriptionsConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出处方 Excel")
    @PreAuthorize("@ss.hasPermission('patient:prescriptions:export')")
    @OperateLog(type = EXPORT)
    public void exportPrescriptionsExcel(@Valid PrescriptionsExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<PrescriptionsDO> list = prescriptionsService.getPrescriptionsList(exportReqVO);
        // 导出 Excel
        List<PrescriptionsExcelVO> datas = PrescriptionsConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "处方.xls", "数据", PrescriptionsExcelVO.class, datas);
    }

    @GetMapping("/distribute")
    public CommonResult<ArrayNode> distribute() {
        String DATE_TIME_PATTERN = "yyyy-MM-dd";

        PrescriptionsExportReqVO reqVO = new PrescriptionsExportReqVO();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minusDays = now.minusDays(7L);
        reqVO.setCreateTime(new LocalDateTime[]{minusDays, now});

        List<PrescriptionsDO> list = prescriptionsService.getPrescriptionsList(reqVO);
        Map<String, Long> dataList = list.stream().collect(Collectors.groupingBy(item -> DateTimeFormatter.ofPattern(DATE_TIME_PATTERN,
                Locale.SIMPLIFIED_CHINESE).format(item.getCreateTime()), Collectors.counting()));
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonArray = mapper.createArrayNode();
        dataList.forEach((k,v) -> {
            ObjectNode jsonObject = mapper.createObjectNode();
            jsonObject.put("name", k);
            jsonObject.put("value", v);
            jsonArray.add(jsonObject);
        });
        return success(jsonArray);
    }
}
