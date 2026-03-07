package com.sqlapk.medicine.module.patient.controller.admin.questions;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;

import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import static com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum.*;

import com.sqlapk.medicine.module.patient.controller.admin.questions.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.questions.QuestionsDO;
import com.sqlapk.medicine.module.patient.convert.questions.QuestionsConvert;
import com.sqlapk.medicine.module.patient.service.questions.QuestionsService;

@Tag(name = "管理后台 - 问卷问题")
@RestController
@RequestMapping("/patient/questions")
@Validated
public class QuestionsController {

    @Resource
    private QuestionsService questionsService;

    @PostMapping("/create")
    @Operation(summary = "创建问卷问题")
    @PreAuthorize("@ss.hasPermission('patient:questions:create')")
    public CommonResult<Long> createQuestions(@Valid @RequestBody QuestionsCreateReqVO createReqVO) {
        return success(questionsService.createQuestions(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新问卷问题")
    @PreAuthorize("@ss.hasPermission('patient:questions:update')")
    public CommonResult<Boolean> updateQuestions(@Valid @RequestBody QuestionsUpdateReqVO updateReqVO) {
        questionsService.updateQuestions(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除问卷问题")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:questions:delete')")
    public CommonResult<Boolean> deleteQuestions(@RequestParam("id") Long id) {
        questionsService.deleteQuestions(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得问卷问题")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:questions:query')")
    public CommonResult<QuestionsRespVO> getQuestions(@RequestParam("id") Long id) {
        QuestionsDO questions = questionsService.getQuestions(id);
        return success(QuestionsConvert.INSTANCE.convert(questions));
    }

    @GetMapping("/list")
    @Operation(summary = "获得问卷问题列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:questions:query')")
    public CommonResult<List<QuestionsRespVO>> getQuestionsList(@RequestParam("ids") Collection<Long> ids) {
        List<QuestionsDO> list = questionsService.getQuestionsList(ids);
        return success(QuestionsConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得问卷问题分页")
    @PreAuthorize("@ss.hasPermission('patient:questions:query')")
    public CommonResult<PageResult<QuestionsRespVO>> getQuestionsPage(@Valid QuestionsPageReqVO pageVO) {
        PageResult<QuestionsDO> pageResult = questionsService.getQuestionsPage(pageVO);
        return success(QuestionsConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出问卷问题 Excel")
    @PreAuthorize("@ss.hasPermission('patient:questions:export')")
    @OperateLog(type = EXPORT)
    public void exportQuestionsExcel(@Valid QuestionsExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<QuestionsDO> list = questionsService.getQuestionsList(exportReqVO);
        // 导出 Excel
        List<QuestionsExcelVO> datas = QuestionsConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "问卷问题.xls", "数据", QuestionsExcelVO.class, datas);
    }

}
