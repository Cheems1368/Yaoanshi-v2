package com.sqlapk.medicine.module.system.documentation;

import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 工单API文档生成器
 * 提供完整的API文档和示例
 *
 * @author sqlapk
 */
@Component
@Slf4j
public class HelpOrderApiDocumentation {

    /**
     * 获取完整的API文档
     */
    public Map<String, Object> getCompleteApiDocumentation() {
        Map<String, Object> documentation = new HashMap<>();
        
        documentation.put("title", "工单系统API文档");
        documentation.put("version", "1.0.0");
        documentation.put("description", "工单管理系统的完整API文档，包含所有接口的详细说明、参数、示例和错误码");
        documentation.put("baseUrl", "http://localhost:8080/admin-api/help-orders");
        documentation.put("lastUpdated", LocalDateTime.now().toString());
        
        // API接口文档
        documentation.put("endpoints", getEndpointsDocumentation());
        
        // 数据模型文档
        documentation.put("models", getModelsDocumentation());
        
        // 错误码文档
        documentation.put("errorCodes", getErrorCodesDocumentation());
        
        // 状态流转文档
        documentation.put("statusFlow", getStatusFlowDocumentation());
        
        // 示例代码
        documentation.put("examples", getExamplesDocumentation());
        
        log.info("生成完整的API文档");
        return documentation;
    }

    /**
     * 获取API接口文档
     */
    private Map<String, Object> getEndpointsDocumentation() {
        Map<String, Object> endpoints = new HashMap<>();
        
        // 创建工单
        endpoints.put("create", Map.of(
            "method", "POST",
            "path", "/create",
            "description", "创建新的工单",
            "parameters", Map.of(
                "patientId", "患者ID (必填)",
                "title", "工单标题 (必填, 最大100字符)",
                "description", "工单描述 (必填, 最大1000字符)",
                "intendedDrugs", "意向药品列表 (可选, 最多10个)",
                "attachments", "附件列表 (可选, 最多5个)",
                "toDoctor", "是否直接转给医生 (可选, 默认false)"
            ),
            "response", Map.of(
                "code", "响应码 (0表示成功)",
                "data", "工单ID",
                "msg", "响应消息"
            ),
            "example", getCreateExample()
        ));
        
        // 获取工单详情
        endpoints.put("get", Map.of(
            "method", "GET",
            "path", "/get",
            "description", "根据ID获取工单详情",
            "parameters", Map.of(
                "id", "工单ID (必填)"
            ),
            "response", Map.of(
                "code", "响应码",
                "data", "工单详细信息",
                "msg", "响应消息"
            ),
            "example", getGetExample()
        ));
        
        // 分页查询工单
        endpoints.put("page", Map.of(
            "method", "GET",
            "path", "/page",
            "description", "分页查询工单列表",
            "parameters", Map.of(
                "pageNo", "页码 (必填, 从1开始)",
                "pageSize", "每页大小 (必填, 1-100)",
                "status", "工单状态 (可选, 支持字符串或数字)",
                "patientId", "患者ID (可选)",
                "assignedPharmacistId", "分配药师ID (可选)",
                "assignedDoctorId", "分配医生ID (可选)",
                "title", "标题关键词 (可选)",
                "createTime", "创建时间范围 (可选, 数组格式)"
            ),
            "response", Map.of(
                "code", "响应码",
                "data", Map.of(
                    "list", "工单列表",
                    "total", "总记录数"
                ),
                "msg", "响应消息"
            ),
            "example", getPageExample()
        ));
        
        // 更新工单
        endpoints.put("update", Map.of(
            "method", "PUT",
            "path", "/update",
            "description", "更新工单信息",
            "parameters", Map.of(
                "id", "工单ID (必填)",
                "title", "工单标题 (可选)",
                "description", "工单描述 (可选)",
                "intendedDrugs", "意向药品列表 (可选)",
                "attachments", "附件列表 (可选)",
                "toDoctor", "是否转给医生 (可选)"
            ),
            "response", Map.of(
                "code", "响应码",
                "data", "更新结果",
                "msg", "响应消息"
            ),
            "example", getUpdateExample()
        ));
        
        // 分配药师
        endpoints.put("assign", Map.of(
            "method", "POST",
            "path", "/assign",
            "description", "分配药师处理工单",
            "parameters", Map.of(
                "id", "工单ID (必填)",
                "pharmacistId", "药师ID (必填)"
            ),
            "response", Map.of(
                "code", "响应码",
                "data", "分配结果",
                "msg", "响应消息"
            ),
            "example", getAssignExample()
        ));
        
        // 升级医生
        endpoints.put("escalate", Map.of(
            "method", "POST",
            "path", "/escalate",
            "description", "升级工单给医生处理",
            "parameters", Map.of(
                "id", "工单ID (必填)",
                "needDoctor", "是否需要医生 (必填)",
                "doctorId", "医生ID (可选)"
            ),
            "response", Map.of(
                "code", "响应码",
                "data", "升级结果",
                "msg", "响应消息"
            ),
            "example", getEscalateExample()
        ));
        
        // 驳回工单
        endpoints.put("reject", Map.of(
            "method", "POST",
            "path", "/reject",
            "description", "驳回工单",
            "parameters", Map.of(
                "id", "工单ID (必填)",
                "reason", "驳回原因 (必填, 最大500字符)"
            ),
            "response", Map.of(
                "code", "响应码",
                "data", "驳回结果",
                "msg", "响应消息"
            ),
            "example", getRejectExample()
        ));
        
        // 更新状态
        endpoints.put("updateStatus", Map.of(
            "method", "PUT",
            "path", "/update-status",
            "description", "更新工单状态",
            "parameters", Map.of(
                "id", "工单ID (必填)",
                "status", "新状态 (必填)",
                "reason", "状态更新原因 (可选, 最大500字符)"
            ),
            "response", Map.of(
                "code", "响应码",
                "data", "更新结果",
                "msg", "响应消息"
            ),
            "example", getUpdateStatusExample()
        ));
        
        // 导出Excel
        endpoints.put("exportExcel", Map.of(
            "method", "GET",
            "path", "/export-excel",
            "description", "导出工单数据为Excel文件",
            "parameters", Map.of(
                "status", "工单状态 (可选)",
                "patientId", "患者ID (可选)",
                "assignedPharmacistId", "分配药师ID (可选)",
                "assignedDoctorId", "分配医生ID (可选)",
                "title", "标题关键词 (可选)",
                "createTime", "创建时间范围 (可选)"
            ),
            "response", "Excel文件流",
            "example", getExportExample()
        ));
        
        // 删除工单
        endpoints.put("delete", Map.of(
            "method", "DELETE",
            "path", "/delete",
            "description", "删除工单",
            "parameters", Map.of(
                "id", "工单ID (必填)"
            ),
            "response", Map.of(
                "code", "响应码",
                "data", "删除结果",
                "msg", "响应消息"
            ),
            "example", getDeleteExample()
        ));
        
        return endpoints;
    }

