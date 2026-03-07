package com.sqlapk.medicine.module.patient.controller.admin.answers;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.AnswersCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.AnswersExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.AnswersExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.AnswersPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.AnswersRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.AnswersUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.answers.AnswersConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.answers.AnswersDO;
import com.sqlapk.medicine.module.patient.enums.QuestionnaireId;
import com.sqlapk.medicine.module.patient.service.answers.AnswersService;
import com.sqlapk.medicine.module.system.api.user.AdminUserApi;
import com.sqlapk.medicine.module.system.api.user.dto.AdminUserRespDTO;
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

@Tag(name = "管理后台 - 问卷")
@RestController
@RequestMapping("/patient/answers")
@Validated
public class AnswersController {

    @Resource
    private AnswersService answersService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建问卷")
//    @PreAuthorize("@ss.hasPermission('patient:answers:create')")
    public CommonResult<Long> createAnswers(@Valid @RequestBody AnswersCreateReqVO createReqVO) {
        return success(answersService.createAnswers(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新问卷")
    @PreAuthorize("@ss.hasPermission('patient:answers:update')")
    public CommonResult<Boolean> updateAnswers(@Valid @RequestBody AnswersUpdateReqVO updateReqVO) {
        answersService.updateAnswers(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除问卷")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:answers:delete')")
    public CommonResult<Boolean> deleteAnswers(@RequestParam("id") Long id) {
        answersService.deleteAnswers(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得问卷")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:answers:query')")
    public CommonResult<AnswersRespVO> getAnswers(@RequestParam("id") Long id) {
        AnswersDO answers = answersService.getAnswers(id);
        return success(AnswersConvert.INSTANCE.convert(answers));
    }

    @GetMapping("/list")
    @Operation(summary = "获得问卷列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:answers:query')")
    public CommonResult<List<AnswersRespVO>> getAnswersList(@RequestParam("ids") Collection<Long> ids) {
        List<AnswersDO> list = answersService.getAnswersList(ids);
        return success(AnswersConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得问卷分页")
//    @PreAuthorize("@ss.hasPermission('patient:answers:query')")
    public CommonResult<PageResult<AnswersRespVO>> getAnswersPage(@Valid AnswersPageReqVO pageVO) {
        PageResult<AnswersDO> pageResult = answersService.getAnswersPage(pageVO);
        PageResult<AnswersRespVO> answersRespVOPageResult = AnswersConvert.INSTANCE.convertPage(pageResult);
        List<AnswersRespVO> respVOPageResultList = answersRespVOPageResult.getList();
        for (AnswersRespVO answersRespVO : respVOPageResultList) {
            AdminUserRespDTO user = adminUserApi.getUser(answersRespVO.getUserId());
            answersRespVO.setPatientName(user.getNickname());
            answersRespVO.setQuestionName(QuestionnaireId.getQuestionnaireNameByValue(answersRespVO.getQuestionId().intValue()));
        }

        return success(answersRespVOPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出问卷 Excel")
    @PreAuthorize("@ss.hasPermission('patient:answers:export')")
    @OperateLog(type = EXPORT)
    public void exportAnswersExcel(@Valid AnswersExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<AnswersDO> list = answersService.getAnswersList(exportReqVO);
        // 导出 Excel
        List<AnswersExcelVO> datas = AnswersConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "问卷.xls", "数据", AnswersExcelVO.class, datas);
    }

}
