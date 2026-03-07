package com.sqlapk.medicine.module.patient.service.options;

import java.util.*;
import javax.validation.*;
import com.sqlapk.medicine.module.patient.controller.admin.options.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.options.OptionsDO;
import com.sqlapk.medicine.framework.common.pojo.PageResult;

/**
 * 问题选项 Service 接口
 *
 * @author 管理员
 */
public interface OptionsService {

    /**
     * 创建问题选项
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOptions(@Valid OptionsCreateReqVO createReqVO);

    /**
     * 更新问题选项
     *
     * @param updateReqVO 更新信息
     */
    void updateOptions(@Valid OptionsUpdateReqVO updateReqVO);

    /**
     * 删除问题选项
     *
     * @param id 编号
     */
    void deleteOptions(Long id);

    /**
     * 获得问题选项
     *
     * @param id 编号
     * @return 问题选项
     */
    OptionsDO getOptions(Long id);

    /**
     * 获得问题选项列表
     *
     * @param ids 编号
     * @return 问题选项列表
     */
    List<OptionsDO> getOptionsList(Collection<Long> ids);

    /**
     * 获得问题选项分页
     *
     * @param pageReqVO 分页查询
     * @return 问题选项分页
     */
    PageResult<OptionsDO> getOptionsPage(OptionsPageReqVO pageReqVO);

    /**
     * 获得问题选项列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 问题选项列表
     */
    List<OptionsDO> getOptionsList(OptionsExportReqVO exportReqVO);

}
