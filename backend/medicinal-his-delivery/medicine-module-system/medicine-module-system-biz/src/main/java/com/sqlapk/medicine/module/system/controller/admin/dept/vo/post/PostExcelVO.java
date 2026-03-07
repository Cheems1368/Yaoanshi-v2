package com.sqlapk.medicine.module.system.controller.admin.dept.vo.post;

import com.alibaba.excel.annotation.ExcelProperty;
import com.sqlapk.medicine.framework.excel.core.annotations.DictFormat;
import com.sqlapk.medicine.framework.excel.core.convert.DictConvert;
import com.sqlapk.medicine.module.system.enums.DictTypeConstants;
import lombok.Data;

/**
 * 岗位 Excel 导出响应 VO
 */
@Data
public class PostExcelVO {

    @ExcelProperty("岗位序号")
    private Long id;

    @ExcelProperty("岗位编码")
    private String code;

    @ExcelProperty("岗位名称")
    private String name;

    @ExcelProperty("岗位排序")
    private Integer sort;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private String status;

}
