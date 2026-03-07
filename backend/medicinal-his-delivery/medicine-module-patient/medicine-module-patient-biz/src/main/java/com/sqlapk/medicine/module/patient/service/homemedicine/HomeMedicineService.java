package com.sqlapk.medicine.module.patient.service.homemedicine;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicinePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homemedicine.HomeMedicineDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 用药清单 Service 接口
 *
 * @author 管理员
 */
public interface HomeMedicineService {

    /**
     * 创建用药清单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createHomeMedicine(@Valid HomeMedicineCreateReqVO createReqVO);

    /**
     * 更新用药清单
     *
     * @param updateReqVO 更新信息
     */
    void updateHomeMedicine(@Valid HomeMedicineUpdateReqVO updateReqVO);

    /**
     * 删除用药清单
     *
     * @param id 编号
     */
    void deleteHomeMedicine(Long id);

    /**
     * 获得用药清单
     *
     * @param id 编号
     * @return 用药清单
     */
    HomeMedicineDO getHomeMedicine(Long id);

    /**
     * 获得用药清单列表
     *
     * @param ids 编号
     * @return 用药清单列表
     */
    List<HomeMedicineDO> getHomeMedicineList(Collection<Long> ids);

    /**
     * 获得用药清单分页
     *
     * @param pageReqVO 分页查询
     * @return 用药清单分页
     */
    PageResult<HomeMedicineDO> getHomeMedicinePage(HomeMedicinePageReqVO pageReqVO);

    /**
     * 获得用药清单列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 用药清单列表
     */
    List<HomeMedicineDO> getHomeMedicineList(HomeMedicineExportReqVO exportReqVO);

}
