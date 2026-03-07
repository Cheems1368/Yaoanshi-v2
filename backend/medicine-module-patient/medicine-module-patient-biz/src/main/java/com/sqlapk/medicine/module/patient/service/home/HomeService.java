package com.sqlapk.medicine.module.patient.service.home;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.home.HomeDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 居家药学服务 Service 接口
 *
 * @author 管理员
 */
public interface HomeService {

    /**
     * 创建居家药学服务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createHome(@Valid HomeCreateReqVO createReqVO);

    /**
     * 更新居家药学服务
     *
     * @param updateReqVO 更新信息
     */
    void updateHome(@Valid HomeUpdateReqVO updateReqVO);

    /**
     * 删除居家药学服务
     *
     * @param id 编号
     */
    void deleteHome(Long id);

    /**
     * 获得居家药学服务
     *
     * @param id 编号
     * @return 居家药学服务
     */
    HomeDO getHome(Long id);

    /**
     * 获得居家药学服务列表
     *
     * @param ids 编号
     * @return 居家药学服务列表
     */
    List<HomeDO> getHomeList(Collection<Long> ids);

    /**
     * 获得居家药学服务分页
     *
     * @param pageReqVO 分页查询
     * @return 居家药学服务分页
     */
    PageResult<HomeDO> getHomePage(HomePageReqVO pageReqVO);

    /**
     * 获得居家药学服务列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 居家药学服务列表
     */
    List<HomeDO> getHomeList(HomeExportReqVO exportReqVO);

}
