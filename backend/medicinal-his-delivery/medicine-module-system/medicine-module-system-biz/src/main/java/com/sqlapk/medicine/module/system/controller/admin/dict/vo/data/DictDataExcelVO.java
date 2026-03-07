package com.sqlapk.medicine.module.system.controller.admin.dict.vo.data;

import com.alibaba.excel.annotation.ExcelProperty;
import com.sqlapk.medicine.framework.excel.core.annotations.DictFormat;
import com.sqlapk.medicine.framework.excel.core.convert.DictConvert;
import com.sqlapk.medicine.module.system.enums.DictTypeConstants;
import lombok.Data;

/**
 * 字典数据 Excel 导出响应 VO
 */
@Data
public class DictDataExcelVO {

    @ExcelProperty("字典编码")
    private Long id;

    @ExcelProperty("字典排序")
    private Integer sort;

    @ExcelProperty("字典标签")
    private String label;

    @ExcelProperty("字典键值")
    private String value;

    @ExcelProperty("字典类型")
    private String dictType;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

}