    /**
     * 获取数据模型文档
     */
    private Map<String, Object> getModelsDocumentation() {
        Map<String, Object> models = new HashMap<>();
        
        // 工单对象
        Map<String, String> helpOrderModel = new HashMap<>();
        helpOrderModel.put("id", "工单ID (Long)");
        helpOrderModel.put("patientId", "患者ID (Long)");
        helpOrderModel.put("title", "工单标题 (String)");
        helpOrderModel.put("description", "工单描述 (String)");
        helpOrderModel.put("status", "工单状态 (HelpOrderStatus)");
        helpOrderModel.put("intendedDrugs", "意向药品列表 (List<String>)");
        helpOrderModel.put("attachments", "附件列表 (List<String>)");
        helpOrderModel.put("assignedPharmacistId", "分配药师ID (Long)");
        helpOrderModel.put("assignedDoctorId", "分配医生ID (Long)");
        helpOrderModel.put("rejectReason", "驳回原因 (String)");
        helpOrderModel.put("createTime", "创建时间 (LocalDateTime)");
        helpOrderModel.put("updateTime", "更新时间 (LocalDateTime)");
        helpOrderModel.put("timeline", "时间线事件列表 (List<TimelineEvent>)");
        models.put("HelpOrder", helpOrderModel);
        
        // 时间线事件
        models.put("TimelineEvent", Map.of(
            "id", "事件ID (Long)",
            "helpOrderId", "工单ID (Long)",
            "eventType", "事件类型 (String)",
            "description", "事件描述 (String)",
            "operatorId", "操作者ID (Long)",
            "operatorName", "操作者姓名 (String)",
            "createTime", "创建时间 (LocalDateTime)"
        ));
        
        // 分页结果
        models.put("PageResult", Map.of(
            "list", "数据列表 (List<T>)",
            "total", "总记录数 (Long)"
        ));
        
        return models;
    }

