package com.sqlapk.medicine.module.drugs.service.drug;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsCreateReqVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsExportReqVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsPageReqVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsUpdateReqVO;
import com.sqlapk.medicine.module.drugs.dal.dataobject.drug.DrugsDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 药品 Service 接口
 *
 * @author 管理员
 */
public interface DrugsService {

    /**
     * 创建药品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long create(@Valid DrugsCreateReqVO createReqVO);

    /**
     * 更新药品
     *
     * @param updateReqVO 更新信息
     */
    void update(@Valid DrugsUpdateReqVO updateReqVO);

    /**
     * 删除药品
     *
     * @param id 编号
     */
    void delete(Long id);

    /**
     * 获得药品
     *
     * @param id 编号
     * @return 药品
     */
    DrugsDO get(Long id);

    /**
     * 获得药品列表
     *
     * @param ids 编号
     * @return 药品列表
     */
    List<DrugsDO> getList(Collection<Long> ids);

    /**
     * 获得药品分页
     *
     * @param pageReqVO 分页查询
     * @return 药品分页
     */
    PageResult<DrugsDO> getPage(DrugsPageReqVO pageReqVO);

    /**
     * 获得药品列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 药品列表
     */
    List<DrugsDO> getList(DrugsExportReqVO exportReqVO);

    /**
     * 根据药品名称获得药品信息
     *
     * @param drugName 药品名称
     * @return 药品信息
     */
    DrugsDO getByName(String drugName);

}