    /**
     * 获取错误码文档
     */
    private Map<String, Object> getErrorCodesDocumentation() {
        Map<String, Object> errorCodes = new HashMap<>();
        
        // 基础验证错误
        errorCodes.put("基础验证", Map.of(
            "1_000_000_000", "工单标题不能为空",
            "1_000_000_001", "工单标题长度不能超过100个字符",
            "1_000_000_002", "工单描述不能为空",
            "1_000_000_003", "工单描述长度不能超过1000个字符",
            "1_000_000_004", "意向药品数量不能超过10个",
            "1_000_000_005", "附件数量不能超过5个"
        ));
        
        // 分页参数错误
        errorCodes.put("分页参数", Map.of(
            "1_000_000_010", "页码必须为正数",
            "1_000_000_011", "每页大小必须在1-100之间"
        ));
        
        // 时间参数错误
        errorCodes.put("时间参数", Map.of(
            "1_000_000_020", "开始时间不能晚于结束时间",
            "1_000_000_021", "时间范围不能超过30天",
            "1_000_000_022", "时间范围参数格式错误"
        ));
        
        // 业务逻辑错误
        errorCodes.put("业务逻辑", Map.of(
            "1_000_000_030", "用户ID不能为空",
            "1_000_000_031", "工单不存在",
            "1_000_000_032", "工单状态不能为空",
            "1_000_000_100", "工单状态无效",
            "1_000_000_101", "工单状态转换无效",
            "1_000_000_102", "工单已被分配",
            "1_000_000_103", "工单权限不足"
        ));
        
        return errorCodes;
    }

    /**
     * 获取状态流转文档
     */
    private Map<String, Object> getStatusFlowDocumentation() {
        Map<String, Object> statusFlow = new HashMap<>();
        
        statusFlow.put("状态说明", Map.of(
            "SUBMITTED", "已提交",
            "ASSIGNED_PHARMACIST", "已分配药师",
            "PHARMACIST_REVIEW", "药师审核中",
            "ADVICE_READY", "建议已准备",
            "TO_DOCTOR", "转给医生",
            "DOCTOR_REVIEW", "医生审核中",
            "PRESCRIPTION_READY", "处方已准备",
            "DONE", "已完成",
            "REJECTED", "已驳回"
        ));
        
        statusFlow.put("状态转换规则", Map.of(
            "SUBMITTED", "可以转换为 ASSIGNED_PHARMACIST 或 REJECTED",
            "ASSIGNED_PHARMACIST", "可以转换为 PHARMACIST_REVIEW 或 REJECTED",
            "PHARMACIST_REVIEW", "可以转换为 ADVICE_READY、TO_DOCTOR 或 REJECTED",
            "ADVICE_READY", "可以转换为 DONE 或 TO_DOCTOR",
            "TO_DOCTOR", "可以转换为 DOCTOR_REVIEW 或 REJECTED",
            "DOCTOR_REVIEW", "可以转换为 PRESCRIPTION_READY 或 REJECTED",
            "PRESCRIPTION_READY", "可以转换为 DONE",
            "DONE", "不能转换",
            "REJECTED", "可以转换为 SUBMITTED"
        ));
        
        return statusFlow;
    }

    /**
     * 获取示例代码文档
     */
    private Map<String, Object> getExamplesDocumentation() {
        Map<String, Object> examples = new HashMap<>();
        
        examples.put("JavaScript", Map.of(
            "创建工单", "const response = await fetch('/admin-api/help-orders/create', {\n" +
                "    method: 'POST',\n" +
                "    headers: {\n" +
                "        'Content-Type': 'application/json',\n" +
                "        'Authorization': 'Bearer ' + token\n" +
                "    },\n" +
                "    body: JSON.stringify({\n" +
                "        patientId: 1005,\n" +
                "        title: '高血压用药咨询',\n" +
                "        description: '我最近血压有点高，想咨询一下降压药的用法和注意事项',\n" +
                "        intendedDrugs: ['硝苯地平', '氨氯地平'],\n" +
                "        attachments: ['血压记录表.jpg'],\n" +
                "        toDoctor: false\n" +
                "    })\n" +
                "});\n" +
                "const result = await response.json();",
            "分页查询", "const response = await fetch('/admin-api/help-orders/page?pageNo=1&pageSize=10&status=SUBMITTED', {\n" +
                "    headers: {\n" +
                "        'Authorization': 'Bearer ' + token\n" +
                "    }\n" +
                "});\n" +
                "const result = await response.json();"
        ));
        
        examples.put("Java", Map.of(
            "创建工单", "HelpOrderCreateReqVO createReqVO = new HelpOrderCreateReqVO();\n" +
                "createReqVO.setPatientId(1005L);\n" +
                "createReqVO.setTitle(\"高血压用药咨询\");\n" +
                "createReqVO.setDescription(\"我最近血压有点高，想咨询一下降压药的用法和注意事项\");\n" +
                "createReqVO.setIntendedDrugs(Arrays.asList(\"硝苯地平\", \"氨氯地平\"));\n" +
                "createReqVO.setAttachments(Arrays.asList(\"血压记录表.jpg\"));\n" +
                "createReqVO.setToDoctor(false);\n" +
                "\n" +
                "CommonResult<Long> result = helpOrderController.createHelpOrder(createReqVO);",
            "分页查询", "HelpOrderPageReqVO pageReqVO = new HelpOrderPageReqVO();\n" +
                "pageReqVO.setPageNo(1);\n" +
                "pageReqVO.setPageSize(10);\n" +
                "pageReqVO.setStatus(\"SUBMITTED\");\n" +
                "\n" +
                "CommonResult<PageResult<HelpOrderRespVO>> result = helpOrderController.getHelpOrderPage(pageReqVO);"
        ));
        
        return examples;
    }

    // 示例方法
    private Map<String, Object> getCreateExample() {
        return Map.of(
            "request", Map.of(
                "patientId", 1005,
                "title", "高血压用药咨询",
                "description", "我最近血压有点高，想咨询一下降压药的用法和注意事项",
                "intendedDrugs", new String[]{"硝苯地平", "氨氯地平"},
                "attachments", new String[]{"血压记录表.jpg"},
                "toDoctor", false
            ),
            "response", Map.of(
                "code", 0,
                "data", 12345,
                "msg", "success"
            )
        );
    }

    private Map<String, Object> getGetExample() {
        return Map.of(
            "request", Map.of("id", 1),
            "response", Map.of(
                "code", 0,
                "data", Map.of(
                    "id", 1,
                    "patientId", 1005,
                    "title", "高血压用药咨询",
                    "status", "SUBMITTED",
                    "timeline", "array"
                ),
                "msg", "success"
            )
        );
    }

    private Map<String, Object> getPageExample() {
        return Map.of(
            "request", Map.of(
                "pageNo", 1,
                "pageSize", 10,
                "status", "SUBMITTED"
            ),
            "response", Map.of(
                "code", 0,
                "data", Map.of(
                    "list", "array",
                    "total", 100
                ),
                "msg", "success"
            )
        );
    }

    private Map<String, Object> getUpdateExample() {
        return Map.of(
            "request", Map.of(
                "id", 1,
                "title", "高血压用药咨询（更新）",
                "description", "更新后的描述"
            ),
            "response", Map.of(
                "code", 0,
                "data", true,
                "msg", "success"
            )
        );
    }

    private Map<String, Object> getAssignExample() {
        return Map.of(
            "request", Map.of(
                "id", 1,
                "pharmacistId", 1001
            ),
            "response", Map.of(
                "code", 0,
                "data", Map.of(
                    "id", 1,
                    "status", "ASSIGNED_PHARMACIST",
                    "assignedPharmacistId", 1001
                ),
                "msg", "success"
            )
        );
    }

    private Map<String, Object> getEscalateExample() {
        return Map.of(
            "request", Map.of(
                "id", 1,
                "needDoctor", true,
                "doctorId", 1003
            ),
            "response", Map.of(
                "code", 0,
                "data", Map.of(
                    "id", 1,
                    "status", "TO_DOCTOR",
                    "assignedDoctorId", 1003
                ),
                "msg", "success"
            )
        );
    }

    private Map<String, Object> getRejectExample() {
        return Map.of(
            "request", Map.of(
                "id", 1,
                "reason", "患者信息不完整，无法提供准确建议"
            ),
            "response", Map.of(
                "code", 0,
                "data", Map.of(
                    "id", 1,
                    "status", "REJECTED",
                    "rejectReason", "患者信息不完整，无法提供准确建议"
                ),
                "msg", "success"
            )
        );
    }

    private Map<String, Object> getUpdateStatusExample() {
        return Map.of(
            "request", Map.of(
                "id", 1,
                "status", "DONE",
                "reason", "患者确认完成"
            ),
            "response", Map.of(
                "code", 0,
                "data", Map.of(
                    "id", 1,
                    "status", "DONE"
                ),
                "msg", "success"
            )
        );
    }

    private Map<String, Object> getExportExample() {
        return Map.of(
            "request", Map.of(
                "status", "SUBMITTED",
                "patientId", 1005,
                "title", "高血压"
            ),
            "response", "Excel文件流"
        );
    }

    private Map<String, Object> getDeleteExample() {
        return Map.of(
            "request", Map.of("id", 1),
            "response", Map.of(
                "code", 0,
                "data", true,
                "msg", "success"
            )
        );
    }
}
